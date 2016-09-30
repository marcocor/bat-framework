/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.datasetPlugins;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.CharUtils;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.Utils;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class MSNBCDataset implements A2WDataset {
	private List<String> textList;
	private List<HashSet<Annotation>> annList;
	private static Pattern wikiUrlPattern = Pattern.compile("http://en.wikipedia.org/wiki/(.*?)\"?");

	/**
	 * This constructor should only be used by inherited classes.
	 */
	protected MSNBCDataset() {
	};

	public MSNBCDataset(String textPath, String annotationsPath, WikipediaApiInterface api) throws IOException,
	        ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException {
		this(Utils.getFilesAndInputStreams(textPath, ".+\\.txt"), Utils.getFilesAndInputStreams(annotationsPath, ".+\\.txt"), api);
	}

	public MSNBCDataset(Map<String, InputStream> bodyFilenameToInputstream,
	        Map<String, InputStream> anchorsFilenameToInputstream, WikipediaApiInterface api) throws IOException,
	        AnnotationException, XPathExpressionException, ParserConfigurationException, SAXException {
		// load the bodies
		HashMap<String, String> filenameToBody = loadBody(bodyFilenameToInputstream);

		// load the annotations
		HashMap<String, HashSet<Annotation>> filenameToAnnotations = loadTags(anchorsFilenameToInputstream, api);

		// check that files are coherent.
		checkConsistency(filenameToBody, filenameToAnnotations);

		// unify the two mappings and generate the lists.
		unifyMaps(filenameToBody, filenameToAnnotations);
	}

	protected HashMap<String, HashSet<Annotation>> loadTags(Map<String, InputStream> anchorsFilenameToInputstream,
	        WikipediaApiInterface api) throws ParserConfigurationException, SAXException, IOException, AnnotationException,
	        XPathExpressionException {
		HashMap<String, HashSet<MSNBCAnnotation>> filenameToTags = new HashMap<String, HashSet<MSNBCAnnotation>>();
		for (String tf : anchorsFilenameToInputstream.keySet()) {
			HashSet<MSNBCAnnotation> tags = new HashSet<MSNBCAnnotation>();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(anchorsFilenameToInputstream.get(tf));
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("ReferenceInstance");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					NodeList annData = eElement.getChildNodes();
					int position = -1;
					int length = -1;
					String title = null;
					for (int j = 0; j < annData.getLength(); j++) {
						Node dataNode = annData.item(j);
						if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
							Element dataElement = (Element) dataNode;
							if (dataElement.getTagName().equals("Offset"))
								position = CharUtils.parseInt(CharUtils.trim(dataElement.getTextContent()));
							if (dataElement.getTagName().equals("Length"))
								length = CharUtils.parseInt(CharUtils.trim(dataElement.getTextContent()));
							if (dataElement.getTagName().equals("ChosenAnnotation")) {
								String concept = URLDecoder.decode(CharUtils.trim(dataElement.getTextContent()).toString()
								        .replace('_', ' '), "UTF-8");
								Matcher m = wikiUrlPattern.matcher(concept);
								if (m.matches())
									title = m.group(1);
								else
									System.out.println(this.getName() + " dataset is malformed: URL "
									        + CharUtils.trim(dataElement.getTextContent())
									        + " does not match the pattern. Discarding annotation.");
							}
						}
					}
					if (title != null)
						tags.add(new MSNBCAnnotation(position, length, title));
				}
			}
			filenameToTags.put(tf, tags);
		}

		// prefetch all Wikipedia-ids for the titles found
		List<String> titlesToPrefetch = new Vector<String>();
		for (HashSet<MSNBCAnnotation> s : filenameToTags.values())
			for (MSNBCAnnotation a : s)
				titlesToPrefetch.add(a.title);
		api.prefetchTitles(titlesToPrefetch);

		// convert all tags adding the Wikipedia-ID
		HashMap<String, HashSet<Annotation>> result = new HashMap<String, HashSet<Annotation>>();
		for (String s : filenameToTags.keySet()) {
			HashSet<Annotation> anns = new HashSet<Annotation>();
			for (MSNBCAnnotation a : filenameToTags.get(s)) {
				int wid = api.getIdByTitle(a.title); // should be pre-fetched
				if (wid == -1)
					System.out.println(this.getName()
					        + " dataset is malformed: an entity has been tagged with the wikipedia title [" + a.title
					        + "] but this article does not exist. Discarding annotation.");
				else
					anns.add(new Annotation(a.position, a.length, wid));
			}
			HashSet<Annotation> annsNonOverlap = Annotation.deleteOverlappingAnnotations(anns);
			result.put(s, annsNonOverlap);

		}

		return result;
	}

	protected HashMap<String, String> loadBody(Map<String, InputStream> bodyFilenameToInputstream) throws IOException {
		HashMap<String, String> filenameToBody = new HashMap<String, String>();
		for (String tf : bodyFilenameToInputstream.keySet()) {
			BufferedReader r = new BufferedReader(new InputStreamReader(bodyFilenameToInputstream.get(tf), Charset.forName("UTF-8")));
			String line;
			String body = "";
			while ((line = r.readLine()) != null)
				body += line + "\n";
			r.close();
			filenameToBody.put(tf, body);
		}
		return filenameToBody;
	}

	protected void checkConsistency(HashMap<String, String> filenameToBody,
	        HashMap<String, HashSet<Annotation>> filenameToAnnotations) throws AnnotationException {

		for (String filename : filenameToAnnotations.keySet())
			if (!filenameToBody.containsKey(filename))
				throw new AnnotationException("In " + this.getName() + " dataset, there is an annotation file " + filename
				        + " that has no corresponding raw text.");
		for (String filename : filenameToBody.keySet())
			if (!filenameToAnnotations.containsKey(filename))
				throw new AnnotationException("In " + this.getName() + " dataset, there is a raw file " + filename
				        + " that has no corresponding annotations.");
	}

	protected void unifyMaps(HashMap<String, String> filenameToBody, HashMap<String, HashSet<Annotation>> filenameToAnnotations) {
		annList = new Vector<HashSet<Annotation>>();
		textList = new Vector<String>();
		for (String filename : filenameToAnnotations.keySet()) {
			textList.add(filenameToBody.get(filename));
			annList.add(filenameToAnnotations.get(filename));
		}
	}

	@Override
	public int getSize() {
		return this.textList.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (HashSet<Annotation> s : annList)
			count += s.size();
		return count;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(this.getA2WGoldStandardList());
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return getA2WGoldStandardList();
	}

	@Override
	public List<String> getTextInstanceList() {
		return this.textList;
	}

	@Override
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return "MSNBC";
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return annList;
	}

	private static class MSNBCAnnotation implements Comparable<MSNBCAnnotation> {
		public MSNBCAnnotation(int position, int length, String title) {
			this.position = position;
			this.length = length;
			this.title = title;
		}

		public int position, length;
		public String title;

		@Override
		public int compareTo(MSNBCAnnotation a) {
			return this.position - a.position;
		}
	}

}
