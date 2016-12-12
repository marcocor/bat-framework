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
import it.unipi.di.acube.batframework.utils.WikipediaInterface;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


public class IITBDataset implements A2WDataset{
	private List<String> textList;
	private List<HashSet<Annotation>> annList;
	
	public IITBDataset(String textPath, String annotationsPath, WikipediaInterface api) throws IOException, ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException{
		this(Utils.getFilesAndInputStreams(textPath, ".*"), new FileInputStream(annotationsPath), api);
	}
	
	public IITBDataset(Map<String, InputStream> texts, InputStream annotationsIs, WikipediaInterface api) throws IOException, ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException{
		//load the annotations (and the file name list)
		HashMap<String, HashSet<Annotation>> filenameToAnnotations= loadAnns(annotationsIs, api);

		//load the bodies (from the file names list)
		HashMap<String, String> filenameToBody= loadBody(texts, filenameToAnnotations.keySet());

		//check consistency
		checkConsistency(filenameToBody, filenameToAnnotations);
		
		//unify the two mappings and generate the lists.
		unifyMaps(filenameToBody, filenameToAnnotations);
	}

	private void checkConsistency(HashMap<String, String> filenameToBody, HashMap<String, HashSet<Annotation>> filenameToAnnotations) throws AnnotationException {
		for (String fn: filenameToAnnotations.keySet())
			if (!filenameToBody.containsKey(fn))
				throw new AnnotationException("Document "+fn+" cited in annotation not available!");
	}

	public HashMap<String, String> loadBody(Map<String, InputStream> texts, Set<String> textFiles) throws IOException{
		HashMap<String, String> filenameToBody=new HashMap<String, String>();
		for (String filename : texts.keySet()){
			BufferedReader r = new BufferedReader(new InputStreamReader(texts.get(filename), Charset.forName("UTF-8")));
			String line;
			String body="";
			while ((line = r.readLine())!=null)
				body += line.replace((char)0, ' ')+"\n";
			r.close();
			filenameToBody.put(filename, body);
		}
		return filenameToBody;
	}

	public HashMap<String, HashSet<Annotation>> loadAnns(InputStream annsIs, WikipediaInterface api) throws ParserConfigurationException, SAXException, IOException, AnnotationException, XPathExpressionException {
		HashMap<String, HashSet<IITBAnnotation>> filenameToAnns = new HashMap<String, HashSet<IITBAnnotation>>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(annsIs);
		doc.getDocumentElement().normalize();
		List<String> titlesToPrefetch = new Vector<String>();

		NodeList nList = doc.getElementsByTagName("annotation");
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE){
				Element eElement = (Element) nNode;
				NodeList annData = eElement.getChildNodes();
				int position = -1;
				int length = -1;
				String title = null;
				String docName = null;
				for (int j = 0; j < annData.getLength(); j++) {
					Node dataNode = annData.item(j);
					if (dataNode.getNodeType() == Node.ELEMENT_NODE){
						Element dataElement = (Element) dataNode;
						if (dataElement.getTagName().equals("offset"))
							position = CharUtils.parseInt(CharUtils.trim(dataElement.getTextContent()));
						if (dataElement.getTagName().equals("length"))
							length = CharUtils.parseInt(CharUtils.trim(dataElement.getTextContent()));
						if (dataElement.getTagName().equals("wikiName"))
							title = CharUtils.trim(dataElement.getTextContent()).toString(); //URLDecoder.decode(CharUtils.trim(dataElement.getTextContent()).toString().replace('_', ' '),"UTF-8");
						if (dataElement.getTagName().equals("docName"))
							docName = CharUtils.trim(dataElement.getTextContent()).toString();
					}
				}
				if (title != null && length>0 && position>=0 && docName != null){
					if (!title.equals("")){//not a NA-annotation
						if (!filenameToAnns.containsKey(docName))
							filenameToAnns.put(docName, new HashSet<IITBAnnotation>());
						filenameToAnns.get(docName).add(new IITBAnnotation(position, length, title));
						titlesToPrefetch.add(title);
					}
				}
				else
					System.out.printf("ERROR: Dataset %s has an incomplete annotation: file=%s offset=%d length=%d wikiName=%s", this.getName(), docName, position, length, title);
			}
		}
		//prefetch all Wikipedia-ids for the titles found
		api.prefetchTitles(titlesToPrefetch);
		api.flush();

		//convert all annotations adding the Wikipedia-ID
		HashMap<String, HashSet<Annotation>> result = new HashMap<String, HashSet<Annotation>>();
		for (String s: filenameToAnns.keySet()){
			HashSet<Annotation> anns = new HashSet<Annotation>();
			result.put(s, anns);
			for (IITBAnnotation a: filenameToAnns.get(s)){
				int wid = api.getIdByTitle(a.title); //should be pre-fetched
				if (wid == -1)
					System.out.println(this.getName()+" dataset is malformed: a mention has been annotated with the wikipedia title ["+a.title+"] but this article does not exist. Discarding annotation.");
				else 
					anns.add(new Annotation(a.position, a.length, wid));
			}
		}

		return result;
	}

	public void unifyMaps(HashMap<String, String> filenameToBody, HashMap<String, HashSet<Annotation>> filenameToAnnotations){
		annList = new Vector<HashSet<Annotation>>();
		textList = new Vector<String>();
		for (String filename : filenameToAnnotations.keySet()){
			//if (!filename.equals("yn_08Oct08_file_15")) continue;
			textList.add(filenameToBody.get(filename));
			annList.add(filenameToAnnotations.get(filename));
			//System.out.println(filename+": "+filenameToBody.get(filename).length());
			//yn_08Oct08_file_21
		}
	}

	@Override
	public int getSize() {
		return this.textList.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (HashSet<Annotation> s: annList)
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
		return "IITB";
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return annList;
	}

	private static class IITBAnnotation implements Comparable<IITBAnnotation>{
		public IITBAnnotation(int position, int length, String title) {
			this.position = position;
			this.length = length;
			this.title = title;
		}
		public int position, length;
		public String title;
		@Override
		public int compareTo(IITBAnnotation a) {
			return this.position - a.position;
		}
	}

}
