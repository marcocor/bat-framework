package it.unipi.di.acube.batframework.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikipediaInterfaceWAT extends WikipediaInterface {
	public static class WikipediaInterfaceWATBuilder {
		String uri = "https://wat.d4science.org/wat", gcubeToken;
		boolean cache = false;

		public static WikipediaInterfaceWATBuilder builder() {
			return new WikipediaInterfaceWATBuilder();
		}

		public WikipediaInterfaceWAT build() throws URISyntaxException {
			return new WikipediaInterfaceWAT(uri, cache, gcubeToken);
		}

		public WikipediaInterfaceWATBuilder baseUri(String baseUri) {
			this.uri = baseUri;
			return this;
		}

		public WikipediaInterfaceWATBuilder cache() {
			this.cache = true;
			return this;
		}

		public WikipediaInterfaceWATBuilder gcubeToken(String gcubeToken) {
			this.gcubeToken = gcubeToken;
			return this;
		}
	}

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private URIBuilder uriBuilder;
	private String gcubeToken;
	private Map<URI, JSONObject> cache;

	private WikipediaInterfaceWAT(String baseUri, boolean cache, String gcubeToken) throws URISyntaxException {
		this.uriBuilder = new URIBuilder(baseUri + (baseUri.endsWith("/") ? "title" : "/title"));
		this.gcubeToken = gcubeToken;
		if (cache)
			this.cache = new HashMap<URI, JSONObject>();
	}

	private JSONObject query(String title, int wid) throws UnsupportedOperationException, JSONException {

		URI request = null;
		synchronized (uriBuilder) {
			try {
				if (gcubeToken != null)
					uriBuilder.addParameter("gcube-token", gcubeToken);
				if (title != null)
					uriBuilder.addParameter("title", title.replace(' ', '_'));
				else
					uriBuilder.addParameter("id", Integer.toString(wid));
				request = uriBuilder.build();
				uriBuilder.clearParameters();

			} catch (URISyntaxException e) {
				throw new AnnotationException(e.getMessage());
			}
		}
		HttpGet get = new HttpGet(request);
		get.setHeader("Accept", "application/json");

		HttpClient httpClient = HttpClientBuilder.create().build();
		LOG.debug("<querying> {}", request.toString());
		HttpResponse response;
		if (cache != null && cache.containsKey(request))
			return cache.get(request);
		try {
			response = httpClient.execute(get);

			if (response.getStatusLine().getStatusCode() != 200) {
				String msg = IOUtils.toString(response.getEntity().getContent(), "utf-8");
				LOG.error("Got HTTP error {}. Message is: {}", response.getStatusLine().getStatusCode());
				throw new AnnotationException("Got response message:" + msg);
			}
			JSONObject respObj = new JSONObject(IOUtils.toString(response.getEntity().getContent(), "utf-8"));
			if (cache != null)
				cache.put(request, respObj);
			return respObj;
		} catch (IOException e) {
			throw new AnnotationException(e.getMessage());
		}
	}

	@Override
	public int getIdByTitle(String title) throws IOException {
		JSONObject res;
		try {
			res = query(title, -1);
			return res.getInt("wiki_id");
		} catch (UnsupportedOperationException | JSONException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public String getTitlebyId(int wid) throws IOException {
		JSONObject res;
		try {
			res = query(null, wid);
			if (res.getString("wiki_title").isEmpty())
				return null;
			return normalize(res.getString("wiki_title"));
		} catch (UnsupportedOperationException | JSONException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public boolean isRedirect(int wid) throws IOException {
		JSONObject res;
		try {
			res = query(null, wid);
			return res.getInt("type") == 1;
		} catch (UnsupportedOperationException | JSONException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public int dereference(int wid) throws IOException {
		JSONObject res;
		try {
			res = query(null, wid);
			if (res.getInt("type") == 1)
				return res.getInt("redirect_id");
			return wid;
		} catch (UnsupportedOperationException | JSONException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
	}

	@Override
	public void prefetchTitles(List<String> titlesToPrefetch) {
	}

	@Override
	public void prefetchWids(List<Integer> widsToPrefetch) {
	}

	@Override
	public void flush() {
	}

}
