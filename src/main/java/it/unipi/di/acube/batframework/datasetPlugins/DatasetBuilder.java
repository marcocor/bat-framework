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
import it.unipi.di.acube.batframework.utils.WikipediaInterface;

public class DatasetBuilder {
	private static final ClassLoader classLoader = DatasetBuilder.class.getClassLoader();

	public static GERDAQDataset getGerdaqTrainA(WikipediaInterface i){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_trainingA.xml"), i, "trainingA");
	}
	public static GERDAQDataset getGerdaqTrainB(WikipediaInterface i){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_trainingB.xml"), i, "trainingB");
	}
	public static GERDAQDataset getGerdaqDevel(WikipediaInterface i){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_devel.xml"), i, "devel");
	}
	public static GERDAQDataset getGerdaqTest(WikipediaInterface i){
		return new GERDAQDataset(classLoader.getResourceAsStream("datasets/gerdaq_1.0/gerdaq_test.xml"), i, "test");
	}
	public static ERD2014Dataset getERD(FreebaseApi freebaseApi, WikipediaInterface i) throws IOException, JSONException{
		return new ERD2014Dataset(classLoader.getResourceAsStream("datasets/erd2014/Trec_beta.query.txt"), classLoader.getResourceAsStream("datasets/erd2014/Trec_beta.annotation.txt"), freebaseApi, i);
	}
	public static ACE2004Dataset getACE2004(WikipediaInterface i) throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, URISyntaxException{
		return new ACE2004Dataset(Utils.getResourceListing(classLoader, "datasets/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts", "^[^\\.]+.*"), Utils.getResourceListing(classLoader, "datasets/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts", "^[^\\.]+.*"), i);
	}
	public static AQUAINTDataset getAQUAINT(WikipediaInterface i) throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException, URISyntaxException{
		return new AQUAINTDataset(Utils.getResourceListing(classLoader, "datasets/AQUAINT/RawTexts",  ".+\\.htm"), Utils.getResourceListing(classLoader, "datasets/AQUAINT/Problems", ".+\\.htm"), i);
	}
	public static KddDataset getKDDDevel(WikipediaInterface i) throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new KddDataset(new InputStream[]{classLoader.getResourceAsStream("datasets/kdd/kdd_amt_d_1.txt")}, i);
	}
	public static KddDataset getKDDTest(WikipediaInterface i) throws AnnotationException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
		return new KddDataset(new InputStream[]{classLoader.getResourceAsStream("datasets/kdd/kdd_amt_t_1.txt")}, i);
	}
	public static MeijDataset getMeij() throws IOException {
		return new MeijDataset(classLoader.getResourceAsStream("datasets/meij/original_tweets.list"), classLoader.getResourceAsStream("datasets/meij/wsdm2012_annotations.txt"), classLoader.getResourceAsStream("datasets/meij/wsdm2012_qrels.txt"));
	}
	public static MSNBCDataset getMSNBC(WikipediaInterface i) throws AnnotationException, XPathExpressionException, IOException,
	        ParserConfigurationException, SAXException, URISyntaxException {
		return new MSNBCDataset(Utils.getResourceListing(classLoader, "datasets/MSNBC/RawTextsSimpleChars_utf8", ".+\\.txt"),
		        Utils.getResourceListing(classLoader, "datasets/MSNBC/Problems", ".+\\.txt"), i);
	}
	public static A2WDataset getIITB(WikipediaInterface i) throws AnnotationException, XPathExpressionException, UnsupportedEncodingException, IOException, ParserConfigurationException, SAXException, URISyntaxException {
		return new IITBDataset(Utils.getResourceListing(classLoader, "datasets/iitb/crawledDocs", ".*"), classLoader.getResourceAsStream("datasets/iitb/CSAW_Annotations.xml"), i);
	}
	public static A2WDataset getNEEL2016Training(WikipediaInterface i) throws IOException {
		return new NEEL2016Dataset(classLoader.getResourceAsStream("datasets/neel2016/NEEL2016-training_neel.gs"), classLoader.getResourceAsStream("datasets/neel2016/NEEL2016-training.tsv"), i, "training");
	}
	public static A2WDataset getNEEL2016Dev(WikipediaInterface i) throws IOException {
		return new NEEL2016Dataset(classLoader.getResourceAsStream("datasets/neel2016/NEEL2016-dev_neel.gs"), classLoader.getResourceAsStream("datasets/neel2016/NEEL2016-dev.tsv"), i, "dev");
	}
	public static A2WDataset getNEEL2016Test(WikipediaInterface i) throws IOException {
		return new NEEL2016Dataset(classLoader.getResourceAsStream("datasets/neel2016/NEEL2016-test_neel.gs"), classLoader.getResourceAsStream("datasets/neel2016/NEEL2016-test.tsv"), i, "test");
	}
}
