/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.scripts;

import it.acubelab.batframework.cache.*;
import it.acubelab.batframework.data.*;
import it.acubelab.batframework.datasetPlugins.*;
import it.acubelab.batframework.examples.DummyDataset;
import it.acubelab.batframework.metrics.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.systemPlugins.*;
import it.acubelab.batframework.utils.*;

import java.io.*;
import java.util.*;

public class BenchmarkMain {
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		// use system DNS resolution
		java.security.Security.setProperty("networkaddress.cache.negative.ttl",
				"0");
		java.security.Security.setProperty("networkaddress.cache.ttl", "0");

		BenchmarkCache.useCache("/tmp/results.cache");
		System.out.println(BenchmarkCache.getCacheInfo());

		System.out.println("Creating the API to wikipedia...");
		WikipediaApiInterface wikiApi = new WikipediaApiInterface(
				"benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");
		DBPediaApi dbpApi = new DBPediaApi();

		System.out.println("Creating the taggers...");
		// WikipediaTagger illinois = new
		// IllinoisTagger("benchmark/systems/illinois/Config/Benchmark");
		/*
		 * Sc2WSystem tagmyQuery = new TagMyQueryTagger("en",
		 * "benchmark/configs/tagmyquery/config.xml",
		 * "benchmark/systems/tagmyquery/rat_stopword_obj_links"); Sa2WSystem
		 * tagme = new TagmeLocalAnnotator("en",
		 * "benchmark/configs/tagme/config.xml");
		 */
		Sa2WSystem tagme = new TagmeAnnotator(
				"benchmark/configs/tagme/config.xml");
		//Sa2WSystem tagme = new TagmeLocalAnnotator("en",
		//				 "/home/irproject/config.xml", null);
		/*
		 * Sa2WSystem illinois = new IllinoisAnnotator_Server(); Sa2WSystem
		 * miner = new
		 * WikipediaMinerAnnotator("benchmark/configs/wikipediaminer/config.xml"
		 * ); Sa2WSystem aidaLocal = new
		 * AIDALocalAnnotator("benchmark/configs/aida/config.xml", wikiApi);
		 * Sa2WSystem aidaPrior = new
		 * AIDAPriorityOnlyAnnotator("benchmark/configs/aida/config.xml",
		 * wikiApi); Sa2WSystem aidaCocktail = new
		 * AIDACockailPartyAnnotator("benchmark/configs/aida/config.xml",
		 * wikiApi); Sa2WSystem spotLight = new SpotlightAnnotator(dbpApi,
		 * wikiApi);
		 */
		Sa2WSystem wikiSense = new WikiSenseAnnotator();

		System.out.println("Loading the datasets...");
		A2WDataset dummyDs = new DummyDataset();
		C2WDataset meijDs = new MeijDataset(
				"benchmark/datasets/meij/original_tweets.list",
				"benchmark/datasets/meij/wsdm2012_annotations.txt",
				"benchmark/datasets/meij/wsdm2012_qrels.txt");
		A2WDataset aquaintDs = new AQUAINTDataset(
				"benchmark/datasets/AQUAINT/RawTexts",
				"benchmark/datasets/AQUAINT/Problems", wikiApi);
		A2WDataset ace2004Ds = new ACE2004Dataset(
				"benchmark/datasets/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/",
				"benchmark/datasets/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/",
				wikiApi);
		A2WDataset aidaDs = new ConllAidaDataset(
				"benchmark/datasets/aida/AIDA-YAGO2-dataset-update.tsv",
				wikiApi);
		A2WDataset aidaBDs = new ConllAidaTestBDataset(
				"benchmark/datasets/aida/AIDA-YAGO2-dataset-update.tsv",
				wikiApi);
		A2WDataset msnbcDs = new MSNBCDataset(
				"benchmark/datasets/MSNBC/RawTextsSimpleChars_utf8",
				"benchmark/datasets/MSNBC/Problems", wikiApi);
		A2WDataset kddDs = new KddDataset(new String[] {
				"benchmark/datasets/kdd/kdd_amt_d_1.txt",
				"benchmark/datasets/kdd/kdd_amt_t_1.txt" }, wikiApi);
		// A2WDataset kddEditDs = new KddDataset(new
		// String[]{"benchmark/datasets/kdd/kdd_edit.txt"}, wikiApi);
		A2WDataset emptyDs = new EmptyDataset();
		A2WDataset iitbDs = new IITBDataset(
				"benchmark/datasets/iitb/crawledDocs",
				"benchmark/datasets/iitb/CSAW_Annotations.xml", wikiApi);
		A2WDataset timeRunsDs = new TimerunsDataset(
				"benchmark/datasets/timeruns/balrog2.txt", 20, 75);

