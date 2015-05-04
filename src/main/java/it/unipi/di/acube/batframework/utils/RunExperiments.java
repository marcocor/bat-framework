/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.utils;

import it.unipi.di.acube.batframework.cache.BenchmarkCache;
import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.metrics.*;
import it.unipi.di.acube.batframework.problems.*;

import java.util.*;

/**
 * Static methods to run the experiments. A set of annotators are run on a set
 * of datasets, and the metrics are computer according to a set of match
 * relations. The result is written in resulting hash tables.
 * 
 */
public class RunExperiments {

	private static double THRESHOLD_STEP = 1. / 128.;

	public static void computeMetricsA2WFakeReductionToSa2W(
			MatchRelation<Annotation> m,
			A2WSystem tagger,
			A2WDataset ds,
			String precisionFilename,
			String recallFilename,
			String F1Filename,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Annotation> metrics = new Metrics<Annotation>();
		float threshold = 0;
		System.out.print("Doing annotations... ");
		List<HashSet<Annotation>> computedAnnotations = BenchmarkCache
				.doA2WAnnotations(tagger, ds);
		System.out.println("Done.");
		for (threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
			MetricsResultSet rs = metrics.getResult(computedAnnotations,
					ds.getA2WGoldStandardList(), m);
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static void computeMetricsA2WReducedFromSa2W(
			MatchRelation<Annotation> m,
			Sa2WSystem tagger,
			A2WDataset ds,
			String precisionFilename,
			String recallFilename,
			String F1Filename,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Annotation> metrics = new Metrics<Annotation>();
		System.out.println("Doing annotations... ");
		List<HashSet<ScoredAnnotation>> computedAnnotations = BenchmarkCache
				.doSa2WAnnotations(tagger, ds, new AnnotatingCallback() {
					public void run(long msec, int doneDocs, int totalDocs,
							int foundTags) {
						System.out
								.printf("Done %d/%d documents. Found %d annotations/tags so far.%n",
										doneDocs, totalDocs, foundTags);
					}
				}, 60000);
		System.out.println("Done with all documents.");
		for (double threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
			System.out.println("Testing with tagger: " + tagger.getName()
					+ " dataset: " + ds.getName() + " score threshold: "
					+ threshold);
			List<HashSet<Annotation>> reducedTags = ProblemReduction
					.Sa2WToA2WList(computedAnnotations, (float) threshold);
			MetricsResultSet rs = metrics.getResult(reducedTags,
					ds.getA2WGoldStandardList(), m);
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static void computeMetricsC2WReducedFromSa2W(
			MatchRelation<Tag> m,
			Sa2WSystem tagger,
			C2WDataset ds,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Tag> metrics = new Metrics<Tag>();
		System.out.println("Doing annotations... ");
		List<HashSet<ScoredAnnotation>> computedAnnotations = BenchmarkCache
				.doSa2WAnnotations(tagger, ds, new AnnotatingCallback() {
					public void run(long msec, int doneDocs, int totalDocs,
							int foundTags) {
						System.out
								.printf("Done %d/%d documents. Found %d annotations/tags so far.%n",
										doneDocs, totalDocs, foundTags);
					}
				}, 60000);
		System.out.println("Done with all documents.");
		for (double threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
			System.out.println("Testing with tagger: " + tagger.getName()
					+ " dataset: " + ds.getName() + " score threshold: "
					+ threshold);
			List<HashSet<Annotation>> reducedAnnotations = ProblemReduction
					.Sa2WToA2WList(computedAnnotations, (float) threshold);
			List<HashSet<Tag>> reducedTags = ProblemReduction
					.A2WToC2WList(reducedAnnotations);
			List<HashSet<Tag>> reducedGs = ds.getC2WGoldStandardList();
			MetricsResultSet rs = metrics.getResult(reducedTags, reducedGs, m);
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static void computeMetricsC2WReducedFromSc2W(
			MatchRelation<Tag> m,
			Sc2WSystem tagger,
			C2WDataset ds,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Tag> metrics = new Metrics<Tag>();
		double threshold = 0;
		System.out.print("Doing annotations... ");
		List<HashSet<ScoredTag>> computedAnnotations = BenchmarkCache
				.doSc2WTags(tagger, ds);
		System.out.println("Done.");
		for (threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
			System.out.println("Testing with tagger: " + tagger.getName()
					+ " dataset: " + ds.getName() + " score threshold: "
					+ threshold);
			List<HashSet<Tag>> reducedAnnotations = ProblemReduction
					.Sc2WToC2WList(computedAnnotations, (float) threshold);
			List<HashSet<Tag>> reducedGs = ds.getC2WGoldStandardList();
			MetricsResultSet rs = metrics.getResult(reducedAnnotations,
					reducedGs, m);
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static void computeMetricsC2W(
			MatchRelation<Tag> m,
			C2WSystem tagger,
			C2WDataset ds,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Tag> metrics = new Metrics<Tag>();
		double threshold = 0;
		System.out.print("Doing annotations... ");
		List<HashSet<Tag>> computedAnnotations = BenchmarkCache.doC2WTags(
				tagger, ds);
		System.out.println("Done.");
		System.out.println("Testing with tagger: " + tagger.getName()
				+ " dataset: " + ds.getName() + " (no score thr.)");
		MetricsResultSet rs = metrics.getResult(computedAnnotations,
				ds.getC2WGoldStandardList(), m);
		for (threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static void computeMetricsD2WFakeReductionToSa2W(
			D2WSystem tagger,
			D2WDataset ds,
			String precisionFilename,
			String recallFilename,
			String F1Filename,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Annotation> metrics = new Metrics<Annotation>();
		StrongAnnotationMatch m = new StrongAnnotationMatch(api);
		float threshold = 0;
		System.out.print("Doing native D2W annotations... ");
		List<HashSet<Annotation>> computedAnnotations = BenchmarkCache
				.doD2WAnnotations(tagger, ds, new AnnotatingCallback() {
					public void run(long msec, int doneDocs, int totalDocs,
							int foundTags) {
						System.out
								.printf("Done %d/%d documents. Found %d annotations so far.%n",
										doneDocs, totalDocs, foundTags);
					}
				}, 60000);
		System.out.println("Done with all documents.");
		for (threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {
			MetricsResultSet rs = metrics.getResult(computedAnnotations,
					ds.getD2WGoldStandardList(), m);
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static void computeMetricsD2WReducedFromSa2W(
			Sa2WSystem tagger,
			D2WDataset ds,
			String precisionFilename,
			String recallFilename,
			String F1Filename,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> results)
			throws Exception {
		Metrics<Annotation> metrics = new Metrics<Annotation>();
		StrongAnnotationMatch m = new StrongAnnotationMatch(api);
		System.out.println("Doing annotations... ");
		List<HashSet<ScoredAnnotation>> computedAnnotations = BenchmarkCache
				.doSa2WAnnotations(tagger, ds, new AnnotatingCallback() {
					public void run(long msec, int doneDocs, int totalDocs,
							int foundTags) {
						System.out
								.printf("Done %d/%d documents. Found %d annotations/tags so far.%n",
										doneDocs, totalDocs, foundTags);
					}
				}, 60000);
		System.out.println("Done with all documents.");
		System.out
				.printf("Testing with tagger: %s, dataset: %s, for values of the score threshold in [0,1].%n",
						tagger.getName(), ds.getName());
		for (double threshold = 0; threshold <= 1; threshold += THRESHOLD_STEP) {

			List<HashSet<Annotation>> reducedAnns = ProblemReduction
					.Sa2WToD2WList(computedAnnotations,
							ds.getMentionsInstanceList(), (float) threshold);
			MetricsResultSet rs = metrics.getResult(reducedAnns,
					ds.getD2WGoldStandardList(), m);
			updateThresholdRecords(results, m.getName(), tagger.getName(),
					ds.getName(), (float) threshold, rs);
		}
	}

	public static HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> performC2WExpVarThreshold(
			Vector<MatchRelation<Tag>> matchRels,
			Vector<A2WSystem> a2wAnnotators, Vector<Sa2WSystem> sa2wAnnotators,
			Vector<Sc2WSystem> sc2wTaggers,
			Vector<C2WSystem> c2wTaggers, Vector<C2WDataset> dss,
			WikipediaApiInterface api) throws Exception {
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> result = new HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>>();
		for (MatchRelation<Tag> m : matchRels)
			for (C2WDataset ds : dss) {
				System.out.println("Testing " + ds.getName()
						+ " with score threshold parameter...");

				if (sa2wAnnotators != null)
					for (Sa2WSystem t : sa2wAnnotators) {
						computeMetricsC2WReducedFromSa2W(m, t, ds, api, result);
						BenchmarkCache.flush();
					}

				if (sc2wTaggers != null)
					for (Sc2WSystem t : sc2wTaggers) {
						computeMetricsC2WReducedFromSc2W(m, t, ds, api, result);
						BenchmarkCache.flush();
					}
				if (c2wTaggers != null)
					for (C2WSystem t : c2wTaggers) {
						computeMetricsC2W(m, t, ds, api, result);
						BenchmarkCache.flush();
					}

				System.out.println("Flushing Wikipedia API cache...");
				api.flush();
			}
		return result;
	}

	public static HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> performA2WExpVarThreshold(
			Vector<MatchRelation<Annotation>> metrics,
			Vector<A2WSystem> a2wTaggers, Vector<Sa2WSystem> sa2wTaggers,
			Vector<A2WDataset> dss, WikipediaApiInterface api) throws Exception {
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> result = new HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>>();
		for (MatchRelation<Annotation> metric : metrics) {
			for (A2WDataset ds : dss) {
				if (sa2wTaggers != null)
					for (Sa2WSystem t : sa2wTaggers) {
						System.out.println("Testing " + ds.getName() + " on "
								+ t.getName()
								+ " with score threshold parameter...");
						String prefix = metric.getName()
								.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
						String suffix = t.getName()
								.replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
								+ "_"
								+ ds.getName().replaceAll("[^a-zA-Z0-9]", "")
										.toLowerCase() + ".dat";
						computeMetricsA2WReducedFromSa2W(metric, t, ds, prefix
								+ "_precision_threshold_" + suffix, prefix
								+ "_recall_threshold_" + suffix, prefix
								+ "_f1_threshold_" + suffix, api, result);
						BenchmarkCache.flush();
					}

				if (a2wTaggers != null)
					for (A2WSystem t : a2wTaggers) {
						System.out.println("Testing " + ds.getName() + " on "
								+ t.getName()
								+ " with score threshold parameter...");
						String prefix = metric.getName()
								.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
						String suffix = t.getName()
								.replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
								+ "_"
								+ ds.getName().replaceAll("[^a-zA-Z0-9]", "")
										.toLowerCase() + ".dat";
						computeMetricsA2WFakeReductionToSa2W(metric, t, ds,
								prefix + "_precision_threshold_" + suffix,
								prefix + "_recall_threshold_" + suffix, prefix
										+ "_f1_threshold_" + suffix, api,
								result);
						BenchmarkCache.flush();
					}

				System.out.println("Flushing Wikipedia API cache...");
				api.flush();
			}
		}
		return result;
	}

	public static HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> performD2WExpVarThreshold(
			Vector<D2WSystem> d2wAnnotators, Vector<Sa2WSystem> sa2wAnnotators,
			Vector<D2WDataset> dss, WikipediaApiInterface api) throws Exception {
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> result = new HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>>();
		MatchRelation<Annotation> sam = new StrongAnnotationMatch(api);
		for (D2WDataset ds : dss) {
			if (sa2wAnnotators != null)
				for (Sa2WSystem t : sa2wAnnotators) {
					System.out.println("Testing " + ds.getName() + " on "
							+ t.getName()
							+ " with score threshold parameter...");
					String prefix = sam.getName()
							.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
					String suffix = t.getName().replaceAll("[^a-zA-Z0-9]", "")
							.toLowerCase()
							+ "_"
							+ ds.getName().replaceAll("[^a-zA-Z0-9]", "")
									.toLowerCase() + ".dat";
					computeMetricsD2WReducedFromSa2W(t, ds, prefix
							+ "_precision_threshold_" + suffix, prefix
							+ "_recall_threshold_" + suffix, prefix
							+ "_f1_threshold_" + suffix, api, result);
					BenchmarkCache.flush();
				}
			if (d2wAnnotators != null)
				for (D2WSystem t : d2wAnnotators) {
					System.out.println("Testing " + ds.getName() + " on "
							+ t.getName()
							+ " with score threshold parameter...");
					String prefix = sam.getName()
							.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
					String suffix = t.getName().replaceAll("[^a-zA-Z0-9]", "")
							.toLowerCase()
							+ "_"
							+ ds.getName().replaceAll("[^a-zA-Z0-9]", "")
									.toLowerCase() + ".dat";
					computeMetricsD2WFakeReductionToSa2W(t, ds, prefix
							+ "_precision_threshold_" + suffix, prefix
							+ "_recall_threshold_" + suffix, prefix
							+ "_f1_threshold_" + suffix, api, result);
					BenchmarkCache.flush();
				}

			System.out.println("Flushing Wikipedia API cache...");
			api.flush();
		}
		return result;
	}

	private static void updateThresholdRecords(
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords,
			String metricsName, String taggerName, String datasetName,
			float threshold, MetricsResultSet rs) {
		HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>> bestThreshold;
		if (!threshRecords.containsKey(metricsName))
			threshRecords
					.put(metricsName,
							new HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>());
		bestThreshold = threshRecords.get(metricsName);

		HashMap<String, HashMap<Float, MetricsResultSet>> firstLevel;
		if (!bestThreshold.containsKey(taggerName))
			bestThreshold.put(taggerName,
					new HashMap<String, HashMap<Float, MetricsResultSet>>());
		firstLevel = bestThreshold.get(taggerName);

		HashMap<Float, MetricsResultSet> secondLevel;
		if (!firstLevel.containsKey(datasetName))
			firstLevel.put(datasetName, new HashMap<Float, MetricsResultSet>());
		secondLevel = firstLevel.get(datasetName);

		// populate the hash table with the new record.
		secondLevel.put(threshold, rs);
	}

	public static Pair<Float, MetricsResultSet> getBestRecord(
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshResults,
			String metricsName, String taggerName, String datasetName) {
		HashMap<Float, MetricsResultSet> records = threshResults
				.get(metricsName).get(taggerName).get(datasetName);
		List<Float> thresholds = new Vector<Float>(records.keySet());
		Collections.sort(thresholds);
		Pair<Float, MetricsResultSet> bestRecord = null;
		for (Float t : thresholds)
			if (bestRecord == null
					|| records.get(t).getMacroF1() > bestRecord.second
							.getMacroF1())
				bestRecord = new Pair<Float, MetricsResultSet>(t,
						records.get(t));
		return bestRecord;
	}

	public static HashMap<Float, MetricsResultSet> getRecords(
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshResults,
			String metricsName, String taggerName, String datasetName) {
		HashMap<Float, MetricsResultSet> records = threshResults
				.get(metricsName).get(taggerName).get(datasetName);
		return records;

	}

	public static MetricsResultSet performMentionSpottingExp(
			MentionSpotter spotter, D2WDataset ds) throws Exception {
		List<HashSet<Mention>> output = BenchmarkCache.doSpotMentions(spotter,
				ds);
		Metrics<Mention> metrics = new Metrics<Mention>();
		return metrics.getResult(output, ds.getMentionsInstanceList(),
				new MentionMatch());
	}

	public static void performMentionSpottingExp(
			List<MentionSpotter> mentionSpotters, List<D2WDataset> dss)
			throws Exception {
		for (MentionSpotter spotter : mentionSpotters) {
			for (D2WDataset ds : dss) {
				System.out.println("Testing Spotter " + spotter.getName()
						+ " on dataset " + ds.getName());
				System.out.println("Doing spotting... ");
				MetricsResultSet rs = performMentionSpottingExp(spotter, ds);
				System.out.println("Done with all documents.");

				System.out.printf("%s / %s%n%s%n%n", spotter.getName(),
						ds.getName(), rs);
			}
		}

	}

	public static MetricsResultSet performCandidateSpottingExp(
			CandidatesSpotter spotter, D2WDataset dss, WikipediaApiInterface api)
			throws Exception {
		Metrics<MultipleAnnotation> metrics = new Metrics<MultipleAnnotation>();

		List<HashSet<MultipleAnnotation>> gold = annotationToMulti(dss
				.getD2WGoldStandardList());

		List<HashSet<MultipleAnnotation>> output = new Vector<HashSet<MultipleAnnotation>>();
		for (String text : dss.getTextInstanceList())
			output.add(spotter.getSpottedCandidates(text));

		// Filter system annotations so that only those contained in the dataset
		// AND in the output are taken into account.
		output = mentionSubstraction(output, gold);
		gold = mentionSubstraction(gold, output);

		return metrics.getResult(output, gold, new MultiEntityMatch(api));

	}

	public static Integer[] candidateCoverageDistributionExp(
			CandidatesSpotter spotter, D2WDataset dss, WikipediaApiInterface api)
			throws Exception {
		List<HashSet<MultipleAnnotation>> gold = annotationToMulti(dss
				.getD2WGoldStandardList());

		List<HashSet<MultipleAnnotation>> output = new Vector<HashSet<MultipleAnnotation>>();
		for (String text : dss.getTextInstanceList())
			output.add(spotter.getSpottedCandidates(text));

		output = mentionSubstraction(output, gold);
		gold = mentionSubstraction(gold, output);

		Vector<Integer> positions = new Vector<>();
		for (int i = 0; i < output.size(); i++) {
			HashSet<MultipleAnnotation> outI = output.get(i);
			HashSet<MultipleAnnotation> goldI = gold.get(i);
			for (MultipleAnnotation outAnn : outI)
				for (MultipleAnnotation goldAnn : goldI)
					if (outAnn.overlaps(goldAnn)) {
						int goldCand = goldAnn.getCandidates()[0];
						int candIdx = 0;
						for (; candIdx < outAnn.getCandidates().length; candIdx++)
							if (outAnn.getCandidates()[candIdx] == goldCand) {
								positions.add(candIdx);
								break;
							}
						if (candIdx == outAnn.getCandidates().length)
							positions.add(-1);
					}
		}

		return positions.toArray(new Integer[positions.size()]);

	}

	private static <T extends Mention> List<HashSet<T>> mentionSubstraction(
			List<HashSet<T>> list1, List<HashSet<T>> list2) {
		List<HashSet<T>> list1filtered = new Vector<HashSet<T>>();
		for (int i = 0; i < list1.size(); i++) {
			HashSet<T> filtered1 = new HashSet<T>();
			list1filtered.add(filtered1);
			for (T a : list1.get(i)) {
				boolean found = false;
				for (T goldA : list2.get(i))
					if (a.getPosition() == goldA.getPosition()
							&& a.getLength() == goldA.getLength()) {
						found = true;
						break;
					}
				if (found)
					filtered1.add(a);
			}
		}
		return list1filtered;
	}

	private static List<HashSet<MultipleAnnotation>> annotationToMulti(
			List<HashSet<Annotation>> d2wGoldStandardList) {
		List<HashSet<MultipleAnnotation>> res = new Vector<HashSet<MultipleAnnotation>>();
		for (HashSet<Annotation> annSet : d2wGoldStandardList) {
			HashSet<MultipleAnnotation> multiAnn = new HashSet<MultipleAnnotation>();
			res.add(multiAnn);
			for (Annotation a : annSet)
				multiAnn.add(new MultipleAnnotation(a.getPosition(), a
						.getLength(), new int[] { a.getConcept() }));
		}
		return res;
	}

}
