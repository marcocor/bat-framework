package it.unipi.di.acube.batframework.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.DBMaker.Maker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class WikipediaLocalInterface extends WikipediaInterface {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String REDIRECT_FILENAME = "redirects_en.ttl.bz2";
	private static final String PAGEIDS_FILENAME = "page_ids_en.ttl.bz2";
	private static final Pattern DBPEDIA_RESOURCE_URI = Pattern.compile("http://dbpedia.org/resource/(.*)");
	private static final String DBPEDIA_WID_RELATION = "http://dbpedia.org/ontology/wikiPageID";
	private static final String DBPEDIA_REDIRECT_RELATION = "http://dbpedia.org/ontology/wikiPageRedirects";
	private HTreeMap<Integer, String> widToTitle;
	private HTreeMap<String, Integer> titleToWid;
	private HTreeMap<Integer, Integer> redirectToWid;
	private DB db;

	public static WikipediaLocalInterface open(String path){
		return new WikipediaLocalInterface(path, true);
	}
	
	private WikipediaLocalInterface(String datasetFile, boolean readOnly) {
		Maker maker = DBMaker.fileDB(datasetFile).fileMmapEnable().closeOnJvmShutdown();
		if (readOnly)
			maker.readOnly();
		db = maker.make();
		widToTitle = db.hashMap("widToTitle", Serializer.INTEGER, Serializer.STRING).createOrOpen();
		titleToWid = db.hashMap("titleToWid", Serializer.STRING, Serializer.INTEGER).createOrOpen();
		redirectToWid = db.hashMap("redirectToWid", Serializer.INTEGER, Serializer.INTEGER).createOrOpen();
	}

	@Override
	public int getIdByTitle(String title) throws IOException {
		title = normalize(title);
		return titleToWid.containsKey(title) ? titleToWid.get(title) : -1;
	}

	@Override
	public String getTitlebyId(int wid) throws IOException {
		return widToTitle.get(wid);
	}

	@Override
	public boolean isRedirect(int wid) throws IOException {
		return (wid != dereference(wid));
	}

	@Override
	public int dereference(int wid) throws IOException {
		if (!redirectToWid.containsKey(wid) && widToTitle.containsKey(wid))
			return wid;
		return redirectToWid.containsKey(wid) ? redirectToWid.get(wid) : -1;
	}

	@Override
	public void prefetchTitles(List<String> titlesToPrefetch)
	        throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
	}

	@Override
	public void prefetchWids(List<Integer> widsToPrefetch)
	        throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
	}

	@Override
	public void flush() throws FileNotFoundException, IOException {
	}

	private static void createDB(String inputPath, String outputFile) throws FileNotFoundException, IOException {
		WikipediaLocalInterface wli = new WikipediaLocalInterface(outputFile, false);

		{
			PipedRDFIterator<Triple> iter = getTripleIterator(
			        new BZip2CompressorInputStream(FileUtils.openInputStream(Paths.get(inputPath, PAGEIDS_FILENAME).toFile())));

			long count = 0;
			while (iter.hasNext()) {
				Triple t = iter.next();

				String title = dbPediaUrlToTitle(t.getSubject().getURI());
				if (!t.getPredicate().getURI().equals(DBPEDIA_WID_RELATION))
					throw new IllegalArgumentException();
				int wid = (Integer) t.getObject().getLiteralValue();

				wli.titleToWid.put(title, wid);
				wli.widToTitle.put(wid, title);
				if (++count % 100000 == 0)
					LOG.info("Read {} pageids tuples.", count);
			}
		}

		{
			PipedRDFIterator<Triple> iter = getTripleIterator(
			        new BZip2CompressorInputStream(FileUtils.openInputStream(Paths.get(inputPath, REDIRECT_FILENAME).toFile())));

			int count = 0;
			while (iter.hasNext()) {
				Triple t = iter.next();

				String titleFrom = dbPediaUrlToTitle(t.getSubject().getURI());
				if (!t.getPredicate().getURI().equals(DBPEDIA_REDIRECT_RELATION))
					throw new IllegalArgumentException();
				String titleTo = dbPediaUrlToTitle(t.getObject().getURI());

				if (!wli.titleToWid.containsKey(titleFrom)) {
					LOG.warn("Could not find wid for from-title {}", titleFrom);
					continue;
				}
				if (!wli.titleToWid.containsKey(titleTo)) {
					LOG.warn("Could not find wid for to-title {}", titleTo);
					continue;
				}

				int widFrom = wli.titleToWid.get(titleFrom);
				int widTo = wli.titleToWid.get(titleTo);

				wli.redirectToWid.put(widFrom, widTo);

				if (++count % 100000 == 0)
					LOG.info("Read {} redirect tuples.", count);
			}
		}

		LOG.info("Committing changes...");
		wli.db.commit();

		LOG.info("Closing db...");
		wli.db.close();
	}

	public static String dbPediaUrlToTitle(String uri) {
		Matcher m = DBPEDIA_RESOURCE_URI.matcher(uri);
		if (!m.matches())
			throw new IllegalArgumentException();
		try {
			return normalize(URLDecoder.decode(m.group(1), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static PipedRDFIterator<Triple> getTripleIterator(final BZip2CompressorInputStream readFileStream) {
		PipedRDFIterator<Triple> it = new PipedRDFIterator<>();
		final PipedRDFStream<Triple> tripleStream = new PipedTriplesStream(it);

		ExecutorService executor = Executors.newSingleThreadExecutor();

		Runnable parser = new Runnable() {
			@Override
			public void run() {
				RDFDataMgr.parse(tripleStream, readFileStream, Lang.TURTLE);
			}
		};

		executor.submit(parser);
		return it;
	}

	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new GnuParser();
		Options options = new Options();
		options.addOption("i", "input", true, "Path where input TTL files reside.");
		options.addOption("o", "output", true, "Output MAPDB file.");
		CommandLine line = parser.parse(options, args);

		LOG.info("Creating local Wikipedia pages database... ");
		createDB(line.getOptionValue("input"), line.getOptionValue("output"));
		LOG.info("Done.");
	}
}
