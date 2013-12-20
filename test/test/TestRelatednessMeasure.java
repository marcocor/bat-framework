package test;

import it.acubelab.batframework.datasetPlugins.WikipediaSimilarity353;
import it.acubelab.batframework.metrics.RelatednessMetrics;
import it.acubelab.batframework.problems.RelatednessDataset;
import it.acubelab.batframework.problems.RelatednessMeasurer;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class TestRelatednessMeasure {

	public static void main(String[] args) throws Exception {
		RelatednessMeasurer rel = new Measure1();
		WikipediaApiInterface wikiApi = new WikipediaApiInterface("/tmp/wid.cache", "/tmp/redirect.cache");
		RelatednessDataset wr353 = new WikipediaSimilarity353("benchmark/datasets/wikipediaSimilarity353/wikipediaSimilarity353.csv", wikiApi);
		System.out.println(RelatednessMetrics.getQuadraticDistance(wr353.getGoldStandard(), rel));
		System.out.println(RelatednessMetrics.getAbsoluteDistance(wr353.getGoldStandard(), rel));

	}

	public static class Measure1 implements RelatednessMeasurer {

		@Override
		public float getRelatedness(int entity1, int entity2) {
			return 0.6f;
		}

	}
}
