package test;

import java.io.*;

import it.unipi.di.acube.batframework.cache.*;
import it.unipi.di.acube.batframework.datasetPlugins.*;
import it.unipi.di.acube.batframework.problems.*;
import it.unipi.di.acube.batframework.utils.*;

public class InvalidateResults {

	public static void main(String[] args) throws Exception{
		String resultsCacheFilename = "results.cache";
		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");
		//A2WDataset aidaDs = new ConllAidaDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset.tsv", api);
		//A2WDataset aidaTestBDs = new ConllAidaTestBDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset-update.tsv", api);
		
		BenchmarkCache.useCache(resultsCacheFilename);
		System.out.println(BenchmarkCache.getCacheInfo());
		
		BenchmarkResults resultsCache = (BenchmarkResults) new ObjectInputStream(new FileInputStream(resultsCacheFilename)).readObject();
		//resultsCache.invalidateResults("AIDA-PriorityOnly", aidaDs);
		//resultsCache.invalidateResults("AIDA-PriorityOnly", aidaTestBDs);
		//resultsCache.invalidateResults("TagMe 2");
		resultsCache.invalidateD2WResults();
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(resultsCacheFilename));
		oos.writeObject(resultsCache);
		oos.close();
		
		BenchmarkCache.useCache(resultsCacheFilename);
		System.out.println(BenchmarkCache.getCacheInfo());
	}
	
}