		/*
		 * precisionAndRecallT2W(tagme, dummyDs, api);
		 * System.out.println("Flushing benchmark cache (tags)...");
		 * Benchmark.flush(); precisionAndRecallC2W(illinois, ds, api);
		 * System.out.println("Flushing benchmark cache (tags)...");
		 * Benchmark.flush(); precisionAndRecallC2W(aida, ds, api);
		 * System.out.println("Flushing benchmark cache (tags)...");
		 * Benchmark.flush(); precisionAndRecallC2W(miner, ds, api);
		 * System.out.println("Flushing benchmark cache (tags)...");
		 * Benchmark.flush();
		 */

		Vector<A2WDataset> dssA2W = new Vector<A2WDataset>();
		// dssA2W.add(dummyDs);
		// dssA2W.add(emptyDs);
		// dssA2W.add(iitbDs);
		// dssA2W.add(msnbcDs);
		// dssA2W.add(aquaintDs);
		// dssA2W.add(kddDs);
		//dssA2W.add(aidaDs);
		dssA2W.add(aidaBDs);
		// dss.add(ace2004Ds);
		// dssA2W.add(timeRunsDs);
		// dssA2W.add(iitbDs);

		Vector<A2WSystem> a2wAnnotators = new Vector<A2WSystem>();

		Vector<Sc2WSystem> sc2wTaggers = new Vector<Sc2WSystem>();
		// sc2wTaggers.add(tagmyQuery);

		Vector<Sa2WSystem> sa2wAnnotators = new Vector<Sa2WSystem>();
		// sa2wAnnotators.add(aidaLocal);
		sa2wAnnotators.add(tagme);
		// sa2wAnnotators.add(illinois);
		// sa2wAnnotators.add(aidaCocktail);
		// sa2wAnnotators.add(aidaPrior);
		// sa2wAnnotators.add(spotLight);
		// sa2wAnnotators.add(miner);
		//sa2wAnnotators.add(wikiSense);

		// mapping: metric name -> tagger name -> dataset name -> list of
		// {value_id -> actual value}
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> resA2W;

		/** Building match relations */
		MatchRelation<Annotation> wam = new WeakAnnotationMatch(wikiApi);
		MatchRelation<Annotation> sam = new StrongAnnotationMatch(wikiApi);
		MatchRelation<Annotation> cam = new ConceptAnnotationMatch(wikiApi);
		MatchRelation<Annotation> mam = new MentionAnnotationMatch();

		/** A2W experiments */
		Vector<MatchRelation<Annotation>> matchRelationsA2W = new Vector<MatchRelation<Annotation>>();
		matchRelationsA2W.add(wam);
		/*
		 * matchRelationsA2W.add(sam); matchRelationsA2W.add(cam);
		 * matchRelationsA2W.add(mam);
		 */
		// matchRelationsA2W.add(new ProximityAnnotationMatch(wikiApi,
		// 1f,"benchmark/configs/tagme/config.xml"));

