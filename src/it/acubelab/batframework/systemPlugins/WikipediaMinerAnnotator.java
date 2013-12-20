/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import it.acubelab.batframework.data.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

public class WikipediaMinerAnnotator implements Sa2WSystem{
	private long lastTime = -1;
	private long calib = -1;
	private String url;
	
	public WikipediaMinerAnnotator(String configFile) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new FileInputStream(configFile));
		url = getConfigValue("access", "url", doc);
		if (url.equals(""))
			throw new AnnotationException("Configuration file "+configFile+ " has missing value 'url'.");
	}
	
	private String getConfigValue(String setting, String name, Document doc) throws XPathExpressionException{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression userExpr = xpath.compile("wikipediaminer/setting[@name=\""+setting+"\"]/param[@name=\""+name+"\"]/@value");
		return userExpr.evaluate(doc);
	}
	
	@Override
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		Set<ScoredAnnotation> res;
		try{
			res = new HashSet<ScoredAnnotation>();
			lastTime = Calendar.getInstance().getTimeInMillis();
			
			URL wikiApi = new URL(url);
			String parameters = "references=true&repeatMode=all&minProbability=0.0&source="+URLEncoder.encode(text, "UTF-8");
			HttpURLConnection slConnection = (HttpURLConnection) wikiApi.openConnection();
			slConnection.setRequestProperty("accept", "text/xml");
			slConnection.setDoOutput(true);
			slConnection.setDoInput(true);
			slConnection.setRequestMethod("POST"); 
			slConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			slConnection.setRequestProperty("charset", "utf-8");
			slConnection.setRequestProperty("Content-Length", "" + Integer.toString(parameters.getBytes().length));
			slConnection.setUseCaches (false);

			DataOutputStream wr = new DataOutputStream(slConnection.getOutputStream ());
			wr.writeBytes(parameters);
			wr.flush();
			wr.close();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(slConnection.getInputStream());
			
/*			URL wikiApi = new URL(url+"?references=true&repeatMode=all&minProbability=0.0&source="+URLEncoder.encode(text, "UTF-8"));
			URLConnection wikiConnection = wikiApi.openConnection();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(wikiConnection.getInputStream());
*/
			
			lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression idExpr = xpath.compile("//detectedTopic/@id");
			XPathExpression weightExpr = xpath.compile("//detectedTopic/@weight");
			XPathExpression referenceExpr = xpath.compile("//detectedTopic/references");

			NodeList ids = (NodeList) idExpr.evaluate(doc, XPathConstants.NODESET);
			NodeList weights = (NodeList) weightExpr.evaluate(doc, XPathConstants.NODESET);
			NodeList references = (NodeList) referenceExpr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < weights.getLength(); i++) {
				if (weights.item(i).getNodeType() != Node.TEXT_NODE) {
					int id = Integer.parseInt(ids.item(i).getNodeValue());
					float weight = Float.parseFloat(weights.item(i).getNodeValue());
//					System.out.println("ID="+ids.item(i).getNodeValue()+" weight="+weight);
					XPathExpression startExpr = xpath.compile("//detectedTopic[@id="+id+"]/references/reference/@start");
					XPathExpression endExpr = xpath.compile("//detectedTopic[@id="+id+"]/references/reference/@end");
					NodeList starts = (NodeList) startExpr.evaluate(references.item(i), XPathConstants.NODESET);
					NodeList ends = (NodeList) endExpr.evaluate(references.item(i), XPathConstants.NODESET);
					for (int j = 0; j < starts.getLength(); j++) {
						int start = Integer.parseInt(starts.item(j).getNodeValue());
						int end = Integer.parseInt(ends.item(j).getNodeValue());
						int len = end-start;
						res.add(new ScoredAnnotation(start, len, id, weight));
					}
				}

			}
		}
		catch (Exception e){
			e.printStackTrace();
			throw new AnnotationException("An error occurred while querying Wikipedia Miner API. Message: " + e.getMessage());
		}
		return res;

	}
	
	@Override
	public Set<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text), Float.MIN_VALUE);
	}

	@Override
	public Set<Tag> solveC2W(String text)	throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	@Override
	public Set<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(this.solveSa2W(text));
	}
	
	@Override
	public Set<Annotation> solveD2W(String text, Set<Mention> mentions) {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions, Float.MIN_VALUE);
	}

	@Override
	public String getName() {
		return "Wikipedia Miner";
	}

	@Override
	public long getLastAnnotationTime() {
		if (calib == -1)
			calib = TimingCalibrator.getOffset(this);
		return lastTime - calib > 0 ? lastTime - calib  : 0;
	}

}
