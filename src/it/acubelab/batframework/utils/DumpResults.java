/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import it.acubelab.batframework.cache.BenchmarkCache;
import it.acubelab.batframework.data.*;
import it.acubelab.batframework.metrics.*;
import it.acubelab.batframework.problems.*;

import java.io.*;
import java.util.*;

/**
 * This class contains all the methods to dump the result of an experiment,
 * both to the screen (methods print*), in latex form (methods latex*),
 * or as gnuplot data used to generate charts (methods gnuplot*).
 * 
 */
public class DumpResults {
	private static final Locale LOCALE = Locale.US;
	private static final String CHARTS_DIR = "charts/";

	private static class StringLengthComparator implements Comparator<String> {
		public int compare(String o1, String o2) {
			return o1.length() - o2.length();
		}
	}

	/*
	 * //this test features the proximity of wrong concepts to the correct ones,
	 * but needs the relatedness measure which is provided by tagme. private
	 * static void proximityTest(Vector<Sa2WSystem> annotators,
	 * Vector<A2WDataset> dss, WikipediaApiInterface api, HashMap<String,
	 * HashMap<String, HashMap<String, HashMap<String, Float>>>> bestThresholds)
	 * throws Exception {
	 * TagmeConfig.init("benchmark/configs/tagme/config.xml"); Relatedness rel =
	 * new Relatedness("en"); WeakAnnotationMatch wam = new
	 * WeakAnnotationMatch(api); MentionAnnotationMatch mam = new
	 * MentionAnnotationMatch();
	 * 
	 * for (Sa2WSystem annotator: annotators) for (A2WDataset ds: dss){
	 * 
	 * HashMap<String, Float> values = getBestThresholdValues(bestThresholds,
	 * wam.getName(), annotator.getName(), ds.getName()); // System.out.printf(
	 * "%s\t%s\t%.3f\tHashMap<String, HashMap<String, HashMap<String, List<HashMap<String, Float>>>>>%.3f\t%.3f\t%.3f%n"
	 * , ds.getName(), annotator.getName(), values.get("threshold"),
	 * values.get("micro-f1"), values.get("micro-precision"),
	 * values.get("micro-recall"));
	 * 
	 * List<Set<Annotation>> gs =
	 * wam.preProcessGoldStandard(ds.getA2WGoldStandardList());
	 * List<Set<ScoredAnnotation>> annotations =
	 * Benchmark.doSa2WAnnotations(annotator, ds); List<Set<Annotation>>
	 * reducedAnns =
	 * wam.preProcessOutput(ProblemReduction.Sa2WToA2WList(annotations,
	 * values.get("threshold")));
	 * 
	 * int n=20; int[] counter = new int[n]; int totalCount = 0; double sumRel =
	 * 0;
	 * 
	 * for (int i=0; i<ds.getSize(); i++){ //String instanceI =
	 * ds.getTextInstanceList().get(i); Set<Annotation> gsI = gs.get(i);
	 * Set<Annotation> outI = reducedAnns.get(i); for (Annotation aOut: outI){
	 * float bestRel = -1; for (Annotation aGs: gsI) if (!wam.match(aOut, aGs)
	 * && mam.match(aOut, aGs)) try{ bestRel = Math.max(bestRel,
	 * rel.rel(aOut.getConcept(), aGs.getConcept())); } catch
	 * (ArrayIndexOutOfBoundsException e){ //do nothing. } if (bestRel >= 0){
	 * totalCount++; sumRel+=bestRel; counter[bestRel==1? n-1 : (int)
	 * Math.floor(n*bestRel)]++; } } }
	 * 
	 * System.out.println("Avg. rel for "+annotator.getName()+"="+sumRel/(double)
	 * totalCount);
	 * 
	 * String suffix = annotator.getName().replaceAll("[^a-zA-Z0-9]",
	 * "").toLowerCase()+"_"+ds.getName().replaceAll("[^a-zA-Z0-9]",
	 * "").toLowerCase() +".dat"; OutputStreamWriter relOs = new
	 * OutputStreamWriter(new FileOutputStream("fp_relatedness_"+suffix)); for
	 * (int i=0; i<counter.length; i++){
	 * //System.out.printf("%.2f-%.2f: %d%n",(float)i/(float)n,
	 * (float)(i+1)/(float)n, counter[i]); //relOs.write((float)i/(float)n +
	 * "\t" + counter[i]+"\n"); int partSum = 0; for (int j=i; j<counter.length;
	 * j++) partSum+=counter[j]; relOs.write((float)i/(float)n + "\t"
	 * +(float)partSum/(float)totalCount +"\n"); } relOs.close();
	 * 
	 * }
	 * 
	 * 
	 * }
	 */

