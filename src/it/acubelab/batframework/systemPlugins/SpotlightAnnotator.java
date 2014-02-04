/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.*;

import it.acubelab.batframework.data.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

public class SpotlightAnnotator implements Sa2WSystem{
	private long lastTime = -1;
	private long calib = -1;

	private final DisambiguationPolicy disambiguator;
	private DBPediaApi dbpediaApi;
	private WikipediaApiInterface wikiApi;
	private final String host;
	private final int port;

	private static final DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();
	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

	public SpotlightAnnotator(DisambiguationPolicy disambiguator, DBPediaApi dbpediaApi, WikipediaApiInterface wikiApi, String host, int port) {
		this.disambiguator = disambiguator;
		this.dbpediaApi = dbpediaApi;
		this.wikiApi = wikiApi;
		this.host = host;
		this.port = port;
	}
	
	public SpotlightAnnotator(DBPediaApi dbpediaApi, WikipediaApiInterface wikiApi){
		this(DisambiguationPolicy.Default, dbpediaApi, wikiApi, "spotlight.dbpedia.org", 80);
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
		String xmlTextWithSpots = createTextWithMentions(text, mentions);
		Set<ScoredAnnotation> scoredAnnotations = getSpotlightAnnotations(xmlTextWithSpots, Service.DISAMBIGUATE);
		return ProblemReduction.Sa2WToD2W(scoredAnnotations, mentions, Float.MIN_VALUE);
	}

	@Override
	public String getName() {
		return "DBpedia Spotlight (" + disambiguator + ")";
	}

	@Override
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		return getSpotlightAnnotations(text, Service.ANNOTATE);
	}

	/**
	 * Send request to spotlight and parse the response as a set of scored annotations. If disambiguate is used as
	 * service, the endpoint expects a text with spotted mentions in the xml format as produced by the spot service.
	 * 
	 * @param text
	 *            the text to send
	 * @param service
	 *            the endpoint service to use
	 */
	public Set<ScoredAnnotation> getSpotlightAnnotations(String text, Service service) {
		//dbpedia spotlight cannot handle documents made only of whitespaces...
		int i=0;
		while (i<text.length() && (text.charAt(i)==' ' || text.charAt(i)=='\n')) i++;
		if (i==text.length()) return new HashSet<ScoredAnnotation>();
		
		Pattern dbPediaUri = Pattern.compile("^http://dbpedia.org/resource/(.*)$");
		Set<String> toPrefetch = new HashSet<String>();
		Set<SpotLightAnnotation> res = new HashSet<SpotlightAnnotator.SpotLightAnnotation>();
		try{
			lastTime = Calendar.getInstance().getTimeInMillis();

			URL wikiApi = new URL("http://" + host + ":" + port + "/rest/" + service);
			String parameters = "disambiguator=" + disambiguator + "&confidence=0&support=0&text="+URLEncoder.encode(text, "UTF-8");
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

	/**
	 * Create a text that includes the mentions which can be used for the D2W task. Sample xml output for Spotlight:
	 * 
	 * <pre>
	 * {@code
	 * 	<annotation text="     Search U.S. coalition: Forces have killed 43 militants in Afghanistan Posted | Comment | Recommend | | | KABUL, Afghanistan (AP) ">
	 *   <surfaceForm name="Afghanistan" offset="63"/>
	 *   <surfaceForm name="Afghanistan" offset="117"/>
	 *   <surfaceForm name="AP" offset="130"/>
	 * </annotation>
	 * }
	 * </pre>
	 */
	private static String createTextWithMentions(String text, Set<Mention> mentions) {
		try {
			DocumentBuilder docBuilder = DOC_FACTORY.newDocumentBuilder();
			// annotation root element
			Document doc = docBuilder.newDocument();
			Element annotation = doc.createElement("annotation");
			doc.appendChild(annotation);

			// set text attribute to annotation element
			Attr textAttr = doc.createAttribute("text");
			textAttr.setValue(text);
			annotation.setAttributeNode(textAttr);

			for (Mention m : mentions) {
				String name = text.substring(m.getPosition(), m.getPosition() + m.getLength());
				Attr nameAttr = doc.createAttribute("name");
				nameAttr.setValue(name);

				Attr offsetAttr = doc.createAttribute("offset");
				offsetAttr.setValue(Integer.toString(m.getPosition()));

				Element surfaceForm = doc.createElement("surfaceForm");
				surfaceForm.setAttributeNode(nameAttr);
				surfaceForm.setAttributeNode(offsetAttr);
				annotation.appendChild(surfaceForm);
			}

			// write the content into an xml string
			Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			DOMSource source = new DOMSource(doc);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			transformer.transform(source, new StreamResult(out));

			String xml = out.toString("utf-8");
			return xml;
		} catch (UnsupportedEncodingException | TransformerException | ParserConfigurationException e) {
			throw new AnnotationException(e.getMessage());
		}
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

	private enum Service {
		ANNOTATE("annotate"), DISAMBIGUATE("disambiguate");

		private final String name;

		Service(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public enum DisambiguationPolicy {
		Document, Occurrences, CuttingEdge, Default, GraphBased;

		/**
		 * @throws IllegalArgumentException
		 *             if there are no or invalid policies as argument.
		 */
		public static List<DisambiguationPolicy> parsePoliciesFromArgs(String[] args) {
			List<DisambiguationPolicy> policies = new ArrayList<>();
			for (String arg : args) {
				try {
					policies.add(DisambiguationPolicy.valueOf(arg));
				} catch (IllegalArgumentException e) {
				}
			}
			if (policies.isEmpty())
				throw new IllegalArgumentException(
						"Provide one or more valid disambiguation policies as argument. Valid policies: "
								+ DisambiguationPolicy.values());
			System.out.println("Parsed the following disambiguators: " + policies);
			return policies;
		}
	}
}
