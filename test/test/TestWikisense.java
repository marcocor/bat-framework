package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import it.acubelab.batframework.cache.BenchmarkCache;
import it.acubelab.batframework.problems.Sa2WSystem;
import it.acubelab.batframework.systemPlugins.TagmeAnnotator;
import it.acubelab.batframework.systemPlugins.WikiSenseAnnotator;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class TestWikisense {

	public static void main(String[] args) throws Exception {
		java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
		java.security.Security.setProperty("networkaddress.cache.ttl", "0");
		
		//BenchmarkCache.useCache("benchmark/cache/results.cache");
		
		//WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");

		Sa2WSystem wikisense = new WikiSenseAnnotator();
		wikisense.solveSa2W("Obama visited Italy's PM Berlusconi, after leaving China. They talked about blue sky\".}");
		
	}

}