	/**
	 * Writes (as {@code .dat} gnuplot data) the micro-F1, micro-precision and
	 * micro-recall achieved for each combination of Match relation - Annotator
	 * - Dataset, varying the score threshold in [0,1], to a file in the form
	 * matchrelation_datasetname_f1_threshold_annotatorname.dat. Note that this
	 * will generate
	 * {@code 3 * matchRels.size() * annotators.size() * dss.size()} files. Also
	 * note that the experiments must have already been performed and their
	 * results stored in {@code threshRecords}.
	 * 
	 * @param matchRels
	 *            the set of Match relations.
	 * @param annotators
	 *            the set of annotators.
	 * @param dss
	 *            the set of datasets.
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends TopicSystem, T2 extends Tag, T3 extends TopicDataset> void gnuplotCorrectnessPerformance(
			Vector<MatchRelation<T2>> matchRels,
			List<T1> annotators,
			Vector<T3> dss,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords)
			throws IOException {

		for (MatchRelation<T2> m : matchRels)
			for (T3 ds : dss) {
				for (T1 t : annotators) {
					System.out.println("Writing to gnuplot-file "
							+ ds.getName() + "/" + t.getName()
							+ " with varying score threshold...");
					String prefix = CHARTS_DIR
							+ m.getName().replaceAll("[^a-zA-Z0-9]", "")
									.toLowerCase();
					String suffix = t.getName().replaceAll("[^a-zA-Z0-9]", "")
							.toLowerCase()
							+ "_"
							+ ds.getName().replaceAll("[^a-zA-Z0-9]", "")
									.toLowerCase() + ".dat";

					OutputStreamWriter precOs = new OutputStreamWriter(
							new FileOutputStream(prefix
									+ "_precision_threshold_" + suffix));
					OutputStreamWriter recOs = new OutputStreamWriter(
							new FileOutputStream(prefix + "_recall_threshold_"
									+ suffix));
					OutputStreamWriter f1Os = new OutputStreamWriter(
							new FileOutputStream(prefix + "_f1_threshold_"
									+ suffix));

					HashMap<Float, MetricsResultSet> records = RunExperiments
							.getRecords(threshRecords, m.getName(),
									t.getName(), ds.getName());
					List<Float> thresholds = new Vector<Float>(records.keySet());
					Collections.sort(thresholds);

					for (Float thr : thresholds) {
						MetricsResultSet rs = records.get(thr);
						System.out.printf(
								LOCALE,
								t.getName() + " - " + ds.getName() + " " + thr
										+ " tp:" + rs.getGlobalTp() + " fp:"
										+ rs.getGlobalFp() + " fn:"
										+ rs.getGlobalFn() + " prec:"
										+ rs.getMicroPrecision() + " rec:"
										+ rs.getMicroRecall() + " f1:"
										+ rs.getMicroF1() + "\n");
						precOs.write(String.format(LOCALE, "%f\t%f%n", thr,
								rs.getMicroPrecision()));
						recOs.write(String.format(LOCALE, "%f\t%f%n", thr,
								rs.getMicroRecall()));
						f1Os.write(String.format(LOCALE, "%f\t%f%n", thr,
								rs.getMicroF1()));
					}
					precOs.close();
					recOs.close();
					f1Os.close();
				}
			}
		System.out.println("Flushing Wikipedia API cache...");
		api.flush();
	}

	/**
	 * Writes a file {@code runtime_f1.dat} storing (avg. runtime, achieved best
	 * micro-F1) pairs for a single dataset and a single match relation. Note
	 * that the experiments must have already been performed and their results
	 * stored in {@code threshRecords}.
	 * 
	 * @param matchRelName
	 *            The match relation used to compute the micro-F1.
	 * @param a2wAnnotators
	 *            The set of A2W Annotators for which a pair must be included in
	 *            the output.
	 * @param sa2wAnnotators
	 *            The set of Sa2W Annotators for which a pair must be included.
	 * @param datasetName
	 *            The name of the dataset for which the pairs must be included.
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 * @throws Exception
	 *             if the cache does not contain records about the given
	 *             dataset/annotator avg. time.
	 */
	public static void gnuplotRuntimeF1(
			String matchRelName,
			Vector<A2WSystem> a2wAnnotators,
			Vector<Sa2WSystem> sa2wAnnotators,
			String datasetName,
			WikipediaApiInterface api,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords)
			throws IOException, Exception {
		OutputStreamWriter runTimeF1Stream = new OutputStreamWriter(
				new FileOutputStream(CHARTS_DIR + "runtime_f1.dat"));
		if (a2wAnnotators != null)
			for (A2WSystem t : a2wAnnotators)
				runTimeF1Stream.write(String.format(LOCALE, "%f\t%d\t\"%s\"\n",
						RunExperiments.getBestRecord(threshRecords,
								matchRelName, t.getName(), datasetName).second
								.getMicroF1(), (int) BenchmarkCache
								.getAvgA2WTimingsForDataset(t.getName(),
										datasetName), t.getName()));
		if (sa2wAnnotators != null)
			for (Sa2WSystem t : sa2wAnnotators)
				runTimeF1Stream.write(String.format(LOCALE, "%f\t%d\t\"%s\"\n",
						RunExperiments.getBestRecord(threshRecords,
								matchRelName, t.getName(), datasetName).second
								.getMicroF1(), (int) BenchmarkCache
								.getAvgSa2WTimingsForDataset(t.getName(),
										datasetName), t.getName()));
		runTimeF1Stream.close();
	}