		resA2W = RunExperiments.performA2WExpVarThreshold(matchRelationsA2W,
				a2wAnnotators, sa2wAnnotators, dssA2W, wikiApi);
		DumpResults.printCorrectnessPerformance(matchRelationsA2W,
				a2wAnnotators, sa2wAnnotators, null, null, dssA2W, resA2W);
		DumpResults.latexCorrectnessPerformance(matchRelationsA2W,
				a2wAnnotators, sa2wAnnotators, sc2wTaggers, dssA2W, false,
				resA2W);
		DumpResults.gnuplotCorrectnessPerformance(matchRelationsA2W,
				a2wAnnotators, dssA2W, wikiApi, resA2W);

		/** C2W experiments */
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> resC2W;

		Vector<C2WDataset> dssC2W = new Vector<C2WDataset>();
		dssC2W.add(meijDs);
		// dssC2W.add(kddDs);
		// dssC2W.add(msnbcDs);
		// dssC2W.add(kddEditDs);

		Vector<MatchRelation<Tag>> matchRelationsC2W = new Vector<MatchRelation<Tag>>();
		matchRelationsC2W.add(new StrongTagMatch(wikiApi));
		resC2W = RunExperiments.performC2WExpVarThreshold(matchRelationsC2W,
				a2wAnnotators, sa2wAnnotators, sc2wTaggers, dssC2W, wikiApi);
		DumpResults.printCorrectnessPerformance(matchRelationsC2W,
				a2wAnnotators, sa2wAnnotators, sc2wTaggers, null, dssC2W, resC2W);
		DumpResults.latexCorrectnessPerformance(matchRelationsC2W,
				a2wAnnotators, sa2wAnnotators, sc2wTaggers, dssC2W, false,
				resC2W);
		Vector<C2WDataset> allDs = new Vector<C2WDataset>();
		allDs.addAll(dssC2W);
		allDs.addAll(dssA2W);
		DumpResults.gnuplotCorrectnessPerformance(matchRelationsA2W,
				a2wAnnotators, allDs, wikiApi, resA2W);

		/** D2W experiments */
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> resD2W;

		Vector<D2WDataset> dssD2W = new Vector<D2WDataset>();
		// dssD2W.add(aidaDs);
		// dssD2W.add(aidaBDs);
		// dssD2W.add(msnbcDs);

		Vector<MatchRelation<Annotation>> matchRelationsD2W = new Vector<MatchRelation<Annotation>>();
		matchRelationsD2W.add(new StrongAnnotationMatch(wikiApi));
		resD2W = RunExperiments.performD2WExpVarThreshold(null, sa2wAnnotators,
				dssD2W, wikiApi);
		DumpResults.printCorrectnessPerformance(matchRelationsD2W, null,
				sa2wAnnotators, null, null, dssD2W, resD2W);
		DumpResults.latexCorrectnessPerformance(matchRelationsD2W, null,
				sa2wAnnotators, null, dssD2W, false, resD2W);
		DumpResults.gnuplotCorrectnessPerformance(matchRelationsD2W, null,
				dssD2W, wikiApi, resD2W);

		/** Timing */
		// printTimingPerformance(a2wAnnotators, sa2wAnnotators, sc2wTaggers,
		// dssA2W, dssC2W);
		DumpResults.latexTimingPerformance(a2wAnnotators, sa2wAnnotators,
				sc2wTaggers, allDs);
		DumpResults.latexTimingPerformance2(sa2wAnnotators, sc2wTaggers, allDs);
		// DumpResults.gnuplotRuntimeF1(wam.getName(), a2wAnnotators,
		// sa2wAnnotators, dummyDs.getName(), wikiApi, resA2W);
		// traceTiming(sa2wAnnotators, timeRunsDs);

		// printMostRedirectDocument(dss, st2wTaggers, api);

		/** Difference */
		// DumpResults.latexSimilarityA2W(dssA2W, sa2wAnnotators, resA2W,
		// wikiApi);
		/* latexSimilarityC2W(dssC2W, sa2wAnnotators, bestThresholds, api); */
		// dumpDissimilarityA2W(dssA2W, sa2wAnnotators, bestThresholds, api);

