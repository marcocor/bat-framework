package it.unipi.di.acube.batframework.datasetPlugins;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.codehaus.jettison.json.JSONException;
import org.xml.sax.SAXException;

import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.FreebaseApi;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class DatasetBuilder {
	private static final ClassLoader classLoader = DatasetBuilder.class.getClassLoader();

	public static GERDAQDataset getGerdaqTrainA(){
		return new GERDAQDataset(classLoader.getResource("datasets/gerdaq_1.0/gerdaq_trainingA.xml").getFile(), WikipediaApiInterface.api(), "trainingA");
	}
	public static GERDAQDataset getGerdaqTrainB(){
		return new GERDAQDataset(classLoader.getResource("datasets/gerdaq_1.0/gerdaq_trainingB.xml").getFile(), WikipediaApiInterface.api(), "trainingB");
	}
	public static GERDAQDataset getGerdaqDevel(){
		return new GERDAQDataset(classLoader.getResource("datasets/gerdaq_1.0/gerdaq_devel.xml").getFile(), WikipediaApiInterface.api(), "devel");
	}
	public static GERDAQDataset getGerdaqTest(){
		return new GERDAQDataset(classLoader.getResource("datasets/gerdaq_1.0/gerdaq_test.xml").getFile(), WikipediaApiInterface.api(), "test");
	}
	public static ERD2014Dataset getERD(FreebaseApi freebaseApi) throws IOException, JSONException{
		return new ERD2014Dataset(classLoader.getResource("datasets/erd2014/Trec_beta.query.txt").getFile(), classLoader.getResource("datasets/erd2014/Trec_beta.annotation.txt").getFile(), freebaseApi, WikipediaApiInterface.api());
	}
	public static ACE2004Dataset getACE2004() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException{
		return new ACE2004Dataset(classLoader.getResource("datasets/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts").getFile(), classLoader.getResource("datasets/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts").getFile(), WikipediaApiInterface.api());
	}
	public static AQUAINTDataset getAQUAINT() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException{
		return new AQUAINTDataset(classLoader.getResource("datasets/AQUAINT/RawTexts").getFile(), classLoader.getResource("datasets/AQUAINT/Problems").getFile(), WikipediaApiInterface.api());
	}
	public static KddDataset getKDDDevel() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new KddDataset(new String[]{classLoader.getResource("datasets/kdd/kdd_amt_d_1.txt").getFile()}, WikipediaApiInterface.api());
	}
	public static KddDataset getKDDTest() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new KddDataset(new String[]{classLoader.getResource("datasets/kdd/kdd_amt_t_1.txt").getFile()}, WikipediaApiInterface.api());
	}
	public static MeijDataset getMeij() {
		return new MeijDataset(classLoader.getResource("datasets/meij/original_tweets.list").getFile(), classLoader.getResource("datasets/meij/wsdm2012_annotations.txt").getFile(), classLoader.getResource("datasets/meij/wsdm2012_qrels.txt").getFile());
	}
	public static MSNBCDataset getMSNBC() throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException{
		return new MSNBCDataset(classLoader.getResource("datasets/MSNBC/RawTextsSimpleChars_utf8").getFile(), classLoader.getResource("datasets/MSNBC/Problems").getFile(), WikipediaApiInterface.api());
	}
}
