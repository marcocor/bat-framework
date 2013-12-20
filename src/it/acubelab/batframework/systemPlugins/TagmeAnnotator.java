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

public class TagmeAnnotator implements Sa2WSystem{
	private long lastTime = -1;
	private String key;
	private String url;

	
	
	public TagmeAnnotator(String configFile) throws ParserConfigurationException, FileNotFoundException, SAXException, IOException, XPathExpressionException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new FileInputStream(configFile));
		url = getConfigValue("access", "url", doc);
		key = getConfigValue("access", "key", doc);
		if (url.equals("") || key.equals(""))
			throw new AnnotationException("Configuration file "+configFile+ " has missing value 'url' or 'key'.");
		if (key.equals("KEY"))
			throw new AnnotationException("Configuration file "+configFile+ " has dummy default key value 'KEY'. Please replace with an actual key.");
	}
	
	private String getConfigValue(String setting, String name, Document doc) throws XPathExpressionException{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression userExpr = xpath.compile("tagme/setting[@name=\""+setting+"\"]/param[@name=\""+name+"\"]/@value");
		return userExpr.evaluate(doc);
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
		return "TagMe 2";
	}

	@Override
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		//System.out.println(text.length()+ " "+text.substring(0, Math.min(text.length(), 15)));
		//TODO: workaround for a bug in tagme. should be deleted afterwards.
		String newText = "";
		for (int i=0 ;i<text.length(); i++)
			newText += (text.charAt(i) > 127? ' ':text.charAt(i));
		text = newText;
		
		
		//avoid crashes for empty documents
		int j=0;
		while (j<text.length() && text.charAt(j) == ' ') j++;
		if (j == text.length()) return new HashSet<ScoredAnnotation>();

		Set<ScoredAnnotation> res;
		String parameters = null;
		try{
			res = new HashSet<ScoredAnnotation>();

			URL wikiApi = new URL(url);
			parameters = "key="+this.key+"&lang=en&text="+URLEncoder.encode(text, "UTF-8");
			/*System.out.println(parameters);
			System.out.println(text);*/
			HttpURLConnection slConnection = (HttpURLConnection) wikiApi.openConnection();
			slConnection.setRequestProperty("accept", "text/xml");
			slConnection.setDoOutput(true);
			slConnection.setDoInput(true);
			slConnection.setRequestMethod("POST"); 
			slConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			slConnection.setRequestProperty("charset", "utf-8");
			slConnection.setRequestProperty("Content-Length", Integer.toString(parameters.getBytes().length));
			slConnection.setUseCaches (false);

			DataOutputStream wr = new DataOutputStream(slConnection.getOutputStream ());
			wr.writeBytes(parameters);
			wr.flush();
			wr.close();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(slConnection.getInputStream());

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression errorExpr = xpath.compile("//tagme/error");
			NodeList errs = (NodeList) errorExpr.evaluate(doc, XPathConstants.NODESET);
			if (errs.getLength() !=0)
				throw new AnnotationException(this.getName()+" returned error: "+errs.item(0).getTextContent());

			XPathExpression timeExpr = xpath.compile("//tagme/@time");
			lastTime = Integer.parseInt(timeExpr.evaluate(doc));

			XPathExpression annExpr = xpath.compile("//annotations/annotation");
			XPathExpression posExpr = xpath.compile("spot/@pos");
			XPathExpression lenExpr = xpath.compile("spot/@len");
			XPathExpression widExpr = xpath.compile("id/text()");
			XPathExpression rhoExpr = xpath.compile("rho/text()");

			NodeList anns = (NodeList) annExpr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < anns.getLength(); i++) {
				if (anns.item(i).getNodeType() != Node.TEXT_NODE) {
					int pos = Integer.parseInt((String) posExpr.evaluate(anns.item(i), XPathConstants.STRING));
					int len = Integer.parseInt((String) lenExpr.evaluate(anns.item(i), XPathConstants.STRING));
					int wid = Integer.parseInt((String) widExpr.evaluate(anns.item(i), XPathConstants.STRING));
					float rho = Float.parseFloat((String) rhoExpr.evaluate(anns.item(i), XPathConstants.STRING));
					res.add(new ScoredAnnotation(pos, len, wid, rho));
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
			throw new AnnotationException("An error occurred while querying TagMe API. Message: " + e.getMessage() + " Parameters:" + parameters);
		}
		return res;

	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

}
