package it.acubelab.batframework.metrics;

import it.acubelab.batframework.data.RelatednessRecord;
import it.acubelab.batframework.problems.RelatednessMeasurer;

import java.util.List;

public class RelatednessMetrics {

	private static float[] getOutput(List<RelatednessRecord> output,
			RelatednessMeasurer rel) {
		float[] outputRel = new float[output.size()];
		for (int i = 0; i < output.size(); i++) {
			RelatednessRecord rec = output.get(i);
			outputRel[i] = (rel.getRelatedness(rec.getEntity1(), rec.getEntity2()));
			if (outputRel[i] > 1 || outputRel[i] < 0)
				throw new RuntimeException(
						"Both relatedness measures should be in [0,1]. output="
								+ outputRel[i]);
		}
		return outputRel;
	}

	private static float[] getGoldStandard(List<RelatednessRecord> goldStandard) {
		float[] goldStandardRel = new float[goldStandard.size()];
		for (int i = 0; i < goldStandard.size(); i++) {
			RelatednessRecord rec = goldStandard.get(i);
			goldStandardRel[i] = rec.getRelatedness();
			if (goldStandardRel[i] > 1 || goldStandardRel[i] < 0)
				throw new RuntimeException(
						"Both relatedness measures should be in [0,1]. goldStandard="
								+ goldStandardRel[i]);
		}
		return goldStandardRel;
	}

	public static float getQuadraticDistance(
			List<RelatednessRecord> goldStandardList, RelatednessMeasurer rel) {
		float[] output = getOutput(goldStandardList, rel);
		float[] goldStandard = getGoldStandard(goldStandardList);
		if (output.length != goldStandard.length)
			throw new NullPointerException();
		float res = 0;
		for (int i = 0; i < output.length; i++)
			res += Math.pow((output[i] - goldStandard[i]), 2);
		res /= output.length;
		return 1 - (float) Math.sqrt(res);
	}

	public static float getAbsoluteDistance(
			List<RelatednessRecord> goldStandardList, RelatednessMeasurer rel) {
		float[] output = getOutput(goldStandardList, rel);
		float[] goldStandard = getGoldStandard(goldStandardList);
		if (output.length != goldStandard.length)
			throw new NullPointerException();
		float res = 0;
		for (int i = 0; i < output.length; i++)
			res += Math.abs(output[i] - goldStandard[i]);
		res /= output.length;
		return 1 - res;
	}

}
