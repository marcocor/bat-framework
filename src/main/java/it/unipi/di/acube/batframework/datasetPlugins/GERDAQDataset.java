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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaInterface;

public class GERDAQDataset implements A2WDataset {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private List<String> queries = new Vector<String>();
	private List<HashSet<Tag>> tags = new Vector<HashSet<Tag>>();
	private List<HashSet<Annotation>> annotations = new Vector<HashSet<Annotation>>();
	private String name = null;

	public GERDAQDataset(String xmlFile, WikipediaInterface api, String nameSuffix) throws FileNotFoundException {
		this(new FileInputStream(new File(xmlFile)), api, nameSuffix);
	}

	public GERDAQDataset(InputStream stream, WikipediaInterface api, String nameSuffix) {
		this.name  = "GERDAQ-" + nameSuffix;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(stream);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		doc.getDocumentElement().normalize();

		List<HashMap<Mention, Vector<String>>> queryMenToTitles = new Vector<>();
		List<HashMap<Mention, Vector<Integer>>> queryMenToWids = new Vector<>();
		List<String> titlesToPrefetch = new Vector<>();
		List<Integer> widsToPrefetch = new Vector<>();
		NodeList nList = doc.getElementsByTagName("instance");
		for (int i = 0; i < nList.getLength(); i++) {
			HashMap<Mention, Vector<String>> mentionToTitles = new HashMap<>();
			HashMap<Mention, Vector<Integer>> mentionToWids = new HashMap<>();
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
					mentionToWids.put(men, new Vector<Integer>());
					NamedNodeMap attrs = instElemNode.getAttributes();
					int h = 0;
					Node n;
					while ((n = attrs.getNamedItem(String.format(
							"rank_%d_title", h))) != null) {
						String title = n.getTextContent();
						titlesToPrefetch.add(title);
						mentionToTitles.get(men).add(title);
						h++;
					}
					h = 0;
					while ((n = attrs.getNamedItem(String.format(
							"rank_%d_id", h))) != null) {
						int wid = Integer.parseInt(n.getTextContent());
						widsToPrefetch.add(wid);
						mentionToWids.get(men).add(wid);
						h++;
					}
				} else if (instElemNode.getNodeType() == Node.TEXT_NODE)
					query += instElemNode.getTextContent();

			}
			queries.add(query);
			queryMenToTitles.add(mentionToTitles);
			queryMenToWids.add(mentionToWids);
		}

		try {
			api.prefetchTitles(titlesToPrefetch);
			api.prefetchWids(widsToPrefetch);
		} catch (XPathExpressionException | IOException
				| ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
		try {
			for (int i = 0; i < queryMenToTitles.size(); i++) {
				HashSet<Tag> qTags = new HashSet<Tag>();
				HashSet<Annotation> qAnns = new HashSet<Annotation>();
				HashMap<Mention, Vector<String>> menToTitles = queryMenToTitles.get(i);
				HashMap<Mention, Vector<Integer>> menToWids = queryMenToWids.get(i);

				for (Mention m : menToTitles.keySet()) {
					int wid = menToWids.get(m).get(0);
					String title = menToTitles.get(m).get(0);
					String resolvedTitle = api.getTitlebyId(wid);
					int resolvedId = api.getIdByTitle(title);
					if (resolvedId != -1)
						resolvedId = api.dereference(resolvedId);


					if (resolvedTitle != null) {
						if (api.isRedirect(wid)) {
							LOG.warn("In dataset {}: Wikipedia ID {} is a redirect to {}.", this.getName(), wid, api.dereference(wid));
							wid = api.dereference(wid);
						}
						qAnns.add(new Annotation(m.getPosition(), m.getLength(), wid));
						if (!resolvedTitle.equals(title))
							LOG.warn("In dataset {}: The title associated with Wikipedia ID {} is not {} anymore, now it is {}.", this.getName(), wid, title, resolvedTitle);
					}
					else if (resolvedTitle == null && resolvedId != -1){
						LOG.warn("In dataset {}: Wikipedia ID {} does not exist anymore. Falling back to resolving title {}, that lead to Wikipedia ID {}", this.getName(), wid, title, resolvedId);
						qAnns.add(new Annotation(m.getPosition(), m.getLength(), resolvedId));
					}
					else {
						LOG.error("In dataset {}: Wikipedia ID {} does not exist anymore and nor does title {}. Discarding annotation.",  this.getName(), wid, title);
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
		return name;
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
