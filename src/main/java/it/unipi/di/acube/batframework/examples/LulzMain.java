/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.examples;

import java.util.*;

import it.unipi.di.acube.batframework.cache.*;
import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.datasetPlugins.IITBDataset;
import it.unipi.di.acube.batframework.metrics.*;
import it.unipi.di.acube.batframework.problems.*;
import it.unipi.di.acube.batframework.systemPlugins.*;
import it.unipi.di.acube.batframework.utils.*;

public class LulzMain {

	public static void main(String[] args) throws Exception {
		//Store the cache in a file (for future executions).
		BenchmarkCache.useCache("benchmark/cache/results.cache");
		//Create the API to wikipedia, storing retrieved data in two files, and the interface to DBPedia.
		WikipediaInterface wikiApi = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");
		DBPediaApi dbpediaApi = new DBPediaApi();
		
		//Create the interface to an annotator.
		Sa2WSystem miner = new SpotlightAnnotator(dbpediaApi, wikiApi);
		A2WDataset iitb = new IITBDataset("benchmark/datasets/iitb/crawledDocs", "benchmark/datasets/iitb/CSAW_Annotations.xml", wikiApi);
		
		//Run the experiment
		List<HashSet<ScoredAnnotation>> computedAnnotations = BenchmarkCache.doSa2WAnnotations(miner, iitb, null, 0);
		
		//Adapt the output to the same type of the gold standard (A2W) setting a threshold that excludes all annotations under 0.1
		List<HashSet<Annotation>> reducedAnnotations = ProblemReduction.Sa2WToA2WList(computedAnnotations, 0.1f);
		
		//Take the gold standard
		List<HashSet<Annotation>> goldStandard = iitb.getA2WGoldStandardList();

		//Create a Match relation and the metrics calculator.
		MatchRelation<Annotation> wam = new WeakAnnotationMatch(wikiApi);
		Metrics<Annotation> metrics = new Metrics<Annotation>();
		
		//Compute the results
		MetricsResultSet rs = metrics.getResult(reducedAnnotations, goldStandard, wam);

		printResults(rs);
		
		BenchmarkCache.flush();
		wikiApi.flush();
	}

	public static void printResults(MetricsResultSet rs){
		System.out.printf("Micro-Precision:%.3f%nMicro-Recall:%.3f%nMicro-F1:%.3f%n"+
				"Macro-Precision:%.3f%nMacro-Recall:%.3f%nMacro-F1:%.3f%n"+
				"TP:%d FP:%d FN:%d%n",
				rs.getMicroPrecision(), rs.getMicroRecall(), rs.getMicroF1(),
				rs.getMacroPrecision(), rs.getMacroRecall(), rs.getMacroF1(),
				rs.getGlobalTp(), rs.getGlobalFp() ,rs.getGlobalFn()
				);
	}
	
}
