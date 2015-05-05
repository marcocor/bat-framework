package it.unipi.di.acube.batframework.systemPlugins;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.xml.sax.SAXException;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.MentionSpotter;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class AIDADefaultAnnotator implements Sa2WSystem, MentionSpotter {
	private long lastTime = 0;
	private String url;
	private WikipediaApiInterface api;
	private String tech;

	public AIDADefaultAnnotator(String url, String tech,
			WikipediaApiInterface api) {
		this.url = url;
		this.api = api;
		this.tech = tech;
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
		return String.format("AIDA - (%s)", tech);
	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions)
			throws AnnotationException {
		List<Mention> mentionsList = new Vector<Mention>();
		mentionsList.addAll(mentions);
		Collections.sort(mentionsList);
		String spotString = "";
		int lastChar = 0;
		for (Mention m : mentionsList)
			System.out.println(m.toString()
					+ " "
					+ text.substring(m.getPosition(),
							m.getPosition() + m.getLength()));

		for (Mention m : mentionsList) {
			spotString += text.substring(lastChar, m.getPosition());
			spotString += "[[";
			spotString += text.substring(m.getPosition(),
					m.getPosition() + m.getLength());
			spotString += "]]";
			lastChar = m.getPosition() + m.getLength();
		}
		spotString += text.substring(lastChar);

		// System.out.println(spotString);

		HashSet<ScoredAnnotation> resScored = solveSa2W(spotString);
		HashSet<Annotation> res = new HashSet<>();
		for (Mention m : mentionsList) {
			boolean found = false;
			for (ScoredAnnotation a : resScored)
				if (a.getLength() == m.getLength()
						&& a.getPosition() == m.getPosition()) {
					res.add(new Annotation(a.getPosition(), a.getLength(), a
							.getConcept()));
					found = true;
					break;
				}
			if (!found)
				res.add(new Annotation(m.getPosition(), m.getLength(), -1));
		}
		return res;
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String text)
			throws AnnotationException {
		JSONObject obj = null;
		String getParameters = "";// String.format("lang=%s&method=%s&minCommonness=0.01",
									// "en", method);
		try {
			lastTime = Calendar.getInstance().getTimeInMillis();
			obj = queryJson(getParameters, text, url);
			lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;
		} catch (Exception e) {
			System.out
					.print("Got error while querying AIDA API with GET parameters: "
							+ getParameters + " with text: " + text);
			e.printStackTrace();
			throw new AnnotationException(
					"An error occurred while querying AIDA API. Message: "
							+ e.getMessage());
		}

		if (obj == null)
			return new HashSet<>();

		Vector<Integer> startPositions = new Vector<Integer>();
		Vector<Integer> lengths = new Vector<Integer>();
		Vector<String> titles = new Vector<String>();
		Vector<Float> scores = new Vector<Float>();
		JSONArray jsMentions = (JSONArray) obj.get("mentions");
		for (Object jsMentionObj : jsMentions) {
			JSONObject jsMention = (JSONObject) jsMentionObj;
			if (!jsMention.containsKey("bestEntity"))
				continue;
			//System.out.println(jsMention);
			startPositions.add(((Long) jsMention.get("offset")).intValue());
			lengths.add(((Long) jsMention.get("length")).intValue());
			titles.add(StringEscapeUtils
					.unescapeJava((String) ((JSONObject) jsMention
							.get("bestEntity")).get("name")));
			scores.add(Float.parseFloat((String) ((JSONObject) jsMention
					.get("bestEntity")).get("disambiguationScore")));
		}

		for (String title : titles)
			System.out.println(title);

		HashSet<ScoredAnnotation> res = new HashSet<ScoredAnnotation>();
		try {
			api.prefetchTitles(titles);
			for (int i = 0; i < startPositions.size(); i++){
				res.add(new ScoredAnnotation(startPositions.get(i), lengths
						.get(i), api.getIdByTitle(titles.get(i)),
						(float) scores.get(i)));
			}
		} catch (XPathExpressionException | IOException
				| ParserConfigurationException | SAXException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return res;
	}

	@Override
	public HashSet<Mention> getSpottedMentions(String text) {
		HashSet<Mention> res = new HashSet<Mention>();
		JSONObject obj = null;
		String getParameters = "";
		try {
			obj = queryJson(getParameters, text, url);
		} catch (Exception e) {
			System.out
					.print("Got error while querying AIDA API with GET parameters: "
							+ getParameters + " with text: " + text);
			e.printStackTrace();
			throw new AnnotationException(
					"An error occurred while querying AIDA API. Message: "
							+ e.getMessage());
		}

		JSONArray jsMentions = (JSONArray) obj.get("mentions");
		for (Object jsMentionObj : jsMentions) {
			JSONObject jsMention = (JSONObject) jsMentionObj;
			int pos = ((Long) jsMention.get("offset")).intValue() - 1;
			int len = ((Long) jsMention.get("length")).intValue();
			res.add(new Mention(pos, len));
		}
		return res;
	}

	private JSONObject queryJson(String getParameters, String text, String url)
			throws Exception {
		String postParameters = String.format("text=%s\ntech=%s",
				URLEncoder.encode(text, "UTF-8"), tech);

		URL webApi = new URL(String.format("%s?%s", url, getParameters));
		HttpURLConnection slConnection = (HttpURLConnection) webApi
				.openConnection();
		slConnection.setReadTimeout(0);
		slConnection.setDoOutput(true);
		slConnection.setDoInput(true);
		slConnection.setRequestMethod("POST");
		slConnection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded");
		slConnection.setRequestProperty("charset", "utf-8");
		slConnection.setRequestProperty("Content-Length",
				"" + Integer.toString(postParameters.getBytes().length));
		slConnection.setUseCaches(false);

		DataOutputStream wr = new DataOutputStream(
				slConnection.getOutputStream());
		wr.writeBytes(postParameters);
		wr.flush();
		wr.close();

		java.util.Scanner s = new java.util.Scanner(
				slConnection.getInputStream());
		s.useDelimiter("\\A");
		String resultStr = s.hasNext() ? s.next() : "";
		s.close();
		
		if (resultStr.equals("ERROR: Failed Disambiguating"))
			return null;

		JSONObject obj = (JSONObject) JSONValue.parse(resultStr);
		return obj;
	}
}
