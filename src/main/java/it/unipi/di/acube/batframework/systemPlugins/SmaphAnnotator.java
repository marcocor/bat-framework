package it.unipi.di.acube.batframework.systemPlugins;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

public class SmaphAnnotator implements Sa2WSystem {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static URIBuilder uriBuilder = new URIBuilder();
	
	SmaphVersion v = SmaphVersion.DEFAULT;
	private String apiUri = "https://smaph.d4science.org/smaph/annotate";
	private long lastTime = -1;
	private String googleCseId;
	private String googleApiKey;
	private String gcubeToken;

	public enum SmaphVersion {
		SMAPH1("ef"), SMAPHS("ar"), SMAPH2("coll"), DEFAULT("default");
		private String param;

		private SmaphVersion(String name) {
			this.param = name;
		}

		public String toParam() {
			return this.param;
		}
	}

	public SmaphAnnotator(String googleCseId, String googleApiKey) {
		this.googleApiKey = googleApiKey;
		this.googleCseId = googleCseId;
	}

	public SmaphAnnotator setGcubeToken(String token) {
		this.gcubeToken = token;
		return this;
	}

	public SmaphAnnotator setVersion(SmaphVersion v) {
		this.v = v;
		return this;
	}

	public SmaphAnnotator setUri(String apiUri) {
		this.apiUri = apiUri;
		return this;
	}

	@Override
	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(this.solveSa2W(text));
	}

	@Override
	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(ProblemReduction.Sa2WToA2W(this.solveSa2W(text)));
	}

	@Override
	public String getName() {
		return v.toString();
	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions, Float.NEGATIVE_INFINITY);
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String query) throws AnnotationException {
		lastTime = Calendar.getInstance().getTimeInMillis();

		URI request = null;
		synchronized (uriBuilder) {
			try {
				request = new URIBuilder(apiUri).addParameter("q", query).addParameter("annotator", v.toParam())
				        .addParameter("google-cse-id", googleCseId).addParameter("google-api-key", googleApiKey)
				        .addParameter("gcube-token", this.gcubeToken).build();

			} catch (URISyntaxException e) {
				throw new AnnotationException(e.getMessage());
			}
		}
		HttpGet get = new HttpGet(request);
		get.setHeader("Accept", "application/json");
		get.setHeader("Content-Type", "multipart/form-data");

		HttpClient httpClient = HttpClientBuilder.create().build();
		LOG.info("<querying> {}", request.toString());
		HttpResponse response;
		try {
			response = httpClient.execute(get);

			if (response.getStatusLine().getStatusCode() != 200) {
				String msg = IOUtils.toString(response.getEntity().getContent(), "utf-8");
				LOG.error("Got HTTP error {}. Message is: {}", response.getStatusLine().getStatusCode());
				throw new AnnotationException("Got response message:" + msg);
			}
		} catch (IOException e) {
			throw new AnnotationException(e.getMessage());
		}

		HashSet<ScoredAnnotation> result = new HashSet<>();
		try {
			JSONObject jsonResp = new JSONObject(IOUtils.toString(response.getEntity().getContent(), "utf-8"));
			JSONArray jsonAnns = jsonResp.getJSONArray("annotations");
			for (int i = 0; i < jsonAnns.length(); i++) {
				JSONObject jsonAnn = jsonAnns.getJSONObject(i);
				result.add(new ScoredAnnotation(jsonAnn.getInt("begin"), jsonAnn.getInt("end") - jsonAnn.getInt("begin"),
				        jsonAnn.getInt("wid"), jsonAnn.has("score") ? (float) ((jsonAnn.getDouble("score") + 1.0) / 2.0) : 1.0f));
			}
		} catch (UnsupportedOperationException | JSONException | IOException e) {
			throw new AnnotationException("Could not parse Json");
		}

		lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;
		return result;
	}
}
