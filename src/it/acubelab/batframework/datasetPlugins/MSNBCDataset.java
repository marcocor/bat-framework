/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.datasetPlugins;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WDataset;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.CharUtils;
import it.acubelab.batframework.utils.ProblemReduction;
import it.acubelab.batframework.utils.WikipediaApiInterface;

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


public class MSNBCDataset implements A2WDataset{
	private List<String> textList;
	private List<Set<Annotation>> annList;
	private static Pattern wikiUrlPattern = Pattern.compile("http://en.wikipedia.org/wiki/(.*?)\"?");

	/**
	 * This constructor should only be used by inherited classes.
	 */
	public MSNBCDataset(){};

	public MSNBCDataset(String textPath, String annotationsPath, WikipediaApiInterface api) throws IOException, ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException{
		//load the bodies
		HashMap<String, String> filenameToBody= loadBody(textPath, ".+\\.txt");

		//load the annotations
		HashMap<String, Set<Annotation>> filenameToAnnotations= loadTags(annotationsPath, ".+\\.txt", api);

		//check that files are coherent.
		checkConsistency(filenameToBody, filenameToAnnotations);

		//unify the two mappings and generate the lists.
		unifyMaps(filenameToBody, filenameToAnnotations);
	}

	public HashMap<String, String> loadBody(String textPath, String pattern) throws IOException{
		HashMap<String, String> filenameToBody=new HashMap<String, String>();
		File textsDir = new File(textPath);
		File[] textFiles = textsDir.listFiles();
		for (File tf : textFiles)
			if (tf.isFile() && tf.getName().toLowerCase().matches(pattern)){
				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(tf), Charset.forName("UTF-8")));
				String line;
				String body="";
				while ((line = r.readLine())!=null)
					body += line+"\n";
				r.close();
				filenameToBody.put(tf.getName(), body);
			}
		return filenameToBody;
	}

	public HashMap<String, Set<Annotation>> loadTags(String tagsPath, String pattern, WikipediaApiInterface api) throws ParserConfigurationException, SAXException, IOException, AnnotationException, XPathExpressionException {
		HashMap<String, Set<MSNBCAnnotation>> filenameToTags = new HashMap<String, Set<MSNBCAnnotation>>();
		File annDir = new File(tagsPath);
		File[] tagFiles = annDir.listFiles();
		for (File tf: tagFiles)
			if (tf.isFile() && tf.getName().toLowerCase().matches(pattern)){
				HashSet<MSNBCAnnotation> tags = new HashSet<MSNBCAnnotation>();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(tf);
				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("ReferenceInstance");
				for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE){
						Element eElement = (Element) nNode;
						NodeList annData = eElement.getChildNodes();
						int position = -1;
						int length = -1;
						String title = null;
						for (int j = 0; j < annData.getLength(); j++) {
							Node dataNode = annData.item(j);
							if (dataNode.getNodeType() == Node.ELEMENT_NODE){
								Element dataElement = (Element) dataNode;
								if (dataElement.getTagName().equals("Offset"))
									position = CharUtils.parseInt(CharUtils.trim(dataElement.getTextContent()));
								if (dataElement.getTagName().equals("Length"))
									length = CharUtils.parseInt(CharUtils.trim(dataElement.getTextContent()));
								if (dataElement.getTagName().equals("ChosenAnnotation")){
									String concept = URLDecoder.decode(CharUtils.trim(dataElement.getTextContent()).toString().replace('_', ' '),"UTF-8");
									Matcher m = wikiUrlPattern.matcher(concept);
									if (m.matches())
										title = m.group(1);
									else
										System.out.println(this.getName()+" dataset is malformed: URL "+CharUtils.trim(dataElement.getTextContent())+" does not match the pattern. Discarding annotation.");
								}
							}
						}
						if (title != null)
							tags.add(new MSNBCAnnotation(position, length, title));
					}
				}
				filenameToTags.put(tf.getName(), tags);
				
			}
		
		//prefetch all Wikipedia-ids for the titles found
		List<String> titlesToPrefetch = new Vector<String>();
		for (Set<MSNBCAnnotation> s: filenameToTags.values())
			for (MSNBCAnnotation a: s)
				titlesToPrefetch.add(a.title);
		api.prefetchTitles(titlesToPrefetch);
		
		//convert all tags adding the Wikipedia-ID
		HashMap<String, Set<Annotation>> result = new HashMap<String, Set<Annotation>>();
		for (String s: filenameToTags.keySet()){
			Set<Annotation> anns = new HashSet<Annotation>();
			result.put(s, anns);
			for (MSNBCAnnotation a: filenameToTags.get(s)){
				int wid = api.getIdByTitle(a.title); //should be pre-fetched
				if (wid == -1)
					System.out.println(this.getName()+" dataset is malformed: an entity has been tagged with the wikipedia title ["+a.title+"] but this article does not exist. Discarding annotation.");
				else 
					anns.add(new Annotation(a.position, a.length, wid));
			}
		}
			
		return result;
	}


	public void checkConsistency(HashMap<String, String> filenameToBody, HashMap<String, Set<Annotation>> filenameToAnnotations) throws AnnotationException{

		for (String filename : filenameToAnnotations.keySet())
			if (!filenameToBody.containsKey(filename))
				throw new AnnotationException("In "+this.getName()+" dataset, there is an annotation file "+filename+ " that has no corresponding raw text.");
		for (String filename : filenameToBody.keySet())
			if (!filenameToAnnotations.containsKey(filename))
				throw new AnnotationException("In "+this.getName()+" dataset, there is a raw file "+filename+ " that has no corresponding annotations.");
	}

	public void unifyMaps(HashMap<String, String> filenameToBody, HashMap<String, Set<Annotation>> filenameToAnnotations){
		annList = new Vector<Set<Annotation>>();
		textList = new Vector<String>();
		for (String filename : filenameToAnnotations.keySet()){
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
		for (Set<Annotation> s: annList)
			count += s.size();
		return count;
	}

	@Override
	public List<Set<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(this.getA2WGoldStandardList());
	}

	@Override
	public List<Set<Annotation>> getD2WGoldStandardList() {
		return getA2WGoldStandardList();
	}

	@Override
	public List<String> getTextInstanceList() {
		return this.textList;
	}
	
	@Override
	public List<Set<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return "MSNBC";
	}

	@Override
	public List<Set<Annotation>> getA2WGoldStandardList() {
		return annList;
	}
	
	private static class MSNBCAnnotation implements Comparable<MSNBCAnnotation>{
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
