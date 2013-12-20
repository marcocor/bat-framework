/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import it.acubelab.batframework.data.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

public class SpotlightAnnotator implements Sa2WSystem{
	private long lastTime = -1;
	private long calib = -1;
	private DBPediaApi dbpediaApi;
	private WikipediaApiInterface wikiApi;

	public SpotlightAnnotator(DBPediaApi dbpediaApi, WikipediaApiInterface wikiApi){
		this.dbpediaApi = dbpediaApi;
		this.wikiApi = wikiApi;
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
		return "DBPedia Spotlight";
	}

	@Override
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		//dbpedia spotlight cannot handle documents made only of whitespaces...
		int i=0;
		while (i<text.length() && (text.charAt(i)==' ' || text.charAt(i)=='\n')) i++;
		if (i==text.length()) return new HashSet<ScoredAnnotation>();
		
		Pattern dbPediaUri = Pattern.compile("^http://dbpedia.org/resource/(.*)$");
		Set<String> toPrefetch = new HashSet<String>();
		Set<SpotLightAnnotation> res = new HashSet<SpotlightAnnotator.SpotLightAnnotation>();
		try{
			lastTime = Calendar.getInstance().getTimeInMillis();

			URL wikiApi = new URL("http://spotlight.dbpedia.org/rest/annotate");
			String parameters = "confidence=0&support=0&text="+URLEncoder.encode(text, "UTF-8");
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
			Document doc;
			try{
				doc = builder.parse(slConnection.getInputStream());
			} catch (IOException e){
				System.out.print("Got error while querying: "+wikiApi+"?"+parameters);
				throw e;
			}
			lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;

			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression resourceExpr = xpath.compile("//Resource");

			NodeList resources = (NodeList) resourceExpr.evaluate(doc, XPathConstants.NODESET);
			for (int j = 0; j < resources.getLength(); j++) {
				Element currRes = (Element) resources.item(j);
				String uri = currRes.getAttribute("URI");
				int offset = Integer.parseInt(currRes.getAttribute("offset"));
				float score  = Float.parseFloat(currRes.getAttribute("similarityScore"));
				String surface = currRes.getAttribute("surfaceForm");
				Matcher m = dbPediaUri.matcher(uri);
				if (m.matches()){
					String resource = URLDecoder.decode(m.group(1), "UTF-8");
					res.add(new SpotLightAnnotation(offset, surface.length(), resource, score));
					toPrefetch.add(resource);
				}
				else
					throw new AnnotationException(uri +" does not match the pattern.");

			}
		}
		catch (Exception e){
			e.printStackTrace();
			throw new AnnotationException("An error occurred while querying "+this.getName()+" API. Message: " + e.getMessage());
		}

		// query DBPedia API and prefetch wikipedia title
		dbpediaApi.prefetch(toPrefetch);

		//query Wikipedia API and prefetch wikipedia id
		HashSet<String> wikiTitles = new HashSet<String>();
		for (String dbpTitle: toPrefetch)
			wikiTitles.add(dbpediaApi.dbpediaToWikipedia(dbpTitle));
		try {
			wikiApi.prefetchTitles(new Vector<String>(wikiTitles));
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException("Wikipedia API exception: "+e.getMessage());
		}

		HashSet<ScoredAnnotation> result = new HashSet<ScoredAnnotation>();
		for (SpotLightAnnotation a:res){
			String wikipediaTitle = dbpediaApi.dbpediaToWikipedia(a.resource);
			if (wikipediaTitle == null) continue;
			int wikipediaArticle;
			try {
				wikipediaArticle = wikiApi.getIdByTitle(wikipediaTitle);
			} catch (Exception e) {
				e.printStackTrace();
				throw new AnnotationException("Wikipedia API exception: "+e.getMessage());
			}
			if (wikipediaArticle<0) {
				System.out.println("ERROR: wrong binding: "+a.resource+" -> "+wikipediaTitle+" -> "+wikipediaArticle);
				continue;
			}
			result.add(new ScoredAnnotation(a.position, a.length, wikipediaArticle, a.score));
		}
		return result;

	}

	@Override
	public long getLastAnnotationTime() {
		if (calib == -1)
			calib = TimingCalibrator.getOffset(this);
		return lastTime - calib > 0 ? lastTime - calib  : 0;
	}


	private static class SpotLightAnnotation {
		public SpotLightAnnotation(int position, int length, String resource, float score) {
			this.position = position;
			this.length = length;
			this.resource = resource;
			this.score = score;
		}
		public float score;
		public int position, length;
		public String resource;
	}

}
