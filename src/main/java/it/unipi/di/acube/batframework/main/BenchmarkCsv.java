package it.unipi.di.acube.batframework.main;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.datasetPlugins.CsvDataset;
import it.unipi.di.acube.batframework.metrics.ConceptAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.MatchRelation;
import it.unipi.di.acube.batframework.metrics.MentionAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.MetricsResultSet;
import it.unipi.di.acube.batframework.metrics.StrongAnnotationMatch;
import it.unipi.di.acube.batframework.metrics.WeakAnnotationMatch;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.systemPlugins.MockAnnotator;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.Pair;
import it.unipi.di.acube.batframework.utils.RunExperiments;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class BenchmarkCsv {
	static String datasetCsv;
	static String systemCsv;
	static boolean printSam = false;
	static boolean printWam = true;
	static boolean printCam = false;
	static boolean printMam = false;
	static boolean printMicro = false;
	static boolean printMacro = true;
	static boolean printPn = true;
	static boolean printPerdoc = false;
	static boolean explain = false;
	static String widsCache = "wids.cache";
	static String redirectCache = "redirect.cache";

	public static A2WDataset loadDatasetCsv(String filename)
			throws NumberFormatException, AnnotationException, IOException {
		return new CsvDataset(filename, filename);
	}

	public static MockAnnotator loadAnnotatorCsv(String filename,
			List<String> docIds) throws NumberFormatException,
			AnnotationException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		String line = null;
		HashMap<String, HashSet<ScoredAnnotation>> answers = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(",");
			if (tokens.length != 5) {
				br.close();
				throw new RuntimeException(String.format(
						"Line in file %s malformed: [%s]", filename, line));
			}
			String docId = tokens[0];
			int start = Integer.parseInt(tokens[1]);
			int end = Integer.parseInt(tokens[2]);
			int wikiId = Integer.parseInt(tokens[3]);
			float score = Float.parseFloat(tokens[4]);
			if (start < 0 || end < 0 || wikiId < 0) {
				br.close();
				throw new RuntimeException(
						"start, end and wikipediaId must be greater that zero.");
			}
			if (!answers.containsKey(docId))
				answers.put(docId, new HashSet<ScoredAnnotation>());
			answers.get(docId).add(
					new ScoredAnnotation(start, end - start, wikiId, score));
		}
		br.close();
		for (String docId : docIds)
			if (!answers.containsKey(docId))
				answers.put(docId, new HashSet<ScoredAnnotation>());
		return new MockAnnotator(answers, filename);
	}

	public static void main(String[] args) throws Exception {

		Options opts = new Options();
		opts.addOption("d", "dataset-csv", true, "the dataset CSV file");
		opts.addOption("s", "system-csv", true, "the system output CSV file");
		opts.addOption("sam", true, String.format(
				"print Strong Annotation Match comparisons (default: %b)",
				printSam));
		opts.addOption("wam", true, String.format(
				"print Weak Annotation Match comparisons (default: %b)",
				printWam));
		opts.addOption("cam", true, String.format(
				"print Concept Match comparisons (default: %b)", printCam));
		opts.addOption("mam", true, String.format(
				"print Mention Match comparisons (default: %b)", printMam));
		opts.addOption("micro", true, String.format(
				"print micro aggregated results (default: %b)", printMicro));
		opts.addOption("macro", true, String.format(
				"print macro aggregated results (default: %b)", printMacro));
		opts.addOption("pn", true, String.format(
				"print count of positive/negative annotations (default: %b)",
				printPn));
		opts.addOption("perdocument", true, String.format(
				"print per-document results (default: %b)", printPerdoc));
		opts.addOption(
				"explain",
				true,
				String.format(
						"verbousely explain each printed value. This is for beginners only (default: %b)",
						explain));
		opts.addOption("widscache", true, String.format(
				"set cache file for wikipedia ids (default: %s)", widsCache));
		opts.addOption("redirectcache", true, String.format(
				"set cache file for wikipedia redirects (default: %s)",
				redirectCache));

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(opts, args);

		if (!checkParams(cmd)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("BenchmarkCsv", opts);
			System.exit(1);
		}

		datasetCsv = cmd.getOptionValue("dataset-csv");
		systemCsv = cmd.getOptionValue("system-csv");
		if (cmd.hasOption("sam"))
			printSam = Boolean.parseBoolean(cmd.getOptionValue("sam"));
		if (cmd.hasOption("wam"))
			printWam = Boolean.parseBoolean(cmd.getOptionValue("wam"));
		if (cmd.hasOption("cam"))
			printCam = Boolean.parseBoolean(cmd.getOptionValue("cam"));
		if (cmd.hasOption("mam"))
			printMam = Boolean.parseBoolean(cmd.getOptionValue("mam"));
		if (cmd.hasOption("micro"))
			printMicro = Boolean.parseBoolean(cmd.getOptionValue("micro"));
		if (cmd.hasOption("macro"))
			printMacro = Boolean.parseBoolean(cmd.getOptionValue("macro"));
		if (cmd.hasOption("pn"))
			printPn = Boolean.parseBoolean(cmd.getOptionValue("pn"));
		if (cmd.hasOption("explain"))
			explain = Boolean.parseBoolean(cmd.getOptionValue("explain"));
		if (cmd.hasOption("perdocument"))
			printPerdoc = Boolean.parseBoolean(cmd
					.getOptionValue("perdocument"));
		if (cmd.hasOption("widscache"))
			widsCache = cmd.getOptionValue("widscache");
		if (cmd.hasOption("redirectcache"))
			redirectCache = cmd.getOptionValue("redirectcache");

		WikipediaApiInterface wikiApi = new WikipediaApiInterface(widsCache,
				redirectCache);
		A2WDataset ds = loadDatasetCsv(datasetCsv);
		Sa2WSystem ann = loadAnnotatorCsv(systemCsv, ds.getTextInstanceList());

		Vector<MatchRelation<Annotation>> matchRels = new Vector<>();
		StrongAnnotationMatch sam = new StrongAnnotationMatch(wikiApi);
		WeakAnnotationMatch wam = new WeakAnnotationMatch(wikiApi);
		ConceptAnnotationMatch cam = new ConceptAnnotationMatch(wikiApi);
		MentionAnnotationMatch mam = new MentionAnnotationMatch();
		if (printSam)
			matchRels.add(sam);
		if (printWam)
			matchRels.add(wam);
		if (printCam)
			matchRels.add(cam);
		if (printMam)
			matchRels.add(mam);
		Vector<Sa2WSystem> sa2wAnnotators = new Vector<>();
		sa2wAnnotators.add(ann);
		Vector<A2WDataset> dss = new Vector<>();
		dss.add(ds);

		if (explain)
			System.out
					.println("[Now testing the system output againts all score threshold in the range [0,1], in search of the best threshold.]");

		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> res = RunExperiments
				.performA2WExpVarThreshold(matchRels, null, sa2wAnnotators,
						dss, wikiApi);

		if (printSam) {
			Pair<Float, MetricsResultSet> mrs = RunExperiments.getBestRecord(
					res, sam.getName(), ann.getName(), ds.getName());
			System.out.println(" *** Strong Annotation Match results ***");
			if (explain)
				System.out
						.println("[When checking the correctness of an annotation A, the Strong Annotation Match interprets A as correct if and only if there is an annotation G in the gold standard that is exactly the same as A, i.e. A and B have the same starting position, the same length, and point to the same Wikipedia entity (redirects are solved)]");
			printResults(mrs, ds.getTextInstanceList());
			System.out.println();
		}
		if (printWam) {
			Pair<Float, MetricsResultSet> mrs = RunExperiments.getBestRecord(
					res, wam.getName(), ann.getName(), ds.getName());
			System.out.println(" *** Weak Annotation Match results ***");
			if (explain)
				System.out
						.println("[When checking the correctness of an annotation A, the Weak Annotation Match interprets A as correct if and only if there is an annotation G in the gold standard that points to the same Wikipedia entity (redirects are solved) and A and B overlap. In other words, as long as the annotation points to the correct entity and somehow covers the correct portion of text (though not strictly), it is considered correct. For this reason, results based on WAM are always better than those based on SAM. This measure is suggested to test text annotators.]");
			printResults(mrs, ds.getTextInstanceList());
			System.out.println();
		}
		if (printCam) {
			Pair<Float, MetricsResultSet> mrs = RunExperiments.getBestRecord(
					res, cam.getName(), ann.getName(), ds.getName());
			System.out.println(" *** Concept Match results ***");
			if (explain)
				System.out
						.println("[The Concept Match interprets A as correct if and only if there is an annotatin in the gold standard that points to the same entity. This measure is useful to check an annotator performance in finding entities associated to a document without taking into account its ability to link them to the correct portion of text.]");
			printResults(mrs, ds.getTextInstanceList());
			System.out.println();
		}

		if (printMam) {
			Pair<Float, MetricsResultSet> mrs = RunExperiments.getBestRecord(
					res, mam.getName(), ann.getName(), ds.getName());
			System.out.println(" *** Mention Match results ***");
			if (explain)
				System.out
						.println("[The Mention Match interprets A as correct if and only if there is an annotatin in the gold standard that overlap with A. This measure is useful to check an annotator performance in spotting mentions in a document without taking into account its ability to link them to the correct entity. In other words, this measure tests the performance of the annotator in spotting mentions in documents.]");
			printResults(mrs, ds.getTextInstanceList());
			System.out.println();
		}

		wikiApi.flush();
	}

	private static boolean checkParams(CommandLine cmd) {
		return cmd.getOptionValue("d") != null
				&& cmd.getOptionValue("s") != null;
	}

	private static void printResults(Pair<Float, MetricsResultSet> mrs,
			List<String> docids) {
		printBestThreshold(mrs.first);
		if (explain)
			System.out
					.println("[This is the best threshold in the range [0,1], meaning the micro-F1 score for this measure is optimal when discarding annotations under this threshold.]");
	
		if (printMacro) {
			printResultsMacro(mrs.second);
			if (explain)
				System.out
						.println("[Macro results are the average of the results for each document.]");
			}
		if (printMicro) {
			printResultsMicro(mrs.second);
			if (explain)
				System.out
						.println("[Micro results are computed considering the global number of TP, FP, and FN.]");
		}
		if (printPn) {
			printResultsTpFpFn(mrs.second);
			if (explain)
				System.out
						.println("[These are the global True Positives, False Positives and False Negatives.]");
		}
		if (printPerdoc) {
			printResultsSingleDocument(docids, mrs.second);
			if (explain)
				System.out
						.println("[These are the measures for each document.]");
		}
	}

	private static void printResultsSingleDocument(List<String> docIds,
			MetricsResultSet rs) {
		for (int i = 0; i < rs.testedInstances(); i++)
			System.out.printf(
					"docid=%s P/R/F1=%.3f/%.3f/%.3f TP/FP/FN=%d/%d/%d%n",
					docIds.get(i), rs.getPrecisions(i), rs.getRecalls(i),
					rs.getF1s(i), rs.getTPs(i), rs.getFPs(i), rs.getFNs(i));
	}

	public static void printBestThreshold(float bestThreshold) {
		System.out.printf("Best threshold: %.3f%n", bestThreshold);
	}

	public static void printResultsMicro(MetricsResultSet rs) {
		System.out.printf("Micro measures: P/R/F1: %.3f/%.3f/%.3f%n",
				rs.getMicroPrecision(), rs.getMicroRecall(), rs.getMicroF1());
	}

	public static void printResultsMacro(MetricsResultSet rs) {
		System.out.printf("Macro measures: P/R/F1: %.3f/%.3f/%.3f%n",
				rs.getMacroPrecision(), rs.getMacroRecall(), rs.getMacroF1());
	}

	public static void printResultsTpFpFn(MetricsResultSet rs) {
		System.out.printf("TP/FP/FN: %d/%d/%d%n", rs.getGlobalTp(),
				rs.getGlobalFp(), rs.getGlobalFn());
	}
}
