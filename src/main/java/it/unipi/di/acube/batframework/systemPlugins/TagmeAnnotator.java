/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.systemPlugins;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.problems.*;
import it.unipi.di.acube.batframework.utils.*;

public class TagmeAnnotator implements Sa2WSystem {
	private long lastTime = -1;
	private String key;
	private String url;
	private float epsilon = -1;
	private float minComm = -1;
	private float minLink = -1;

	public TagmeAnnotator(String configFile, float epsilon,
			float minCommonness, float minLink)
			throws ParserConfigurationException, FileNotFoundException,
			SAXException, IOException, XPathExpressionException {
		this(configFile);
		this.epsilon = epsilon;
		this.minComm = minCommonness;
		this.minLink = minLink;
	}
	public TagmeAnnotator(String url, String key, float epsilon,
			float minCommonness, float minLink)
			throws ParserConfigurationException, FileNotFoundException,
			SAXException, IOException, XPathExpressionException {
		this.url = url;
		this.key = key;
		this.epsilon = epsilon;
		this.minComm = minCommonness;
		this.minLink = minLink;
	}
	
	public TagmeAnnotator(String url, String key) {
		this.url = url;
		this.key = key;
	}

	public TagmeAnnotator(String configFile)
			throws ParserConfigurationException, FileNotFoundException,
			SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new FileInputStream(configFile));
		url = getConfigValue("access", "url", doc);
		key = getConfigValue("access", "key", doc);
		if (url.equals("") || key.equals(""))
			throw new AnnotationException("Configuration file " + configFile
					+ " has missing value 'url' or 'key'.");
		if (key.equals("KEY"))
			throw new AnnotationException(
					"Configuration file "
							+ configFile
							+ " has dummy default key value 'KEY'. Please replace with an actual key.");
	}

	private String getConfigValue(String setting, String name, Document doc)
			throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression userExpr = xpath.compile("tagme/setting[@name=\""
				+ setting + "\"]/param[@name=\"" + name + "\"]/@value");
		return userExpr.evaluate(doc);
	}

	@Override
	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text), Float.MIN_VALUE);
	}

	@Override
	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(this.solveSa2W(text));
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions,
				Float.MIN_VALUE);
	}

	@Override
	public String getName() {
		return "TagMe 2";
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String text)
			throws AnnotationException {
		// System.out.println(text.length()+ " "+text.substring(0,
		// Math.min(text.length(), 15)));
		// TODO: workaround for a bug in tagme. should be deleted afterwards.
		String newText = "";
		for (int i = 0; i < text.length(); i++)
			newText += (text.charAt(i) > 127 ? ' ' : text.charAt(i));
		text = newText;

		// avoid crashes for empty documents
		int j = 0;
		while (j < text.length() && text.charAt(j) == ' ')
			j++;
		if (j == text.length())
			return new HashSet<ScoredAnnotation>();

		HashSet<ScoredAnnotation> res;
		String params = null;
		try {
			res = new HashSet<ScoredAnnotation>();

			params = "key=" + this.key;
			params += "&lang=en";
			if (epsilon >= 0)
				params += "&epsilon=" + epsilon;
			if (minComm >= 0)
				params += "&min_comm=" + minComm;
			if (minLink >= 0)
				params += "&min_lp=" + minLink;
			params += "&text=" + URLEncoder.encode(text, "UTF-8");
			URL wikiApi = new URL(url);

			HttpURLConnection slConnection = (HttpURLConnection) wikiApi
					.openConnection();
			slConnection.setRequestProperty("accept", "text/xml");
			slConnection.setDoOutput(true);
			slConnection.setDoInput(true);
			slConnection.setRequestMethod("POST");
			slConnection.setRequestProperty("charset", "utf-8");
			slConnection.setRequestProperty("Content-Length",
					Integer.toString(params.getBytes().length));
			slConnection.setUseCaches(false);
			slConnection.setReadTimeout(0);

			DataOutputStream wr = new DataOutputStream(
					slConnection.getOutputStream());
			wr.writeBytes(params);
			wr.flush();
			wr.close();

			Scanner s = new Scanner(slConnection.getInputStream());
			Scanner s2 = s.useDelimiter("\\A");
			String resultStr = s2.hasNext() ? s2.next() : "";
			s.close();

			JSONObject obj = (JSONObject) JSONValue.parse(resultStr);
			lastTime = (Long) obj.get("time");

			JSONArray jsAnnotations = (JSONArray) obj.get("annotations");
			for (Object js_ann_obj : jsAnnotations) {
				JSONObject js_ann = (JSONObject) js_ann_obj;
				System.out.println(js_ann);
				int start = ((Long) js_ann.get("start")).intValue();
				int end = ((Long) js_ann.get("end")).intValue();
				int id = ((Long) js_ann.get("id")).intValue();
				float rho = Float.parseFloat((String) js_ann.get("rho"));
				System.out.println(text.substring(start, end) + "->" + id
						+ " (" + rho + ")");
				res.add(new ScoredAnnotation(start, end - start, id,
						(float) rho));
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException(
					"An error occurred while querying TagMe API. Message: "
							+ e.getMessage() + " Parameters:" + params);
		}
		return res;

	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

}