	/**
	 * Print a latex tables reporting the best micro-F1 achieved by each
	 * (annotator, dataset) pair. A Latex table is printed for the metrics based
	 * on each match relation passed in {@code matchRels}.
	 * 
	 * @param matchRels
	 *            the set of Match relations for which a table will be printed.
	 * @param a2wAnnotators
	 *            The set of A2W Annotators for which the best result will be
	 *            included in the output.
	 * @param sa2wAnnotators
	 *            The set of Sa2W Annotators for which the best result will be
	 *            included.
	 * @param sc2wAnnotator
	 *            The set of Sc2W Annotators for which the best result will be
	 *            included.
	 * @param dss
	 *            The datasets for which the best result will be included.
	 * @param extended
	 *            true if the output table has to include the total number of
	 *            tp/fp/fn
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 */
	public static <T extends Tag, D extends TopicDataset> void latexCorrectnessPerformance(
			Vector<MatchRelation<T>> matchRels,
			Vector<A2WSystem> a2wAnnotator,
			Vector<Sa2WSystem> sa2wAnnotator,
			Vector<Sc2WSystem> sc2wAnnotator,
			Vector<D> dss,
			boolean extended,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords) {
		System.out.println("Correctness performance - latex output");
		Vector<C2WSystem> allAnns = new Vector<C2WSystem>();
		if (sa2wAnnotator != null)
			allAnns.addAll(sa2wAnnotator);
		if (sc2wAnnotator != null)
			allAnns.addAll(sc2wAnnotator);
		if (a2wAnnotator != null)
			allAnns.addAll(a2wAnnotator);
		for (MatchRelation<T> m : matchRels) {
			System.out.println("+++ Match Relation: " + m.getName());
			System.out
					.printf(LOCALE,
							"\\hline \n Dataset & Annotator & Best Threshold & $F1_{micro}$ & $P_{micro}$ & $R_{micro}$ "
									+ (extended ? "& tp & fp & fn" : "")
									+ "\\\\ \n \\hline%n");
			for (TopicDataset d : dss) {
				long len = 0;
				for (String s : d.getTextInstanceList())
					len += s.length();
				System.out
						.printf(LOCALE,
								"\\multirow{%d}{*}{\\parbox{.20\\textwidth}{%s \\newline(avg-len\\newline %d chars)}} %n",
								allAnns.size(), d.getName(),
								(int) ((float) len / (float) d.getSize()));
				for (C2WSystem t : allAnns) {
					Pair<Float, MetricsResultSet> values = RunExperiments
							.getBestRecord(threshRecords, m.getName(),
									t.getName(), d.getName());
					System.out.printf(LOCALE,
							"& %s & $%.3f$ & $%.1f$ & $%.1f$ & $%.1f$ ",
							t.getName(), values.first,
							values.second.getMicroF1() * 100f,
							values.second.getMicroPrecision() * 100f,
							values.second.getMicroRecall() * 100f);
					if (extended)
						System.out.printf(LOCALE, "& $%d$ & $%d$ & $%d$",
								values.second.getGlobalTp(),
								values.second.getGlobalFp(),
								values.second.getGlobalFn());
					System.out.printf(LOCALE, " \\\\ \\cline{2-"
							+ (extended ? "9" : "6") + "}%n");
				}
				System.out.printf(LOCALE, "\\hline%n");
			}
		}
	}

