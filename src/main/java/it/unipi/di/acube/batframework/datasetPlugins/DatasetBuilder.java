package it.unipi.di.acube.batframework.datasetPlugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.codehaus.jettison.json.JSONException;
import org.xml.sax.SAXException;

import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.FreebaseApi;
import it.unipi.di.acube.batframework.utils.Utils;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class DatasetBuilder {
	private static final ClassLoader classLoader = DatasetBuilder.class.getClassLoader();

	public static GERDAQDataset getGerdaqTrainA(){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_trainingA.xml"), WikipediaApiInterface.api(), "trainingA");
	}
	public static GERDAQDataset getGerdaqTrainB(){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_trainingB.xml"), WikipediaApiInterface.api(), "trainingB");
	}
	public static GERDAQDataset getGerdaqDevel(){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_devel.xml"), WikipediaApiInterface.api(), "devel");
	}
	public static GERDAQDataset getGerdaqTest(){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_test.xml"), WikipediaApiInterface.api(), "test");
	}
	public static ERD2014Dataset getERD(FreebaseApi freebaseApi) throws IOException, JSONException{
		return new ERD2014Dataset(classLoader.getResourceAsStream("datasets/erd2014/Trec_beta.query.txt"), classLoader.getResourceAsStream("datasets/erd2014/Trec_beta.annotation.txt"), freebaseApi, WikipediaApiInterface.api());
	}
	public static ACE2004Dataset getACE2004() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, URISyntaxException{
		return new ACE2004Dataset(Utils.getResourceListing(classLoader, "datasets/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts", "^[^\\.]+.*"), Utils.getResourceListing(classLoader, "datasets/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts", "^[^\\.]+.*"), WikipediaApiInterface.api());
	}
	public static AQUAINTDataset getAQUAINT() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, URISyntaxException{
		return new AQUAINTDataset(Utils.getResourceListing(classLoader, "datasets/AQUAINT/RawTexts",  ".+\\.htm"), Utils.getResourceListing(classLoader, "datasets/AQUAINT/Problems", ".+\\.htm"), WikipediaApiInterface.api());
	}
	public static KddDataset getKDDDevel() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new KddDataset(new InputStream[]{classLoader.getResourceAsStream("datasets/kdd/kdd_amt_d_1.txt")}, WikipediaApiInterface.api());
	}
	public static KddDataset getKDDTest() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new KddDataset(new InputStream[]{classLoader.getResourceAsStream("datasets/kdd/kdd_amt_t_1.txt")}, WikipediaApiInterface.api());
	}
	public static MeijDataset getMeij() throws IOException {
		return new MeijDataset(classLoader.getResourceAsStream("datasets/meij/original_tweets.list"), classLoader.getResourceAsStream("datasets/meij/wsdm2012_annotations.txt"), classLoader.getResourceAsStream("datasets/meij/wsdm2012_qrels.txt"));
	}
	public static MSNBCDataset getMSNBC() throws AnnotationException, XPathExpressionException, IOException,
	        ParserConfigurationException, SAXException, URISyntaxException {
		return new MSNBCDataset(Utils.getResourceListing(classLoader, "datasets/MSNBC/RawTextsSimpleChars_utf8", ".+\\.txt"),
		        Utils.getResourceListing(classLoader, "datasets/MSNBC/Problems", ".+\\.txt"), WikipediaApiInterface.api());
	}
	public static A2WDataset getIITB() throws AnnotationException, XPathExpressionException, UnsupportedEncodingException, IOException, ParserConfigurationException, SAXException, URISyntaxException {
		return new IITBDataset(Utils.getResourceListing(classLoader, "datasets/iitb/crawledDocs", ".*"), classLoader.getResourceAsStream("datasets/iitb/CSAW_Annotations.xml"), WikipediaApiInterface.api());
	}
}