		/*
		 * System.out.println("TagMyQuery dump");
		 * dumpResultSc2W(kddEditDs.getTextInstanceList(),
		 * kddEditDs.getC2WGoldStandardList(), Benchmark.doSc2WTags(tagmyQuery,
		 * kddEditDs), api); System.out.println(); System.out.println();
		 */

		Pair<Float, MetricsResultSet> result = RunExperiments.getBestRecord(
				resA2W, wam.getName(), wikiSense.getName(), aidaBDs.getName());
		System.out.println("WikiSense dump - thr. " + result.first);

		DumpData.dumpCompareList(aidaBDs.getTextInstanceList(), aidaBDs
				.getA2WGoldStandardList(), ProblemReduction.Sa2WToA2WList(
				BenchmarkCache.doSa2WAnnotations(wikiSense, aidaBDs, null, 0),
				result.first), wikiApi);

		/** Proximity test */
		// proximityTest(sa2wAnnotators, dssA2W, wikiApi, bestThresholds);

		/*
		 * spotLight.getLastAnnotationTime(); miner.getLastAnnotationTime();
		 */
		wikiApi.flush();

	}

	//
	//
	//
	// /**
	// * Make the annotations and compute macro- and micro- precision and recall
	// for the given Wikipedia Tagger
	// * and the given dataset, then print the results. This function will first
	// tag all the documents contained in
	// * the dataset and then compare their gold annotation against those found
	// by the tagger.
	// * @param tagger the system that tags spots contained in natural language
	// documents to wikipedia concepts.
	// * @param ds the dataset of documents and their right annotations.
	// * @throws Exception
	// */
	// private static void precisionAndRecallA2W(A2WSystem tagger, A2WDataset
	// ds, WikipediaApiInterface api) throws Exception {
	// System.out.println("Computing precision and recall for T2W problem. Tagger="+tagger.getName()+", Dataset="+ds.getName());
	// System.out.print("Doing annotations...");
	// List<Set<Annotation>> computedAnnotations =
	// BenchmarkCache.doA2WAnnotations(tagger, ds);
	// System.out.println(" done");
	// System.out.println("De-redirecting wikipedia urls found by annotator...");
	// Annotation.prefetchRedirectList(computedAnnotations, api);
	// System.out.println(" done");
	// System.out.println("De-redirecting wikipedia urls in dataset...");
	// Annotation.prefetchRedirectList(ds.getA2WGoldStandardList(), api);
	// System.out.println(" done");
	// DumpResults.printPrecisionAndRecall(tagger, new
	// StrongAnnotationMatch(api), ds.getA2WGoldStandardList(),
	// computedAnnotations, api);
	// //for (int i=0; i < ds.getTagsList(); i++
	// //dumpResultTag(ds.getTextList(), ds.getTagsList(), computedAnnotations,
	// api);
	// }
	//
	// private static void precisionAndRecallC2W(C2WSystem tagger, C2WDataset
	// ds, WikipediaApiInterface api) throws Exception {
	// System.out.println("Computing precision and recall for C2W problem. Tagger="+tagger.getName()+", Dataset="+ds.getName());
	// System.out.print("Doing annotations...");
	// List<Set<Tag>> computedAnnotations = BenchmarkCache.doC2WTags(tagger,
	// ds);
	// System.out.println(" done");
	// System.out.println("De-redirecting wikipedia urls found by annotator...");
	// Annotation.prefetchRedirectList(computedAnnotations, api);
	// System.out.println(" done");
	// System.out.println("De-redirecting wikipedia urls in dataset...");
	// Annotation.prefetchRedirectList(ds.getC2WGoldStandardList(), api);
	// System.out.println(" done");
	// DumpData.dumpCompareList(ds.getTextInstanceList(),
	// ds.getC2WGoldStandardList(), computedAnnotations, api);
	// DumpResults.printPrecisionAndRecall(tagger, new StrongTagMatch(api),
	// ds.getC2WGoldStandardList(), computedAnnotations, api);
	// }
	//
	//
	//

}