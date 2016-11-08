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

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.MultipleAnnotation;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.CandidatesSpotter;
import it.unipi.di.acube.batframework.problems.MentionSpotter;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class WATAnnotator implements Sa2WSystem, MentionSpotter,
		CandidatesSpotter {

	private static final int RETRY_N = 2;
	private long lastTime = 0;
	private boolean useContext, useTagger, bogusFilter;
	private final String urlTag;
	private final String urlSpot;
	private final String urlD2W;
	private final String method, relatedness, windowSize, minCommonness,
			minLinkProbability, epsilon, kappa;
	private String sortBy;
	private HashMap<Mention, HashMap<String, Double>> additionalInfo = new HashMap<>();
	private HashMap<Mention, List<HashMap<String, Double>>> additionalCandidatesInfo = new HashMap<>();
	private boolean brutalD2WReduction = false;

	public WATAnnotator(String ip, int port, String method) {
		this(ip, port, method, "PAGERANK", "mw", "", "");
	}

	public WATAnnotator(String ip, int port, String method, String sortBy,
			String relatedness, String epsilon, String minLinkProbability) {
		this(ip, port, method, sortBy, relatedness, epsilon,
				minLinkProbability, false, false, false);
	}

	public WATAnnotator(String ip, int port, String method, String sortBy,
			String relatedness, String epsilon, String minLinkProbability,
			boolean useContext, boolean useTagger, boolean bogusFilter) {
		this.urlTag = String.format("http://%s:%d/tag/tag", ip, port);
		this.urlSpot = String.format("http://%s:%d/tag/spot", ip, port);
		this.urlD2W = String.format("http://%s:%d/tag/disambiguate", ip, port);
		this.method = method;
		this.epsilon = epsilon;
		this.windowSize = "";
		this.minCommonness = "";
		this.kappa = "";
		this.useContext = useContext;
		this.useTagger = useTagger;
		this.bogusFilter = bogusFilter;
		this.minLinkProbability = minLinkProbability;
		this.sortBy = sortBy;
		this.relatedness = relatedness;
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
		return String
				.format("WikiSense (method=%s epsilon=%s usecontext=%b relatedness=%s sortby=%s)",
						method, epsilon.equals("") ? "default" : epsilon,
						useContext, relatedness, sortBy);
	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

	public HashSet<Annotation> solveD2WParams(String text,
			HashSet<Mention> mentions, String newMinCommonness,
			String newEpsilon, String kappa) throws JSONException {
		System.out.println(text.substring(0, Math.min(30, text.length())));
		HashSet<Annotation> res = new HashSet<Annotation>();
		JSONObject obj = null;

		try {
			obj = queryJson(getRequestParameters(text, mentions), urlD2W,
					generateGetParameters(newMinCommonness, newEpsilon, kappa),
					RETRY_N);
			System.out.println(obj);
			lastTime = obj.getJSONObject("time").getInt("total");

		} catch (Exception e) {
			System.err
					.print("Got error while querying WikiSense API with GET parameters: "
							+ generateGetParameters(newMinCommonness,
									newEpsilon, kappa) + " with text: " + text);
			e.printStackTrace();
			throw new AnnotationException(
					"An error occurred while querying WikiSense API. Message: "
							+ e.getMessage());
		}
		JSONArray jsAnnotations = obj.getJSONArray("annotations");
		for (int i = 0; i < jsAnnotations.length(); i++) {
			JSONObject jsAnn = jsAnnotations.getJSONObject(i);
			// System.out.println(js_ann);
			int start = jsAnn.getInt("start");
			int end = jsAnn.getInt("end");
			int id = jsAnn.getInt("id");
			double lp = jsAnn.getDouble("linkProb");
			double commonness = jsAnn.getDouble("commonness");
			double rhoScore = jsAnn.getDouble("rho");
			double ambiguity = 1.0 / (1.0 + jsAnn.getInt("ambiguity"));
			double localCoherence = jsAnn.getDouble("localCoherence");
			double pageRank = jsAnn.getDouble("pageRank");
			// System.out.println(text.substring(start, end) + "->" + id);

			Mention m = new Mention(start, end - start);
			if (mentions.contains(m))
				res.add(new Annotation(m.getPosition(), m.getLength(), id));

			if (!additionalInfo.containsKey(m))
				additionalInfo.put(m, new HashMap<String, Double>());
			additionalInfo.get(m).put("lp", lp);
			additionalInfo.get(m).put("commonness", commonness);
			additionalInfo.get(m).put("rhoScore", rhoScore);
			additionalInfo.get(m).put("ambiguity", ambiguity);
			additionalInfo.get(m).put("localCoherence", localCoherence);
			additionalInfo.get(m).put("pageRank", pageRank);

			JSONArray jsRankings = jsAnn.getJSONArray("ranking");
			int rank = 0;
			for (int j = 0; j < jsRankings.length(); j++) {
				JSONObject jsRanking = jsRankings.getJSONObject(j);
				id = jsRanking.getInt("id");
				commonness = jsRanking.getDouble("commonness");
				double score = jsRanking.getDouble("score");
				pageRank = jsRanking.getDouble("pageRank");
				int synonimy = jsRanking.getInt("synonymy");

				HashMap<String, Double> values = new HashMap<>();
				values.put("id", (double) id);
				values.put("rank", (double) rank);
				values.put("commonness", (double) commonness);
				values.put("score", (double) score);
				values.put("pageRank", (double) pageRank);
				values.put("synonimy", (double) synonimy);
				values.put("lp", (double) lp);
				values.put("ambiguity", (double) ambiguity);
				if (!additionalCandidatesInfo.containsKey(m))
					additionalCandidatesInfo.put(m,
							new Vector<HashMap<String, Double>>());
				additionalCandidatesInfo.get(m).add(values);
				rank++;
			}
		}
		return res;
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions)
			throws AnnotationException {
		if (brutalD2WReduction)
			return ProblemReduction.Sa2WToD2W(this.solveSa2W(text), mentions,
					-1f);
		try {
			return solveD2WParams(text, mentions, minCommonness, epsilon, kappa);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		// System.out.println(text);
		HashSet<ScoredTag> res = new HashSet<ScoredTag>();
		JSONObject obj = null;
		String getParameters = String.format("lang=%s", "en");
		if (!method.equals(""))
			getParameters += String.format("&method=%s", method);
		if (!windowSize.equals(""))
			getParameters += String.format("&windowSize=%s", windowSize);
		if (!epsilon.equals(""))
			getParameters += String.format("&epsilon=%s", epsilon);
		if (!minCommonness.equals(""))
			getParameters += String.format("&minCommonness=%s", minCommonness);
		try {
			obj = queryJson(getRequestParameters(text, null), urlTag, getParameters, RETRY_N);
			lastTime = obj.getJSONObject("time").getInt("total");

		} catch (Exception e) {
			System.out
					.print("Got error while querying WikiSense API with GET parameters: "
							+ getParameters + " with text: " + text);
			throw new AnnotationException(
					"An error occurred while querying WikiSense API. Message: "
							+ e.getMessage());
		}

		try {
			JSONArray jsAnnotations = obj.getJSONArray("annotations");
			for (int i = 0; i < jsAnnotations.length(); i++) {
				JSONObject js_ann = jsAnnotations.getJSONObject(i);
				JSONArray jsRanking = js_ann.getJSONArray("ranking");
				// System.out.println(jsRanking);
				for (int j = 0; j < jsRanking.length(); j++) {
					JSONObject jsCand = jsRanking.getJSONObject(j);
					int id = jsCand.getInt("id");
					double rho = jsCand.getDouble("score");
					// System.out.println(id + " (" + rho + ")");
					res.add(new ScoredTag(id, (float) rho));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}

		return res;
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String text)
			throws AnnotationException {
		// System.out.println(text);
		HashSet<ScoredAnnotation> res = new HashSet<ScoredAnnotation>();
		JSONObject obj = null;
		try {
			obj = queryJson(getRequestParameters(text, null), urlTag,
					generateGetParameters(minCommonness, epsilon, kappa),
					RETRY_N);
			lastTime = obj.getJSONObject("time").getInt("total");

		} catch (Exception e) {
			System.out
					.print("Got error while querying WikiSense API with GET parameters: "
							+ generateGetParameters(minCommonness, epsilon,
									kappa) + " with text: " + text);

			throw new AnnotationException(
					"An error occurred while querying WikiSense API. Message: "
							+ e.getMessage());
		}
		try {
			JSONArray jsAnnotations = obj.getJSONArray("annotations");
			for (int i = 0; i < jsAnnotations.length(); i++) {
				JSONObject js_ann = jsAnnotations.getJSONObject(i);
				// System.out.println(js_ann);
				int start = js_ann.getInt("start");
				int end = js_ann.getInt("end");
				int id = js_ann.getInt("id");
				double lp = js_ann.getDouble("linkProb");
				double commonness = js_ann.getDouble("commonness");
				double rho = js_ann.getDouble("rho");
				double ambiguity = 1.0 / (1.0 + js_ann.getInt("ambiguity"));
				double localCoherence = js_ann.getDouble("localCoherence");
				double pageRank = js_ann.getDouble("pageRank");
				// System.out.println(text.substring(start, end) + "->" + id);

				Mention m = new Mention(start, end - start);
				if (!additionalInfo.containsKey(m))
					additionalInfo.put(m, new HashMap<String, Double>());
				additionalInfo.get(m).put("lp", lp);
				additionalInfo.get(m).put("commonness", commonness);
				additionalInfo.get(m).put("ambiguity", ambiguity);
				additionalInfo.get(m).put("localCoherence", localCoherence);
				additionalInfo.get(m).put("rho", rho);
				additionalInfo.get(m).put("pageRank", pageRank);

				// System.out.println(text.substring(start, end) + "->" + id +
				// " ("
				// + rho + ")");
				res.add(new ScoredAnnotation(start, end - start, id,
						(float) rho));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		return res;
	}

	@Override
	public HashSet<Mention> getSpottedMentions(String text) {
		HashSet<Mention> res = new HashSet<Mention>();
		JSONObject obj = null;
		String getParameters = String.format("lang=%s", "en", method);
		try {
			obj = queryJson(getRequestParameters(text, null), urlSpot, getParameters, RETRY_N);
			// System.out.println(obj);
		} catch (Exception e) {
			System.out
					.print("Got error while querying WikiSense API with GET parameters: "
							+ getParameters + " with text: " + text);
			throw new AnnotationException(
					"An error occurred while querying WikiSense API. Message: "
							+ e.getMessage());
		}
		try {
			JSONArray jsSpots = obj.getJSONArray("spots");
			for (int i = 0; i < jsSpots.length(); i++) {
				JSONObject jsSpot = jsSpots.getJSONObject(i);
				// System.out.println(jsSpot);
				int start = jsSpot.getInt("start");
				int end = jsSpot.getInt("end");
				// System.out.printf("Found spot: [%s]%n", text.substring(start,
				// end));
				Mention newMention = new Mention(start, end - start);
				res.add(newMention);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		return res;
	}

	private String generateGetParameters(String newMinCommonness,
			String newEpsilon, String newKappa) {
		String getParameters = String.format("lang=%s", "en");
		if (!method.equals(""))
			getParameters += String.format("&method=%s", method);
		if (!windowSize.equals(""))
			getParameters += String.format("&windowSize=%s", windowSize);
		if (!newEpsilon.equals(""))
			getParameters += String.format("&epsilon=%s", newEpsilon);
		if (!newMinCommonness.equals(""))
			getParameters += String.format("&minCommonness=%s",
					newMinCommonness);
		if (!newKappa.equals(""))
			getParameters += String.format("&kappa=%s", newKappa);
		if (!minLinkProbability.equals(""))
			getParameters += String.format("&minLinkProbability=%s",
					minLinkProbability);
		if (!relatedness.equals(""))
			getParameters += String.format("&relatedness=%s", relatedness);
		if (!sortBy.equals(""))
			getParameters += String.format("&sortBy=%s", sortBy);
		getParameters += "&bogusFilter=" + this.bogusFilter;
		getParameters += "&useTagger=" + this.useTagger;
		getParameters += "&useContext=" + this.useContext;
		return getParameters;
	}

	private String getRequestParameters(String text, Set<Mention> mentions) throws JSONException{
		JSONObject parameters = new JSONObject();
		if (mentions != null) {
			JSONArray mentionsJson = new JSONArray();
			for (Mention m : mentions) {
				JSONObject mentionJson = new JSONObject();
				mentionJson.put("start", m.getPosition());
				mentionJson.put("end", m.getPosition() + m.getLength());
				mentionsJson.put(mentionJson);
			}
			parameters.put("spans", mentionsJson);
		}
		parameters.put("text", text);
		return parameters.toString();
	}
	
	protected JSONObject queryJson(String parameters,
			String url, String getParameters, int retry) throws Exception {
		byte[] parametersBytes = parameters.getBytes("utf-8");
		String resultStr = null;
		try {
			URL wikiSenseApi = new URL(String.format("%s?%s", url,
					getParameters));

			System.out.println("Querying " + wikiSenseApi.toString());

			HttpURLConnection slConnection = (HttpURLConnection) wikiSenseApi
					.openConnection();
			slConnection.setReadTimeout(0);
			slConnection.setDoOutput(true);
			slConnection.setDoInput(true);
			slConnection.setRequestMethod("POST");
			slConnection.setRequestProperty("Content-Type", "application/json");
			slConnection.setRequestProperty("Content-Length", ""
					+ parametersBytes.length);

			slConnection.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(
					slConnection.getOutputStream());
			wr.write(parametersBytes);
			wr.flush();
			wr.close();

			if (slConnection.getResponseCode() != 200) {
				String errorMsg = IOUtils.toString(slConnection.getErrorStream(), "utf-8");
				System.err.printf("Got HTTP error %d. Message is: %s%n",
						slConnection.getResponseCode(), errorMsg);
			}

			resultStr = IOUtils.toString(slConnection.getInputStream(), "utf-8");
			JSONObject obj = new JSONObject(resultStr);

			return obj;

		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(3000);
				if (retry > 0)
					return queryJson(parameters, url, getParameters,
							retry - 1);
				else
					throw e;
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e1);
			}
		}
	}

	@Override
	public HashSet<MultipleAnnotation> getSpottedCandidates(String text) {
		HashSet<MultipleAnnotation> res = new HashSet<MultipleAnnotation>();
		JSONObject obj = null;
		String getParameters = String.format(
				"lang=%s&includeEntities=true&sortBy=SCORE", "en");
		try {
			obj = queryJson(getRequestParameters(text, null), urlSpot, getParameters, RETRY_N);
		} catch (Exception e) {
			System.out
					.print("Got error while querying WikiSense API with GET parameters: "
							+ getParameters + " with text: " + text);
			throw new AnnotationException(
					"An error occurred while querying WikiSense API. Message: "
							+ e.getMessage());
		}
		try {
			JSONArray jsSpots = obj.getJSONArray("spots");
			for (int i = 0; i < jsSpots.length(); i++) {
				JSONObject jsSpot = jsSpots.getJSONObject(i);
				int start = jsSpot.getInt("start");
				int end = jsSpot.getInt("end");

				JSONArray jsRanking = jsSpot.getJSONArray("ranking");
				int[] rankedCandidates = new int[jsRanking.length()];
				for (int j = 0; j < jsRanking.length(); j++) {
					JSONObject jsCand = jsRanking.getJSONObject(j);
					int id = jsCand.getInt("id");
					rankedCandidates[j] = id;
				}
				MultipleAnnotation newAnnotation = new MultipleAnnotation(
						start, end - start, rankedCandidates);
				res.add(newAnnotation);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		return res;
	}

	public HashMap<Mention, HashMap<String, Double>> getLastQueryAdditionalInfo() {
		HashMap<Mention, HashMap<String, Double>> clone = new HashMap<>(
				additionalInfo);
		additionalInfo.clear();
		return clone;
	}

	public HashMap<Mention, List<HashMap<String, Double>>> getLastQueryAdditionalCandidatesInfo() {
		HashMap<Mention, List<HashMap<String, Double>>> clone = new HashMap<>(
				additionalCandidatesInfo);
		additionalCandidatesInfo.clear();
		return clone;
	}

	public void setBrutalD2WReduction() {
		this.brutalD2WReduction = true;
	}
}
