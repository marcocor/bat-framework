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

package it.unipi.di.acube.batframework.datasetPlugins;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class GERDAQDataset implements A2WDataset {
	private List<String> queries = new Vector<String>();
	private List<HashSet<Tag>> tags = new Vector<HashSet<Tag>>();
	private List<HashSet<Annotation>> annotations = new Vector<HashSet<Annotation>>();

	public GERDAQDataset(String xmlFile, WikipediaApiInterface api) {
		File fXmlFile = new File(xmlFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		doc.getDocumentElement().normalize();

		List<HashMap<Mention, Vector<String>>> queryMenToTitles = new Vector<HashMap<Mention, Vector<String>>>();
		NodeList nList = doc.getElementsByTagName("instance");
		for (int i = 0; i < nList.getLength(); i++) {
			HashMap<Mention, Vector<String>> mentionToTitles = new HashMap<>();
			String query = "";
			Node instanceNode = nList.item(i);
			Element eElement = (Element) instanceNode;
			NodeList instElemList = eElement.getChildNodes();
			for (int j = 0; j < instElemList.getLength(); j++) {
				Node instElemNode = instElemList.item(j);
				if (instElemNode.getNodeType() == Node.ELEMENT_NODE) {
					if (!instElemNode.getNodeName().equals("annotation"))
						throw new RuntimeException(
								"Found internal node that is not an annotation.");
					int pos = query.length();
					query += instElemNode.getTextContent();
					int len = query.length() - pos;
					Mention men = new Mention(pos, len);
					mentionToTitles.put(men, new Vector<String>());
					NamedNodeMap attrs = instElemNode.getAttributes();
					int h = 0;
					Node n = null;
					while ((n = attrs.getNamedItem(String.format(
							"rank_%d_title", h))) != null) {
						mentionToTitles.get(men).add(n.getTextContent());
						h++;
					}
				} else if (instElemNode.getNodeType() == Node.TEXT_NODE)
					query += instElemNode.getTextContent();

			}
			queries.add(query);
			queryMenToTitles.add(mentionToTitles);
		}

		List<String> titlesToPrefetch = new Vector<String>();
		for (HashMap<Mention, Vector<String>> setS : queryMenToTitles)
			for (Vector<String> titles : setS.values())
				titlesToPrefetch.addAll(titles);
		try {
			api.prefetchTitles(titlesToPrefetch);
		} catch (XPathExpressionException | IOException
				| ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
		try {
			for (int i = 0; i < queryMenToTitles.size(); i++) {
				HashSet<Tag> qTags = new HashSet<Tag>();
				HashSet<Annotation> qAnns = new HashSet<Annotation>();
				HashMap<Mention, Vector<String>> menToTitles = queryMenToTitles
						.get(i);

				for (Mention m : menToTitles.keySet()) {
					String title = menToTitles.get(m).get(0);
					int id = api.getIdByTitle(title);
					if (id == -1)
						System.err.println("Error in dataset " + this.getName()
								+ ": Could not find wikipedia title: " + title);
					else {
						qAnns.add(new Annotation(m.getPosition(),
								m.getLength(), id));
					}
				}

				for (Vector<String> menTitles : queryMenToTitles.get(i)
						.values()) {
					for (String title : menTitles) {

						int id = api.getIdByTitle(title);
						if (id == -1)
							System.err.println("Error in dataset "
									+ this.getName()
									+ ": Could not find wikipedia title: "
									+ title);
						else
							qTags.add(new Tag(id));

					}
				}
				annotations.add(qAnns);
				tags.add(qTags);
			}
		} catch (DOMException | IOException e) {
			throw new RuntimeException(e);
		}

		if (queries.size() != tags.size() || tags.size() != annotations.size())
			throw new RuntimeException("Parsing error");
	}

	@Override
	public int getSize() {
		return queries.size();
	}

	@Override
	public String getName() {
		return "GERDAQ";
	}

	@Override
	public List<String> getTextInstanceList() {
		return queries;
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (HashSet<Tag> tagSet : tags)
			count += tagSet.size();
		return count;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(annotations);
	}

	@Override
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(this
				.getA2WGoldStandardList());
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return annotations;
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return annotations;
	}

}
