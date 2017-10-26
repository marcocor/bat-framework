/**
 *  Copyright 2014 Marco Cornolti
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unipi.di.acube.batframework.systemPlugins;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
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

public class WAT2Annotator implements Sa2WSystem {
	public static class WAT2AnnotatorBuilder {
		String baseUri = "https://wat.d4science.org/wat", method = "default", tokenizer = "", gcubeToken = "";
		int debug = 0;

		public static WAT2AnnotatorBuilder builder() {
			return new WAT2AnnotatorBuilder();
		}

		public WAT2AnnotatorBuilder tokenizer(String tokenizer) {
			this.tokenizer = tokenizer;
			return this;
		}

		public WAT2AnnotatorBuilder method(String method) {
			this.method = method;
			return this;
		}

		public WAT2AnnotatorBuilder enableAdditionalInfo() {
			if (!Arrays.asList(method.split(",")).contains("pagerankinfo"))
				method += ",pagerankinfo";
			this.debug = 8;
			return this;
		}

		public WAT2Annotator build() {
			return new WAT2Annotator(baseUri, tokenizer, method, debug, gcubeToken);
		}

		public WAT2AnnotatorBuilder baseUri(String uri) {
			this.baseUri = uri;
			return this;
		}

		public WAT2AnnotatorBuilder gcubeToken(String gcubeToken) {
			this.gcubeToken = gcubeToken;
			return this;
		}
	}

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private long lastTime = 0;
	private final String urlJson, method;
	private HashMap<Mention, HashMap<String, Double>> additionalInfo = new HashMap<>();
	private String tokenizer;
	private int debug;
	private String gcubeToken;

	protected WAT2Annotator(String uri, String tokenizer, String method, int debug, String gcubeToken) {
		this.urlJson = String.format("%s/tag/json", uri);
		this.method = method;
		this.tokenizer = tokenizer;
		this.debug = debug;
		this.gcubeToken = gcubeToken;
	}

	@Override
	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text));
	}

	@Override
	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	@Override
	public String getName() {
		return String.format("WAT (tokenizer=%s method=%s)", tokenizer, method);
	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException {
		HashSet<Annotation> res = new HashSet<Annotation>();

		if (text.isEmpty())
			return res;

		JSONObject obj = null;

		try {
			obj = queryJson(urlJson, generateGetParameters(mentions, text));
			lastTime = getTime(obj);

		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException("An error occurred while querying WAT2 API. Message: " + e.getMessage());
		}
		JSONArray jsAnnotations;
		try {
			jsAnnotations = obj.getJSONArray("annotations");
			for (int i = 0; i < jsAnnotations.length(); i++) {
				JSONObject jsAnn = jsAnnotations.getJSONObject(i);
				int start = jsAnn.getInt("start");
				int end = jsAnn.getInt("end");
				int id = jsAnn.getInt("id");

				Mention m = new Mention(start, end - start);
				if (mentions.contains(m))
					res.add(new Annotation(m.getPosition(), m.getLength(), id));

				if (((debug & 8) != 0) && !additionalInfo.containsKey(m))
					additionalInfo.put(m, additionalInfos(jsAnn.getJSONObject("explanation")));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		return res;
	}

	private static HashMap<String, Double> additionalInfos(JSONObject explanation) throws JSONException {
		HashMap<String, Double> res = new HashMap<>();
		res.put("lp", explanation.getJSONObject("prior_explanation").getDouble("link_prob"));
		res.put("commonness", explanation.getJSONObject("prior_explanation").getDouble("entity_mention_probability"));
		res.put("rho", explanation.getJSONObject("confidence_explanation").getDouble("confidence"));
		res.put("ambiguity", 1.0 / (1.0 + explanation.getJSONObject("prior_explanation").getInt("ambiguity")));
		res.put("pageRank", explanation.getJSONObject("pagerank_explanation").getDouble("pagerank_score"));
		return res;
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		HashSet<ScoredAnnotation> res = new HashSet<ScoredAnnotation>();

		if (text.isEmpty())
			return res;

		JSONObject obj = null;
		try {
			obj = queryJson(urlJson, generateGetParameters(null, text));
			lastTime = getTime(obj);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException("An error occurred while querying WAT2 API. Message: " + e.getMessage());
		}
		try {
			JSONArray jsAnnotations = obj.getJSONArray("annotations");
			for (int i = 0; i < jsAnnotations.length(); i++) {
				JSONObject jsAnn = jsAnnotations.getJSONObject(i);
				int start = jsAnn.getInt("start");
				int end = jsAnn.getInt("end");
				int id = jsAnn.getInt("id");
				double rho = jsAnn.getDouble("rho");

				Mention m = new Mention(start, end - start);
				if (((debug & 8) != 0) && !additionalInfo.containsKey(m))
					additionalInfo.put(m, additionalInfos(jsAnn.getJSONObject("explanation")));

				res.add(new ScoredAnnotation(start, end - start, id, (float) rho));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		return res;
	}

	private List<Pair<String, String>> generateGetParameters(Collection<Mention> mentions, String text) throws JSONException {
		List<Pair<String, String>> params = new Vector<>();
		params.add(new ImmutablePair<String, String>("lang", "en"));
		if (!method.equals(""))
			params.add(new ImmutablePair<String, String>("method", method));
		if (!tokenizer.equals(""))
			params.add(new ImmutablePair<String, String>("tokenizer", tokenizer));
		if (!gcubeToken.equals(""))
			params.add(new ImmutablePair<String, String>("gcube-token", gcubeToken));
		if (debug != 0)
			params.add(new ImmutablePair<String, String>("debug", Integer.toString(debug)));

		JSONObject document = new JSONObject();
		if (mentions != null) {
			JSONArray mentionsJson = new JSONArray();
			for (Mention m : mentions.stream().sorted().collect(Collectors.toList())) {
				JSONObject mentionJson = new JSONObject();
				mentionJson.put("start", m.getPosition());
				mentionJson.put("end", m.getEnd());
				mentionsJson.put(mentionJson);
			}
			document.put("spans", mentionsJson);
		}
		document.put("text", text);

		params.add(new ImmutablePair<String, String>("document", document.toString()));

		return params;
	}

	protected URI getRequestUri(String baseUrl, List<Pair<String, String>> params) {
		URI requestUri = null;
		try {
			URIBuilder builder = new URIBuilder(baseUrl);
			for (Pair<String, String> keyValue : params)
				builder.addParameter(keyValue.getKey(), keyValue.getValue());
			requestUri = builder.build();
			builder.clearParameters();
		} catch (URISyntaxException e) {
			throw new AnnotationException(e.getMessage());
		}
		return requestUri;
	}

	protected JSONObject queryJson(String baseUrl, List<Pair<String, String>> params) throws Exception {
		URI requestUri = getRequestUri(baseUrl, params);
		HttpGet request = new HttpGet(requestUri);

		HttpClient httpClient = HttpClientBuilder.create().build();
		LOG.info("<querying> {}", requestUri.toString());
		HttpResponse response;
		try {
			response = httpClient.execute(request);

			if (response.getStatusLine().getStatusCode() != 200) {
				String msg = IOUtils.toString(response.getEntity().getContent(), "utf-8");
				LOG.error("Got HTTP error {}. Message is: {}", response.getStatusLine().getStatusCode());
				throw new AnnotationException("Got response message:" + msg);
			}
		} catch (IOException e) {
			throw new AnnotationException(e.getMessage());
		}

		JSONObject obj = new JSONObject(EntityUtils.toString(response.getEntity(), Charsets.UTF_8));

		return obj;

	}

	private static int getTime(JSONObject result) throws JSONException {
		JSONObject metrics = result.getJSONObject("metrics");
		return (int) ((metrics.getLong("time_tokenize") + metrics.getLong("time_spot") + metrics.getLong("time_disambiguation"))
		        / 1000000.0);

	}

	public HashMap<Mention, HashMap<String, Double>> getLastQueryAdditionalInfo() {
		HashMap<Mention, HashMap<String, Double>> clone = new HashMap<>(additionalInfo);
		additionalInfo.clear();
		return clone;
	}
}