	/**
	 * Print a set of latex tables reporting the similarity between annotators
	 * in solving the Sa2W problem, for each dataset given in {@code dssA2W}. A
	 * table will be printed for each of these focuses:<br>
	 * 1- similarity of the whole output ("wholeOutput")<br>
	 * 2- similarity of the output, restricted to the true positives ("TPonly")<br>
	 * 3- similarity of the mentions ("mention")<br>
	 * 4- similarity of the concepts ("concept")
	 * 
	 * @param dssA2W
	 *            the datasets for which the set of tables will be printed.
	 * @param sa2wAnnotators
	 *            the Sa2W annotators whose similarity will be printed.
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @throws Exception
	 *             if anything went wrong while retrieving the results.
	 */
	public static <D extends A2WDataset> void latexSimilarityA2W(
			Vector<D> dssA2W,
			Vector<Sa2WSystem> sa2wAnnotators,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords,
			WikipediaApiInterface api) throws Exception {
		/** Difference between systems & "Jaccard" measure */
		for (String focus : new String[] { "wholeOutput", "TPonly", "mention",
				"concept", "mention/concept" }) {
			System.out.println("Similarity measures - latex output - " + focus);
			for (A2WDataset ds : dssA2W) {
				System.out.println("Dataset: " + ds.getName());

				for (Sa2WSystem t : sa2wAnnotators)
					System.out.printf(LOCALE, "&%s", t.getName());
				System.out.printf(LOCALE, "\\\\%n");
				System.out.printf(LOCALE, "\\hline%n");

				for (int i = 0; i < sa2wAnnotators.size(); i++) {
					Sa2WSystem t1 = sa2wAnnotators.get(i);
					System.out.printf(LOCALE, t1.getName());

					for (int j = 0; j < sa2wAnnotators.size(); j++) {
						if (j < i) {
							System.out.printf(LOCALE, "&");
							continue;
						}

						Sa2WSystem t2 = sa2wAnnotators.get(j);
						List<Set<ScoredAnnotation>> t1Annotations = BenchmarkCache
								.doSa2WAnnotations(t1, ds, null, 0);
						List<Set<ScoredAnnotation>> t2Annotations = BenchmarkCache
								.doSa2WAnnotations(t2, ds, null, 0);
						List<Set<Annotation>> out1 = null;
						List<Set<Annotation>> out2 = null;
						MatchRelation<Annotation> m = null;
						Metrics<Annotation> metrics = new Metrics<Annotation>();
						if (focus.equals("wholeOutput")) {
							m = new WeakAnnotationMatch(api);
							out1 = ProblemReduction.Sa2WToA2WList(
									t1Annotations, RunExperiments
											.getBestRecord(threshRecords,
													m.getName(), t1.getName(),
													ds.getName()).first);
							out2 = ProblemReduction.Sa2WToA2WList(
									t2Annotations, RunExperiments
											.getBestRecord(threshRecords,
													m.getName(), t2.getName(),
													ds.getName()).first);
							System.out
									.printf(LOCALE, "&$%.1f$",
											metrics.macroSimilarity(out1, out2,
													m) * 100);
						} else if (focus.equals("TPonly")) {
							m = new WeakAnnotationMatch(api);
							List<Set<Annotation>> reducedT1Tags = ProblemReduction
									.Sa2WToA2WList(
											t1Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t1.getName(), ds.getName()).first);
							List<Set<Annotation>> reducedT2Tags = ProblemReduction
									.Sa2WToA2WList(
											t2Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t2.getName(), ds.getName()).first);
							out1 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT1Tags, m);
							out2 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT2Tags, m);
							System.out
									.printf(LOCALE, "&$%.1f$",
											metrics.macroSimilarity(out1, out2,
													m) * 100);
						} else if (focus.equals("mention")) {
							m = new MentionAnnotationMatch();
							List<Set<Annotation>> reducedT1Tags = ProblemReduction
									.Sa2WToA2WList(
											t1Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t1.getName(), ds.getName()).first);
							List<Set<Annotation>> reducedT2Tags = ProblemReduction
									.Sa2WToA2WList(
											t2Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t2.getName(), ds.getName()).first);
							out1 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT1Tags, m);
							out2 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT2Tags, m);
							System.out
									.printf(LOCALE, "&$%.1f$",
											metrics.macroSimilarity(out1, out2,
													m) * 100);
						} else if (focus.equals("concept")) {
							m = new ConceptAnnotationMatch(api);
							List<Set<Annotation>> reducedT1Tags = ProblemReduction
									.Sa2WToA2WList(
											t1Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t1.getName(), ds.getName()).first);
							List<Set<Annotation>> reducedT2Tags = ProblemReduction
									.Sa2WToA2WList(
											t2Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t2.getName(), ds.getName()).first);
							out1 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT1Tags, m);
							out2 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT2Tags, m);
							System.out
									.printf(LOCALE, "&$%.1f$",
											metrics.macroSimilarity(out1, out2,
													m) * 100);
						} else if (focus.equals("mention/concept")) {
							m = new MentionAnnotationMatch();
							List<Set<Annotation>> reducedT1Tags = ProblemReduction
									.Sa2WToA2WList(
											t1Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t1.getName(), ds.getName()).first);
							List<Set<Annotation>> reducedT2Tags = ProblemReduction
									.Sa2WToA2WList(
											t2Annotations,
											RunExperiments.getBestRecord(
													threshRecords, m.getName(),
													t2.getName(), ds.getName()).first);
							out1 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT1Tags, m);
							out2 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT2Tags, m);
							m = new ConceptAnnotationMatch(api);
							System.out
									.printf(LOCALE, "&$%.0f$/",
											metrics.macroSimilarity(out1, out2,
													m) * 100);
							reducedT1Tags = ProblemReduction.Sa2WToA2WList(
									t1Annotations, RunExperiments
											.getBestRecord(threshRecords,
													m.getName(), t1.getName(),
													ds.getName()).first);
							reducedT2Tags = ProblemReduction.Sa2WToA2WList(
									t2Annotations, RunExperiments
											.getBestRecord(threshRecords,
													m.getName(), t2.getName(),
													ds.getName()).first);
							out1 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT1Tags, m);
							out2 = metrics.getTp(ds.getA2WGoldStandardList(),
									reducedT2Tags, m);
							System.out
									.printf(LOCALE, "$%.0f$",
											metrics.macroSimilarity(out1, out2,
													m) * 100);
						}

					}
					System.out.printf(LOCALE, "\\\\%n");
					System.out.printf(LOCALE, "\\hline%n");
				}
			}
		}
	}

	/**
	 * Print a set of latex tables reporting the similarity between annotators
	 * in solving the C2W problem, for each dataset given in {@code dssC2W}.<br>
	 * 
	 * @param dssC2W
	 *            the datasets for which the set of tables will be printed.
	 * @param sa2wAnnotators
	 *            the Sa2W annotators whose similarity will be printed.
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @throws Exception
	 *             if anything went wrong while retrieving the results.
	 */
	public static void latexSimilarityC2W(
			Vector<C2WDataset> dssC2W,
			Vector<Sa2WSystem> sa2wAnnotators,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords,
			WikipediaApiInterface api) throws Exception {
		System.out.println("Similarity measures - latex output - tp C2W");
		for (C2WDataset ds : dssC2W) {
			System.out.println("Dataset: " + ds.getName());

			for (Sa2WSystem t : sa2wAnnotators)
				System.out.printf(LOCALE, "&" + t.getName());
			System.out.printf(LOCALE, "\\\\%n");
			System.out.printf(LOCALE, "\\hline%n");

			for (int i = 0; i < sa2wAnnotators.size(); i++) {
				Sa2WSystem t1 = sa2wAnnotators.get(i);
				System.out.printf(LOCALE, t1.getName());

				for (int j = 0; j < sa2wAnnotators.size(); j++) {
					if (j < i) {
						System.out.printf(LOCALE, "&");
						continue;
					}

					Sa2WSystem t2 = sa2wAnnotators.get(j);
					MatchRelation<Tag> m = new StrongTagMatch(api);
					Metrics<Tag> metrics = new Metrics<Tag>();
					List<Set<ScoredAnnotation>> t1Annotations = BenchmarkCache
							.doSa2WAnnotations(t1, ds, null, 0);
					List<Set<ScoredAnnotation>> t2Annotations = BenchmarkCache
							.doSa2WAnnotations(t2, ds, null, 0);
					List<Set<Annotation>> reducedT1Anns = ProblemReduction
							.Sa2WToA2WList(t1Annotations, RunExperiments
									.getBestRecord(threshRecords, m.getName(),
											t1.getName(), ds.getName()).first);
					List<Set<Annotation>> reducedT2Anns = ProblemReduction
							.Sa2WToA2WList(t2Annotations, RunExperiments
									.getBestRecord(threshRecords, m.getName(),
											t2.getName(), ds.getName()).first);
					List<Set<Tag>> reducedT1Tags = ProblemReduction
							.A2WToC2WList(reducedT1Anns);
					List<Set<Tag>> reducedT2Tags = ProblemReduction
							.A2WToC2WList(reducedT2Anns);
					List<Set<Tag>> tponlyTagsT1 = metrics.getTp(
							ds.getC2WGoldStandardList(), reducedT1Tags, m);
					List<Set<Tag>> tponlyTagsT2 = metrics.getTp(
							ds.getC2WGoldStandardList(), reducedT2Tags, m);

					System.out.printf(LOCALE, "&$%.3f$", metrics
							.macroSimilarity(tponlyTagsT1, tponlyTagsT2, m));
				}
				System.out.printf(LOCALE, "\\\\%n");
				System.out.printf(LOCALE, "\\hline%n");
			}
		}
	}

	/**
	 * Prints (in latex form) a table representing the average time needed by
	 * all systems to solve the instance of a dataset. Rows are grouped by
	 * dataset and are in the form (dataset, annotator, avg. time).
	 * 
	 * @param a2wAnnotators
	 *            The A2W annotators that will be included in the output.
	 * @param sa2wAnnotators
	 *            The Sa2W annotators that will be included in the output.
	 * @param sc2wAnnotators
	 *            The Sc2W annotators that will be included in the output.
	 * @param dss
	 *            The datasets that will be included in the output.
	 * @throws Exception
	 *             if the cache does not contain records about the given
	 *             dataset/annotator avg. time.
	 */
	public static <D extends TopicDataset> void latexTimingPerformance(
			Vector<A2WSystem> a2wAnnotators, Vector<Sa2WSystem> sa2wAnnotators,
			Vector<Sc2WSystem> sc2wAnnotators, Vector<D> dss) throws Exception {
		System.out.println("Timing performance - latex output");

		int allAnnsSize = 0;
		if (sa2wAnnotators != null)
			allAnnsSize += sa2wAnnotators.size();
		if (a2wAnnotators != null)
			allAnnsSize += a2wAnnotators.size();
		if (sc2wAnnotators != null)
			allAnnsSize += sc2wAnnotators.size();

		System.out.printf(LOCALE,
				"\\hline \nDataset & Tagger & Average Time\\\\ \n \\hline%n");
		for (TopicDataset d : dss) {
			long len = 0;
			for (String s : d.getTextInstanceList())
				len += s.length();
			System.out
					.printf(LOCALE,
							"\\multirow{%d}{*}{\\parbox{.40\\textwidth}{%s \\newline(len %d)}} %n",
							allAnnsSize, d.getName(),
							(int) ((float) len / (float) d.getSize()));
			if (a2wAnnotators != null)
				for (A2WSystem t : a2wAnnotators)
					System.out.printf(
							LOCALE,
							"& %s & $%d$ms \\\\ \\cline{2-3}%n",
							t.getName(),
							(int) BenchmarkCache.getAvgA2WTimingsForDataset(
									t.getName(), d.getName()));
			if (sa2wAnnotators != null)
				for (Sa2WSystem t : sa2wAnnotators)
					System.out.printf(
							LOCALE,
							"& %s & $%d$ms \\\\ \\cline{2-3}%n",
							t.getName(),
							(int) BenchmarkCache.getAvgSa2WTimingsForDataset(
									t.getName(), d.getName()));
			System.out.printf(LOCALE, "\\hline%n");

		}

	}

	/**
	 * Prints (in latex form) a table representing the average time needed by a
	 * system to solve the instance of a dataset. Rows are in the form
	 * (annotator, avg. time for system 1, avg. time for system 2, ...).
	 * 
	 * @param sa2wAnnotators
	 *            The Sa2W annotators that will be included in the output.
	 * @param dss
	 *            The datasets that will be included in the output.
	 * @throws Exception
	 *             if the cache does not contain records about the given
	 *             dataset/annotator avg. time.
	 */
	public static <D extends TopicDataset> void latexTimingPerformance2(
			Vector<Sa2WSystem> sa2wTaggers, Vector<Sc2WSystem> sc2wTaggers,
			Vector<D> dss) throws Exception {
		System.out.println("Timing performance - latex output 2");

		System.out.printf(LOCALE, "\\hline \nSystem ");
		for (TopicDataset ds : dss)
			System.out.printf(LOCALE, " & " + ds.getName());
		System.out.printf(LOCALE, " \n \\hline%n");

		if (sa2wTaggers != null)
			for (Sa2WSystem t : sa2wTaggers) {
				System.out.printf(LOCALE, t.getName());
				for (TopicDataset d : dss)
					System.out.printf(
							LOCALE,
							" & $%d$ ",
							(int) BenchmarkCache.getAvgSa2WTimingsForDataset(
									t.getName(), d.getName()));
				System.out.printf(LOCALE, "\\\\%n");
				System.out.printf(LOCALE, "\\hline%n");

			}
		if (sc2wTaggers != null)
			for (Sc2WSystem t : sc2wTaggers) {
				System.out.printf(LOCALE, t.getName());
				for (TopicDataset d : dss)
					System.out.printf(
							LOCALE,
							" & $%d$ ",
							(int) BenchmarkCache.getAvgSa2WTimingsForDataset(
									t.getName(), d.getName()));
				System.out.printf(LOCALE, "\\\\%n");
				System.out.printf(LOCALE, "\\hline%n");

			}

	}

	/**
	 * Print the best micro- and macro- measures achieved by each (annotator,
	 * dataset) pair along with other data. Data is printed for the metrics
	 * based on each match relation passed in {@code matchRels}.
	 * 
	 * @param matchRels
	 *            the set of Match relations for which a table will be printed.
	 * @param a2wAnnotators
	 *            The set of A2W Annotators for which the best result will be
	 *            included in the output.
	 * @param sa2wAnnotators
	 *            The set of Sa2W Annotators for which the best result will be
	 *            included.
	 * @param c2wAnnotators
	 *            The set of Sc2W Annotators for which the best result will be
	 *            included.
	 * @param dss
	 *            The datasets for which the best result will be included.
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 */
	public static <T extends Tag, D extends TopicDataset> void printCorrectnessPerformance(
			Vector<MatchRelation<T>> matchRels,
			Vector<A2WSystem> a2wAnnotators,
			Vector<Sa2WSystem> sa2wAnnotators,
			Vector<Sc2WSystem> c2wAnnotators,
			Vector<D2WSystem> d2wAnnotators,
			Vector<D> dss,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> threshRecords) {
		Vector<TopicSystem> allAnns = new Vector<>();
		if (sa2wAnnotators != null)
			allAnns.addAll(sa2wAnnotators);
		if (c2wAnnotators != null)
			allAnns.addAll(c2wAnnotators);
		if (a2wAnnotators != null)
			allAnns.addAll(a2wAnnotators);
		if (d2wAnnotators != null)
			allAnns.addAll(d2wAnnotators);
		System.out.println("Correctness performance [F1/prec/rec]");
		for (MatchRelation<T> metric : matchRels) {
			System.out.printf(LOCALE, "Best results (metrics: %s):%n",
					metric.getName());
			for (TopicSystem t : allAnns)
				for (TopicDataset d : dss) {
					Pair<Float, MetricsResultSet> result = RunExperiments
							.getBestRecord(threshRecords, metric.getName(),
									t.getName(), d.getName());
					System.out
							.printf(LOCALE,
									"%s\t%s\t%.3f\t[mic: %.3f\t%.3f\t%.3f] [mac: %.3f\t%.3f\t%.3f]%n",
									d.getName(), t.getName(), result.first,
									result.second.getMicroF1(),
									result.second.getMicroPrecision(),
									result.second.getMicroRecall(),
									result.second.getMacroF1(),
									result.second.getMacroPrecision(),
									result.second.getMacroRecall());
				}
		}
	}

	/**
	 * Prints the similarity, dissimilarity, and union of the results for each
	 * pair of annotators.
	 * 
	 * @param dssA2W
	 *            The datasets for which the data will be printed.
	 * @param sa2wAnnotators
	 *            The annotators for which the data will be printed.
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param threshRecords
	 *            the hashmap in the form metric -> annotator -> dataset ->
	 *            (threshold, results) where the results are stored.
	 * @throws Exception
	 *             if something went wrong while retrieving the results.
	 */
	public static void printDissimilarityA2W(
			Vector<A2WDataset> dssA2W,
			Vector<Sa2WSystem> sa2wAnnotators,
			HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> bestThresholds,
			WikipediaApiInterface api) throws Exception {
		Metrics<Annotation> metrics = new Metrics<Annotation>();
		WeakAnnotationMatch m = new WeakAnnotationMatch(api);

		for (A2WDataset ds : dssA2W) {
			System.out.println("Dataset: " + ds.getName());

			for (int i = 0; i < sa2wAnnotators.size(); i++) {
				Sa2WSystem t1 = sa2wAnnotators.get(i);

				for (int j = 0; j < sa2wAnnotators.size(); j++) {
					if (j <= i)
						continue;
					Sa2WSystem t2 = sa2wAnnotators.get(j);
					System.out.println("Annotator1: " + t1.getName());
					System.out.println("Annotator2: " + t2.getName());

					List<Set<ScoredAnnotation>> t1Annotations = BenchmarkCache
							.doSa2WAnnotations(t1, ds, null, 0);
					List<Set<ScoredAnnotation>> t2Annotations = BenchmarkCache
							.doSa2WAnnotations(t2, ds, null, 0);

					List<Set<Annotation>> reducedT1Tags = ProblemReduction
							.Sa2WToA2WList(t1Annotations, RunExperiments
									.getBestRecord(bestThresholds, m.getName(),
											t1.getName(), ds.getName()).first);
					List<Set<Annotation>> reducedT2Tags = ProblemReduction
							.Sa2WToA2WList(t2Annotations, RunExperiments
									.getBestRecord(bestThresholds, m.getName(),
											t2.getName(), ds.getName()).first);

					List<Set<Annotation>> out1Tp = metrics.getTp(
							ds.getA2WGoldStandardList(), reducedT1Tags, m);
					List<Set<Annotation>> out2Tp = metrics.getTp(
							ds.getA2WGoldStandardList(), reducedT2Tags, m);
					List<Set<Annotation>> out1Fp = metrics.getFp(
							ds.getA2WGoldStandardList(), reducedT1Tags, m);
					List<Set<Annotation>> out2Fp = metrics.getFp(
							ds.getA2WGoldStandardList(), reducedT2Tags, m);

					long tpUnion = metrics.listUnion(out1Tp, out2Tp, m);
					int tpdiss1 = metrics.dissimilarityListCount(out1Tp,
							out2Tp, m);
					int tpdiss2 = metrics.dissimilarityListCount(out2Tp,
							out1Tp, m);
					int tpsim = metrics.similarityListCount(out1Tp, out2Tp, m);

					long fpUnion = metrics.listUnion(out1Fp, out2Fp, m);
					int fpdiss1 = metrics.dissimilarityListCount(out1Fp,
							out2Fp, m);
					int fpdiss2 = metrics.dissimilarityListCount(out2Fp,
							out1Fp, m);
					int fpsim = metrics.similarityListCount(out1Fp, out2Fp, m);

					System.out.printf(LOCALE,
							"Ann1 dissimilarity tp/fp: %d(%.2f)/%d(%.2f)%n",
							tpdiss1, (float) tpdiss1
									/ (float) (tpdiss1 + fpdiss1), fpdiss1,
							(float) fpdiss1 / (float) (tpdiss1 + fpdiss1));
					System.out.printf(LOCALE,
							"Ann2 dissimilarity tp/fp: %d(%.2f)/%d(%.2f)%n",
							tpdiss2, (float) tpdiss2
									/ (float) (tpdiss2 + fpdiss2), fpdiss2,
							(float) fpdiss2 / (float) (tpdiss2 + fpdiss2));
					System.out.printf(LOCALE,
							"Anns similarity tp/fp: %d(%.2f)/%d(%.2f)%n",
							tpsim, (float) tpsim / (float) (tpsim + fpsim),
							fpsim, (float) fpsim / (float) (tpsim + fpsim));
					System.out.printf(LOCALE,
							"Anns union tp/fp: %d(%.2f)/%d(%.2f)%n", tpUnion,
							(float) tpUnion / (float) (tpUnion + fpUnion),
							fpUnion, (float) fpUnion
									/ (float) (tpUnion + fpUnion));

				}
			}
		}
	}

	/**
	 * Prints the document in a dataset with the most redirect documents for
	 * each (dataset, annotator) pair. (Mainly for debug purposes.)
	 * 
	 * @param dss
	 *            The datasets.
	 * @param sa2wAnnotators
	 *            The annotators.
	 * @param api
	 *            the API to Wikipedia (needed to find out if an
	 *            annotations/tags is a redirect).
	 * @throws Exception
	 *             if something went wrong while retrieving the results.
	 */
	public static void printMostRedirectDocument(Vector<A2WDataset> dss,
			Vector<Sa2WSystem> sa2wAnnotators, WikipediaApiInterface api)
			throws Exception {
		System.out.println("Returned Redirect");
		for (A2WDataset ds : dss) {
			for (Sa2WSystem t : sa2wAnnotators) {
				int totalTags = 0;
				int totalRedirects = 0;
				System.out
						.printf(LOCALE,
								"Finding document with most redirect tags for dataset:%s tagger:%s%n",
								ds.getName(), t.getName());
				List<Set<ScoredAnnotation>> compRes = BenchmarkCache
						.doSa2WAnnotations(t, ds, null, 0);
				int bestCount = -1;

				for (int i = 0; i < compRes.size(); i++) {
					Set<ScoredAnnotation> comp = compRes.get(i);
					int count = 0;
					Set<Integer> distinctRedirects = new HashSet<Integer>();
					for (ScoredAnnotation tag : comp)
						if (api.isRedirect(tag.getConcept())) {
							count++;
							distinctRedirects.add(tag.getConcept());
						}
					if (bestCount < distinctRedirects.size())
						bestCount = distinctRedirects.size();
					totalTags += compRes.size();
					totalRedirects += count;
				}
				System.out.printf(LOCALE,
						"Tags found=%d Redirects=%d redirect/total=%.3f",
						totalTags, totalRedirects, (float) totalRedirects
								/ (float) totalTags);
			}
		}
	}

	/**
	 * Print data about correctness performance (f1, precision, recall, ...) for
	 * a given (annotator, dataset) pair.
	 * 
	 * @param ann
	 *            the annotator.
	 * @param m
	 *            the match relation which the measurements are based upon.
	 * @param goldStandard
	 *            the gold standard for the dataset.
	 * @param output
	 *            the output found by the tagger.
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <E extends Tag> void printCorrectnessPerformance(
			TopicSystem ann, MatchRelation<E> m, List<Set<E>> goldStandard,
			List<Set<E>> output, WikipediaApiInterface api) throws IOException {
		Metrics<E> metrics = new Metrics<E>();
		MetricsResultSet rs = metrics.getResult(output, goldStandard, m);
		System.out.format(LOCALE,
				"%s tp:%d fp:%d fn:%d precision:%.3f recall:%.3f F1:%.3f%n",
				ann.getName(), rs.getGlobalTp(), rs.getGlobalFp(),
				rs.getGlobalFn(), rs.getMicroPrecision(), rs.getMicroRecall(),
				rs.getMicroF1());
		System.out.format(LOCALE,
				"%s macro-precision: %.3f macro-recall:%.3f macro-F1:%.3f%n",
				ann.getName(), rs.getMacroPrecision(), rs.getMacroRecall(),
				rs.getMacroF1());

	}

	/**
	 * Prints a table representing the average time needed by all systems to
	 * solve the instance of a dataset. Rows are grouped by dataset and are in
	 * the form (dataset, annotator, avg. time).
	 * 
	 * @param a2wAnnotators
	 *            The A2W annotators that will be included in the output.
	 * @param sa2wAnnotators
	 *            The Sa2W annotators that will be included in the output.
	 * @param sc2wAnnotators
	 *            The Sa2W annotators that will be included in the output.
	 * @param dss
	 *            The datasets that will be included in the output.
	 * @throws Exception
	 *             if the cache does not contain records about the given
	 *             dataset/annotator avg. time.
	 */
	public static void printTimingPerformance(Vector<A2WSystem> a2wAnnotators,
			Vector<Sa2WSystem> sa2wAnnotators,
			Vector<Sc2WSystem> sc2wAnnotators, Vector<A2WDataset> dss)
			throws Exception {
		if (sa2wAnnotators == null) sa2wAnnotators = new Vector<Sa2WSystem>();
		if (a2wAnnotators == null) a2wAnnotators = new Vector<A2WSystem>();
		if (sc2wAnnotators == null) sc2wAnnotators = new Vector<Sc2WSystem>();
		System.out.println("Timing performance:");
		for (A2WDataset d : dss) {
			for (A2WSystem t : a2wAnnotators) {
				System.out.printf(LOCALE,
						"Average time for T2W tagger=%s dataset=%s: %.3f%n", t
								.getName(), d.getName(), BenchmarkCache
								.getAvgA2WTimingsForDataset(t.getName(),
										d.getName()));
				if (d.getSize() != BenchmarkCache.getA2WTimingsForDataset(
						t.getName(), d.getName()).size())
					System.out
							.printf(LOCALE,
									"ERROR: size of dataset %s and computed results by %s mismatch! %d != %d",
									d.getName(),
									t.getName(),
									d.getSize(),
									BenchmarkCache.getA2WTimingsForDataset(
											t.getName(), d.getName()).size());
			}
			for (Sa2WSystem t : sa2wAnnotators) {
				System.out.printf(LOCALE,
						"Average time for St2W tagger=%s dataset=%s: %.3f%n", t
								.getName(), d.getName(), BenchmarkCache
								.getAvgSa2WTimingsForDataset(t.getName(),
										d.getName()));
				if (d.getSize() != BenchmarkCache.getSa2WTimingsForDataset(
						t.getName(), d.getName()).size())
					System.out
							.printf(LOCALE,
									"ERROR: size of dataset %s and computed results by %s mismatch! %d != %d",
									d.getName(),
									t.getName(),
									d.getSize(),
									BenchmarkCache.getSa2WTimingsForDataset(
											t.getName(), d.getName()).size());
			}
		}
	}

	/**
	 * For each dataset passed in {@code annotators}, writes one gnuplot
	 * {@code timing_annotator_dataset.dat} file containing the time needed by
	 * the annotator to annotate each document of a dataset, ordered by the
	 * amount of time.<br>
	 * Note that the experiments must have already been performed and their
	 * results stored in the cache when this method is called.
	 * 
	 * @param annotators
	 *            the set of annotators for which a file will be created.
	 * @param ds
	 *            the dataset.
	 * @throws Exception
	 *             if there were errors in writing the file or in retrieving the
	 *             timing.
	 */
	public static void gnuplotTraceTiming(Vector<Sa2WSystem> annotators,
			A2WDataset ds) throws Exception {
		for (Sa2WSystem annotator : annotators) {
			String suffix = annotator.getName().replaceAll("[^a-zA-Z0-9]", "")
					.toLowerCase()
					+ "_"
					+ ds.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase()
					+ ".dat";
			OutputStreamWriter relOs = new OutputStreamWriter(
					new FileOutputStream(CHARTS_DIR + "timing_" + suffix));

			Vector<String> texts = new Vector<String>(ds.getTextInstanceList());
			Collections.sort(texts, new StringLengthComparator());
			for (String text : texts) {
				long time = BenchmarkCache.getSa2WTiming(annotator.getName(),
						ds.getName(), text);
				relOs.write(String.format(LOCALE, "%d\t%d%n", text.length(),
						time));
			}
			relOs.close();
		}
	}
}
