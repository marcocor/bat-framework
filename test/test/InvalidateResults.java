package test;

import java.io.*;

import it.acubelab.batframework.cache.*;
import it.acubelab.batframework.datasetPlugins.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

public class InvalidateResults {

	public static void main(String[] args) throws Exception{
		String resultsCacheFilename = "benchmark/cache/results.cache";
		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");
		//A2WDataset aidaDs = new ConllAidaDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset.tsv", api);
		//A2WDataset aidaTestBDs = new ConllAidaTestBDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset-update.tsv", api);
		
		BenchmarkCache.useCache(resultsCacheFilename);
		System.out.println(BenchmarkCache.getCacheInfo());
		
		BenchmarkResults resultsCache = (BenchmarkResults) new ObjectInputStream(new FileInputStream(resultsCacheFilename)).readObject();
		//resultsCache.invalidateResults("AIDA-PriorityOnly", aidaDs);
		//resultsCache.invalidateResults("AIDA-PriorityOnly", aidaTestBDs);
		resultsCache.invalidateResults("TagMe 2");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(resultsCacheFilename));
		oos.writeObject(resultsCache);
		oos.close();
		
		BenchmarkCache.useCache(resultsCacheFilename);
		System.out.println(BenchmarkCache.getCacheInfo());
	}
	
}
