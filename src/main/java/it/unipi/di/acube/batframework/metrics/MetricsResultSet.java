/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.metrics;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

public class MetricsResultSet implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final StandardDeviation std = new StandardDeviation();
	private static final Variance var = new Variance();
	private float microF1, microRecall, microPrecision, macroF1, macroRecall,
			macroPrecision;
	private int tp, fn, fp;
	private float[] precisions, recalls, f1s;
	private int[] tps, fns, fps;

	public MetricsResultSet(float microF1, float microRecall,
			float microPrecision, float macroF1, float macroRecall,
			float macroPrecision, int tp, int fn, int fp, float[] precisions,
			float[] recalls, float[] f1s, int[] tps, int[] fps, int[] fns) {
		this.microF1 = microF1;
		this.microRecall = microRecall;
		this.microPrecision = microPrecision;
		this.macroF1 = macroF1;
		this.macroRecall = macroRecall;
		this.macroPrecision = macroPrecision;
		this.tp = tp;
		this.fn = fn;
		this.fp = fp;
		this.precisions = precisions;
		this.recalls = recalls;
		this.f1s = f1s;
		this.tps = tps;
		this.fns = fns;
		this.fps = fps;
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
