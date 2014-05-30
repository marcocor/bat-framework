package test;

import java.util.HashSet;
import java.util.Set;

import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.problems.Sa2WSystem;
import it.acubelab.batframework.systemPlugins.AIDADefaultAnnotator;
import it.acubelab.batframework.utils.DumpData;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class TestWikisense {

	public static void main(String[] args) throws Exception {
		java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
		java.security.Security.setProperty("networkaddress.cache.ttl", "0");
		
		//BenchmarkCache.useCache("benchmark/cache/results.cache");
		
		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");

		Sa2WSystem aida = new AIDADefaultAnnotator("http://localhost:9999/aida/service/disambiguate-defaultsettings", api);
/*		Set<ScoredAnnotation> res = aida.solveSa2W("The new CEO of Fédération Internationale de l'Automobile is Slobodan [[Milošević]], the Yugoslav politician who was the President of Serbia");
		for (ScoredAnnotation sa : res)
			System.out.println(sa);
*/		
		String text = "The new CEO of Fédération Internationale de l'Automobile is Slobodan Milošević, the Yugoslav politician who was the President of Serbia";
		HashSet<Mention> mentions = new HashSet<>();
		mentions.add(new Mention(text.indexOf("Milošević"), "Milošević".length()));
		mentions.add(new Mention(text.indexOf("of"), "of".length()));
		HashSet<Annotation> res = aida.solveD2W(text, mentions);
		DumpData.dumpCompare(text, null, res, api);
		
	}

}
