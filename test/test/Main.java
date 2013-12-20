/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.*;

import it.acubelab.batframework.cache.BenchmarkCache;
import it.acubelab.batframework.cache.BenchmarkResults;
import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.ScoredAnnotation;
import it.acubelab.batframework.datasetPlugins.ConllAidaDataset;
import it.acubelab.batframework.metrics.Metrics;
import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.metrics.StrongAnnotationMatch;
import it.acubelab.batframework.metrics.StrongTagMatch;
import it.acubelab.batframework.problems.A2WDataset;
import it.acubelab.batframework.problems.Sa2WSystem;
import it.acubelab.batframework.systemPlugins.TagmeAnnotator;
import it.acubelab.batframework.systemPlugins.WikipediaMinerAnnotator;
import it.acubelab.batframework.utils.DumpData;
import it.acubelab.batframework.utils.ProblemReduction;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class Main {


	public static void main(String[] args) throws Exception {
		
		/*BenchmarkCache.useCache("benchmark/cache/results.cache");
		
		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");

		Sa2WSystem tagme = new TagmeAnnotator("benchmark/configs/tagme/config.xml");
		
		A2WDataset aidaDs = new ConllAidaDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset.tsv", api);
		
		String text = aidaDs.getTextInstanceList().get(0);
		
		//System.out.println("Text:\n"+text+"\n\n");
		
		Set<Mention> mentions = aidaDs.getMentionsInstanceList().get(0);
		
		for (Mention m: mentions)
			System.out.println("\t"+m.getPosition()+" "+m.getLength()+" "+text.substring(m.getPosition(), m.getPosition()+m.getLength()));

		Set<Annotation> anns = ProblemReduction.Sa2WToD2W(tagme.solveSa2W(text), mentions, .5f);
		Set<Annotation> gs = aidaDs.getD2WGoldStandardList().get(0);*/
		/*String text = "Bill clinton's thought about the Vietnam War.";
		
		Set<Mention> mentions = new HashSet<Mention>();
		mentions.add(new Mention(text.indexOf(" clinton's"), " clinton's".length()));
		mentions.add(new Mention(text.indexOf("War."), "War.".length()));
		mentions.add(new Mention(text.indexOf("thought"), "thought".length()));
		
		Set<Annotation> anns = ProblemReduction.Sa2WToD2W(tagme.solveSa2W(text), mentions, 0.3f);
		Set<Annotation> gs = new HashSet<Annotation>();
		gs.add(new Annotation(text.indexOf(" clinton's"), " clinton's".length(), 3356));
		gs.add(new Annotation(text.indexOf("about"), "about".length(), 666));
		gs.add(new Annotation(text.indexOf("War."), "War.".length(), 777));
		gs.add(new Annotation(text.indexOf("thought"), "thought".length(), 557913));
		*/
		/*DumpData.dumpCompare(text, gs, anns, api);
		
		Metrics<Annotation> met = new Metrics<Annotation>();
		MetricsResultSet r = met.getResult(Arrays.asList(anns), Arrays.asList(gs), new StrongAnnotationMatch(api));
		
		System.out.printf("TP:%d FP:%d FN:%d%n", r.getGlobalTp(), r.getGlobalFp(), r.getGlobalFn());
		System.out.printf("MACRO Prec:%.3f Rec:%.3f F1:%.3f %n", r.getMacroPrecision(), r.getMacroRecall(), r.getMacroF1());
		System.out.printf("MICRO Prec:%.3f Rec:%.3f F1:%.3f %n", r.getMicroPrecision(), r.getMicroRecall(), r.getMicroF1());
		*/
/*		Benchmark.useCache("benchmark/cache/results2.cache");
		String longest = Benchmark.getLongestSa2WTimeDoc("AIDA-CocktailParty", "TimeRuns");
		long timeLongest = Benchmark.getSa2WTiming("AIDA-CocktailParty", "TimeRuns", longest);
		System.out.println("Longest doc:\n"+longest);
		System.out.println("Time: "+timeLongest+" ms");
		System.out.println("Length: "+longest.length());
*/		
		/*String resultsCacheFilename1 = "benchmark/cache/results.cache";
		String resultsCacheFilename2 = "benchmark/cache/results_spotlight.cache";
		
		Benchmark.useCache(resultsCacheFilename1);
		Benchmark.merge(resultsCacheFilename2);*/
		
		String resultsCacheFilename = "benchmark/cache/results.cache";
		BenchmarkResults resultsCache = (BenchmarkResults) new ObjectInputStream(new FileInputStream(resultsCacheFilename)).readObject();
		resultsCache.invalidateResults("WikiSense");
		//resultsCache.invalidateBestThresholds();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(resultsCacheFilename));
		oos.writeObject(resultsCache);
		oos.close();

		/*		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");

		AnnotatorMetrics<Tag> m = new WeakTaggerMetrics(api);
		Set<Tag> comp1 = new HashSet<Tag>();
		comp1.add(new Tag(1,1,1)); //tp
		comp1.add(new Tag(1,1,3));//fp
		comp1.add(new Tag(1,5,5));//tp
		comp1.add(new Tag(1,1,5));//tp
		comp1.add(new Tag(1,5,7));//tp
		comp1.add(new Tag(10,5,7));//fp

		Set<Tag> gs1 = new HashSet<Tag>();
		gs1.add(new Tag(1,1,1));
		gs1.add(new Tag(3,18,1)); //fn
		gs1.add(new Tag(1,5,5));
		gs1.add(new Tag(7,5,5));//fn
		gs1.add(new Tag(1,7,5));
		gs1.add(new Tag(1,5,7));

		Set<Tag> comp2 = new HashSet<Tag>();
		comp2.add(new Tag(1,1,1)); //tp

		Set<Tag> gs2 = new HashSet<Tag>();
		gs2.add(new Tag(1,1,1));
		
		List<Set<Tag>> comp = new Vector<Set<Tag>>();
		comp.add(comp1);
		comp.add(comp2);

		List<Set<Tag>> gs = new Vector<Set<Tag>>();
		gs.add(gs1);
		gs.add(gs2);

		System.out.println("size:"+m.singleTp(gs, comp).length);
		for (int tp: m.singleTp(gs, comp))
			System.out.println("tp:"+tp);
		for (int fp: m.singleFp(gs, comp))
			System.out.println("fp:"+fp);
		for (int fn: m.singleFn(gs, comp))
			System.out.println("fn:"+fn);
		int tp = m.tp(gs, comp);
		int fp = m.fp(gs, comp);
		int fn = m.fn(gs, comp);
		System.out.println("prec:"+m.precision(tp, fp));
		System.out.println("recall:"+m.recall(tp, fp, fn));

		System.out.println("True positives:");
		List<Set<Tag>> truePositives = m.getTp(comp, gs);
		for (int i=0; i<truePositives.size(); i++){
			System.out.println("instance "+i);
			for (Tag t: truePositives.get(i))
				System.out.printf("(%d %d %d)%n", t.getPosition(), t.getLength(), t.getWikipediaArticle());
		}

		System.out.println("Jaccard:"+m.outputSimilarity(comp, gs));
*/

		/*
		AIDATagger aida = new AIDATagger("benchmark/configs/aida/config.xml", api);
//		aida.getAnnotations();
		api.flush();*/
		/*String string = "abc\\u5639\\u563b";
	    System.out.println(string);
	    byte[] utf8 = string.getBytes("UTF-8");

	    // Convert from UTF-8 to Unicode
	    string = new String(string.getBytes(), "UTF-8");

	    System.out.println(string);
	    string = org.apache.commons.lang.StringEscapeUtils.unescapeJava(string);
	    System.out.println(string);*/

	}

}
