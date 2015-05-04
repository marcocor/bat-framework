/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.examples;

import it.unipi.di.acube.batframework.metrics.MatchRelation;
import it.unipi.di.acube.batframework.metrics.Metrics;

import java.io.IOException;
import java.util.List;
import java.util.HashSet;


public class GeneralizedF1Metrics<T> extends Metrics<T> {

	public static float genF1(float recall, float precision, float beta){
		float betaPow = beta*beta;
		return (recall+precision == 0) ? 0 : (1+betaPow)*recall*precision/(betaPow*recall+precision);
	}

	public float computeF1(List<HashSet<T>> outputOrig, List<HashSet<T>> goldStandardOrig, float beta, MatchRelation<T> m) throws IOException{
		List<HashSet<T>> output = m.preProcessOutput(outputOrig);
		List<HashSet<T>> goldStandard = m.preProcessGoldStandard(goldStandardOrig);
		int tp = tpCount(goldStandard, output, m);
		int fp = fpCount(goldStandard, output, m);
		int fn = fnCount(goldStandard, output, m);
		float precision = precision(tp, fp);
		float recall = recall(tp, fp, fn);

		return genF1(recall, precision, beta);
	}
}
