/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.examples;

import it.acubelab.batframework.cache.BenchmarkCache;
import it.acubelab.batframework.data.*;
import it.acubelab.batframework.datasetPlugins.*;
import it.acubelab.batframework.metrics.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.systemPlugins.*;
import it.acubelab.batframework.utils.*;

import java.util.*;

public class BatchMain {

	public static void main(String[] args) throws Exception {
		//Use system DNS
		java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
		java.security.Security.setProperty("networkaddress.cache.ttl", "0");
		
		//Cache retrieved annotations here
		BenchmarkCache.useCache("benchmark/cache/results.cache");
		
		System.out.println(BenchmarkCache.getCacheInfo());

		System.out.println("Creating the API to wikipedia...");
		WikipediaApiInterface wikiApi = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");
		DBPediaApi dbpApi = new DBPediaApi();

		System.out.println("Creating the taggers...");
		Sa2WSystem tagme = new TagmeAnnotator("benchmark/configs/tagme/config.xml");
		Sa2WSystem illinois = new IllinoisAnnotator_Server();
		Sa2WSystem miner = new WikipediaMinerAnnotator("benchmark/configs/wikipediaminer/config.xml");
		Sa2WSystem aidaLocal = new AIDALocalAnnotator("benchmark/configs/aida/config.xml", wikiApi);
		Sa2WSystem aidaPrior = new AIDAPriorityOnlyAnnotator("benchmark/configs/aida/config.xml", wikiApi);
		Sa2WSystem aidaCocktail = new AIDACockailPartyAnnotator("benchmark/configs/aida/config.xml", wikiApi);
		Sa2WSystem spotLight = new SpotlightAnnotator(dbpApi, wikiApi);


		System.out.println("Loading the datasets...");
		C2WDataset meijDs = new MeijDataset("benchmark/datasets/meij/original_tweets.list", "benchmark/datasets/meij/wsdm2012_annotations.txt", "benchmark/datasets/meij/wsdm2012_qrels.txt");
		A2WDataset aquaintDs = new AQUAINTDataset("benchmark/datasets/AQUAINT/RawTexts", "benchmark/datasets/AQUAINT/Problems", wikiApi);
		A2WDataset aidaDs = new ConllAidaDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset.tsv", wikiApi);
		A2WDataset msnbcDs = new MSNBCDataset("benchmark/datasets/MSNBC/RawTextsSimpleChars_utf8", "benchmark/datasets/MSNBC/Problems", wikiApi);
		A2WDataset iitbDs = new IITBDataset("benchmark/datasets/iitb/crawledDocs", "benchmark/datasets/iitb/CSAW_Annotations.xml", wikiApi);
		
		/** Create a vector containing all the A2W datasets */
		Vector<A2WDataset> dssA2W = new Vector<A2WDataset>();
		dssA2W.add(iitbDs);
		dssA2W.add(msnbcDs);
		dssA2W.add(aquaintDs);
		dssA2W.add(aidaDs);
		
		/** Create a vector containing all the C2W datasets */
		Vector<C2WDataset> dssC2W = new Vector<C2WDataset>();
		dssC2W.add(meijDs);
		dssC2W.add(iitbDs); //Yes, you can put a A2W dataset here, since it also provides a C2W gold standard.
		
		/** Create a vector containing all the Sa2W annotators */
		Vector<Sa2WSystem> sa2wAnnotators = new Vector<Sa2WSystem>();
		sa2wAnnotators.add(aidaLocal);
		sa2wAnnotators.add(tagme);
		sa2wAnnotators.add(illinois);
		sa2wAnnotators.add(aidaCocktail);
		sa2wAnnotators.add(aidaPrior);
		sa2wAnnotators.add(spotLight);
		sa2wAnnotators.add(miner);

		/** Create the match relations */
		MatchRelation<Annotation> wam = new WeakAnnotationMatch(wikiApi);
		MatchRelation<Annotation> sam = new StrongAnnotationMatch(wikiApi);
		MatchRelation<Annotation> cam = new ConceptAnnotationMatch(wikiApi);
		MatchRelation<Annotation> mam = new MentionAnnotationMatch();
		
		/*********** A2W experiments ************/
		
		/** Create a vector containing the match relations we want to base our measurements for the A2W Experiment on. */
		Vector<MatchRelation<Annotation>> matchRelationsA2W = new Vector<MatchRelation<Annotation>>();
		matchRelationsA2W.add(wam);
		matchRelationsA2W.add(sam);
		matchRelationsA2W.add(cam);
		matchRelationsA2W.add(mam);
		
		/** Hashmap for saving the measurements results.
		 * The mapping will be: metric name -> tagger name -> dataset name -> threshold -> results set */
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> resA2W;
		
		/** Run the experiments for varying thresholds, store the resulting measures to resA2W */
		resA2W = RunExperiments.performA2WExpVarThreshold(matchRelationsA2W, null, sa2wAnnotators, dssA2W, wikiApi);
		
		Pair<Float, MetricsResultSet> p =  RunExperiments.getBestRecord(resA2W, wam.getName(), tagme.getName(), iitbDs.getName());
		System.out.printf("The best micro-F1 for %s on %s is achieved with a threshold of $.3f. Its value is %.3f.%n",tagme.getName(), iitbDs.getName(), p.first, p.second.getMicroF1());
		
		/** Print the results about correctness (F1, precision, recall) to the screen */
		DumpResults.printCorrectnessPerformance(matchRelationsA2W, null, sa2wAnnotators, null, null, dssA2W, resA2W);
		
		/** Print the results about correctness to the screen as a Latex table */
		DumpResults.latexCorrectnessPerformance(matchRelationsA2W, null, sa2wAnnotators, null, dssA2W, false, resA2W);

		/** Output the results in a gnuplot data .dat file that can then be given to Gnuplot*/
		DumpResults.gnuplotCorrectnessPerformance(matchRelationsA2W, sa2wAnnotators, dssA2W, wikiApi, resA2W);

		/*********** C2W experiments ************/
		
		/** Hashmap for saving the measurements results. */
		HashMap<String, HashMap<String, HashMap<String, HashMap<Float, MetricsResultSet>>>> resC2W;

		/** Create a vector containing the match relations we want to base our measurements for the C2W Experiment on. */
		Vector<MatchRelation<Tag>> matchRelationsC2W = new Vector<MatchRelation<Tag>>();
		matchRelationsC2W.add(new StrongTagMatch(wikiApi));
		
		/** Run the experiments for varying thresholds, store the resulting measures to resC2W */
		resC2W = RunExperiments.performC2WExpVarThreshold(matchRelationsC2W, null, sa2wAnnotators, null, dssC2W, wikiApi);

		/** Print the results about correctness (F1, precision, recall) to the screen */
		DumpResults.printCorrectnessPerformance(matchRelationsC2W, null, sa2wAnnotators, null, null, dssC2W, resC2W);
		
		/** Print the results about correctness to the screen as a Latex table */
		DumpResults.latexCorrectnessPerformance(matchRelationsC2W, null, sa2wAnnotators, null, dssC2W, false, resC2W);

		/** Output the results in a gnuplot data .dat file that can then be given to Gnuplot*/
		DumpResults.gnuplotCorrectnessPerformance(matchRelationsC2W, sa2wAnnotators, dssC2W, wikiApi, resC2W);

		/** Timing tables in two forms */
		Vector<C2WDataset> dss = new Vector<C2WDataset>();
		dss.addAll(dssC2W);
		dss.addAll(dssA2W);
		DumpResults.latexTimingPerformance(null, sa2wAnnotators, null, dss);
		DumpResults.latexTimingPerformance2(sa2wAnnotators, null, dss);
		
		/** F1-runtime plot, just for the IITB dataset. */
		DumpResults.gnuplotRuntimeF1(wam.getName(), null, sa2wAnnotators, iitbDs.getName(), wikiApi, resA2W);
		
		/** Output annotations similarity in a latex table. */
		DumpResults.latexSimilarityA2W(dssA2W, sa2wAnnotators, resA2W, wikiApi);

		wikiApi.flush();
	}

}
