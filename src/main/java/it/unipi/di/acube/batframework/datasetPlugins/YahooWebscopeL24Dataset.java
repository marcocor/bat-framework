package it.unipi.di.acube.batframework.datasetPlugins;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class YahooWebscopeL24Dataset implements A2WDataset {
	List<String> queries = new Vector<String>();
	List<HashSet<Annotation>> annotations = new Vector<HashSet<Annotation>>();

	public YahooWebscopeL24Dataset(String filename) throws ParserConfigurationException, SAXException, IOException,
	        XPathExpressionException {
		FileInputStream fis = new FileInputStream(new File(filename));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(fis);
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression queryExpr = xpath.compile("//query[@cannot-judge=\"false\"]");
		NodeList queriesNodes = (NodeList) queryExpr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < queriesNodes.getLength(); ++i) {
			String queryI = "";
			HashSet<Annotation> annI = new HashSet<Annotation>();
			NodeList queryNodes = queriesNodes.item(i).getChildNodes();
			for (int j = 0; j < queryNodes.getLength(); ++j) {
				Node nodeJ = queryNodes.item(j);
				nodeJ.normalize();
				if (nodeJ.getNodeType() == 3)
					continue;
				if (nodeJ.getNodeType() != 1) {
					throw new RuntimeException("Node should be an element" + nodeJ.toString());
				}
				if (nodeJ.getNodeName().equals("text")) {
					queryI = nodeJ.getTextContent();
					continue;
				}
				if (nodeJ.getNodeName().equals("annotation")) {
					NodeList annotationNodes = nodeJ.getChildNodes();
					String span = "";
					int wid = -1;
					for (int h = 0; h < annotationNodes.getLength(); ++h) {
						if (annotationNodes.item(h).getNodeName().equals("span")) {
							span = annotationNodes.item(h).getTextContent().replace((CharSequence) "/", (CharSequence) "");
							continue;
						}
						if (!annotationNodes.item(h).getNodeName().equals("target"))
							continue;
						wid = Integer.parseInt(annotationNodes.item(h).getAttributes().getNamedItem("wiki-id").getNodeValue());
					}
					if (span.isEmpty() || wid == -1)
						continue;
					int position = queryI.toLowerCase().indexOf(span.toLowerCase());
					int length = span.length();
					if (position >= 0) {
						annI.add(new Annotation(position, length, wid));
						continue;
					}
					if (queryI.toLowerCase().replaceAll("\"", "").indexOf(span.toLowerCase()) != -1) {
						String[] tokens = queryI.toLowerCase().replaceAll("\\W", " ").replaceAll("^ +", "").replaceAll(" +$", "")
						        .replaceAll(" +", " ").split(" ");
						String firstWord = tokens[0];
						String lastWord = tokens[tokens.length - 1];
						position = queryI.toLowerCase().indexOf(firstWord);
						length = queryI.toLowerCase().indexOf(lastWord) + lastWord.length() - position;
						annI.add(new Annotation(position, span.length(), wid));
						continue;
					}
					System.err.printf("mention [%s] is not a substring of [%s], skipping.%n", span, queryI);
					continue;
				}
				throw new RuntimeException("Unrecognized node:" + nodeJ);
			}
			this.queries.add(queryI);
			this.annotations.add(annI);
		}
	}

	public int getTagsCount() {
		int c = 0;
		for (HashSet<Annotation> s : this.annotations) {
			c += s.size();
		}
		return c;
	}

	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(this.annotations);
	}

	public int getSize() {
		return this.queries.size();
	}

	public String getName() {
		return "Yahoo Webscope L24";
	}

	public List<String> getTextInstanceList() {
		return this.queries;
	}

	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(this.annotations);
	}

	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return this.annotations;
	}

	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return this.annotations;
	}
}