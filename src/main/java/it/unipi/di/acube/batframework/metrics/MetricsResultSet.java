/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.metrics;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

public class MetricsResultSet implements Serializable{
	public static final int BOOTSTRAP_K = 40; 
	private static final long serialVersionUID = 1L;
	private static final StandardDeviation std = new StandardDeviation();
	private static final Variance var = new Variance();
	private static final Mean mean = new Mean();
	private float microF1, microRecall, microPrecision, macroF1, macroRecall,
			macroPrecision;
	private int tp, fn, fp;
	private float[] precisions, recalls, f1s;
	private int[] tps, fns, fps;

	public MetricsResultSet(float[] precisions, float[] recalls, float[] f1s, int[] tps, int[] fps, int[] fns) {
		this.tps = tps;
		this.fns = fns;
		this.fps = fps;
		this.precisions = precisions;
		this.recalls = recalls;
		this.f1s = f1s;
		this.tp = Arrays.stream(tps).sum();
		this.fn = Arrays.stream(fns).sum();
		this.fp = Arrays.stream(fps).sum();
		this.microPrecision = Metrics.precision(tp, fp);
		this.microRecall = Metrics.recall(tp, fp, fn);
		this.microF1 = Metrics.F1(microRecall, microPrecision);
		this.macroPrecision = Metrics.macroPrecision(tps, fps);
		this.macroRecall = Metrics.macroRecall(tps, fps, fns);
		this.macroF1 = Metrics.macroF1(tps, fps, fns);
	}

	public int testedInstances() {
		return precisions.length;
	}

	public float getMicroRecall() {
		return microRecall;
	}

	public float getMicroPrecision() {
		return microPrecision;
	}

	public float getMicroF1() {
		return microF1;
	}

	public float getMacroRecall() {
		return macroRecall;
	}

	public float getMacroPrecision() {
		return macroPrecision;
	}

	public float getMacroF1() {
		return macroF1;
	}

	public int getGlobalTp() {
		return tp;
	}

	public int getGlobalFp() {
		return fp;
	}

	public int getGlobalFn() {
		return fn;
	}

	public float getPrecisions(int i) {
		return precisions[i];
	}

	public float getRecalls(int i) {
		return recalls[i];
	}

	public float getF1s(int i) {
		return f1s[i];
	}

	public double getF1StdDev() {
		return std.evaluate(Doubles.toArray(Floats.asList(f1s)));
	}
	
	public double getPrecisionStdDev() {
		return std.evaluate(Doubles.toArray(Floats.asList(precisions)));
	}
	
	public double getRecallStdDev() {
		return std.evaluate(Doubles.toArray(Floats.asList(recalls)));
	}
	
	public double getF1Var() {
		return var.evaluate(Doubles.toArray(Floats.asList(f1s)));
	}
	
	public double getPrecisionVar() {
		return var.evaluate(Doubles.toArray(Floats.asList(precisions)));
	}
	
	public double getRecallVar() {
		return var.evaluate(Doubles.toArray(Floats.asList(recalls)));
	}

	public static int[] getRandomSampleIndices(int n, Random r) {
		int[] indexes = new int[n];
		for (int i = 0; i < n; i++)
			indexes[i] = r.nextInt(n);
		return indexes;
	}

	private MetricsResultSet[] getBootstrapRuns() {
		Random r = new Random(42);
		MetricsResultSet[] results = new MetricsResultSet[BOOTSTRAP_K];
		for (int i = 0; i < BOOTSTRAP_K; i++) {
			int[] randIndexesI = getRandomSampleIndices(tps.length, r);
			float[] precisionsI = new float[randIndexesI.length];
			float[] recallsI = new float[randIndexesI.length];
			float[] f1sI = new float[randIndexesI.length];
			int[] tpsI = new int[randIndexesI.length];
			int[] fpsI = new int[randIndexesI.length];
			int[] fnsI = new int[randIndexesI.length];
			for (int j = 0; j < randIndexesI.length; j++) {
				precisionsI[j] = precisions[randIndexesI[j]];
				recallsI[j] = recalls[randIndexesI[j]];
				f1sI[j] = f1s[randIndexesI[j]];
				tpsI[j] = tps[randIndexesI[j]];
				fpsI[j] = fps[randIndexesI[j]];
				fnsI[j] = fns[randIndexesI[j]];
			}
			results[i] = new MetricsResultSet(precisionsI, recallsI, f1sI, tpsI, fpsI, fnsI);
		}

		return results;
	}

	public double getMicroPrecisionBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return mean.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMicroPrecision()).toArray());
	}

	public double getMicroPrecisionStdBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return std.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMicroPrecision()).toArray());
	}

	public double getMicroRecallBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return mean.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMicroRecall()).toArray());
	}

	public double getMicroRecallStdBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return std.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMicroRecall()).toArray());
	}

	public double getMicroF1Bootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return mean.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMicroF1()).toArray());
	}

	public double getMicroF1StdBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return std.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMicroF1()).toArray());
	}

	public double getMacroPrecisionBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return mean.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMacroPrecision()).toArray());
	}

	public double getMacroPrecisionStdBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return std.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMacroPrecision()).toArray());
	}

	public double getMacroRecallBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return mean.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMacroRecall()).toArray());
	}

	public double getMacroRecallStdBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return std.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMacroRecall()).toArray());
	}

	public double getMacroF1Bootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return mean.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMacroF1()).toArray());
	}

	public double getMacroF1StdBootstrap() {
		MetricsResultSet[] bootstrapRuns = getBootstrapRuns();
		return std.evaluate(Arrays.stream(bootstrapRuns).mapToDouble(run -> run.getMacroF1()).toArray());
	}

	public String toString() {
		return String.format(Locale.ENGLISH, "mac-P/R/F1: %.3f/%.3f/%.3f mic-P/R/F1: %.3f/%.3f/%.3f TP/FP/FN: %d/%d/%d",
		        this.getMacroPrecision(), this.getMacroRecall(), this.getMacroF1(), this.getMicroPrecision(),
		        this.getMicroRecall(), this.getMicroF1(), this.getGlobalTp(), this.getGlobalFp(), this.getGlobalFn());
	}

	public int getTPs(int i) {
		return tps[i];
	}

	public int getFPs(int i) {
		return fps[i];
	}

	public int getFNs(int i) {
		return fns[i];
	}
}
