/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.metrics;

import java.io.IOException;
import java.util.*;

public class Metrics <T> {

	public MetricsResultSet getResult(List<Set<T>> outputOrig, List<Set<T>> goldStandardOrig, MatchRelation<T> m) throws IOException{
		List<Set<T>> output = m.preProcessOutput(outputOrig);
		List<Set<T>> goldStandard = m.preProcessGoldStandard(goldStandardOrig);

		int tp = tpCountPreprocessed(goldStandard, output, m);
		int fp = fpCountPreprocessed(goldStandard, output, m);
		int fn = fnCountPreprocessed(goldStandard, output, m);
		float microPrecision = precision(tp, fp);
		float microRecall = recall(tp, fp, fn);
		float microF1 = F1(microRecall, microPrecision);
		int[] tps = singleTpCount(goldStandard, output, m);
		int[] fps = singleFpCount(goldStandard, output, m);
		int[] fns = singleFnCount(goldStandard, output, m);
		float macroPrecision = macroPrecision(tps, fps);
		float macroRecall = macroRecall(tps, fps, fns);
		float macroF1 = macroF1(tps, fps, fns);
		float precisions[] =  precisions(tps, fps);
		float recalls[] =  recalls(tps, fps, fns);
		float f1s[] =  f1s(tps, fps, fns);
		
		return new MetricsResultSet(microF1, microRecall, microPrecision, macroF1, macroRecall, macroPrecision, tp, fn, fp, precisions, recalls, f1s);
	}

	private int similarityIntersection(Set<T> set1, Set<T> set2, MatchRelation<T> m){
		int intersectionI = 0;
		for (T obj1 : set1)
			for (T obj2: set2)
				if (m.match(obj1,obj2)) {
					intersectionI++;
					break;
				}
		for (T obj2 : set2)
			for (T obj1: set1)
				if (m.match(obj1,obj2)) {
					intersectionI++;
					break;
				}
		return intersectionI;
	}

	/**
	 * @param set1
	 * @param set2
	 * @param m
	 * @return the number of elements that are in set1 and have no match with any element of set2, according to match relation m.
	 */
	private int dissimilaritySet(Set<T> set1, Set<T> set2, MatchRelation<T> m){
		int diss = 0;
		for (T obj1 : set1){
			boolean found = false;
			for (T obj2: set2)
				if (m.match(obj1,obj2)){
					found = true;
					break;
				}
			if (!found) diss++;
		}
		return diss;
	}

	private int similarityUnion(Set<T> set1, Set<T> set2){
		return set1.size() + set2.size();
	}

	public float singleSimilarity(Set<T> set1, Set<T> set2, MatchRelation<T> m){
		int intersectionI = similarityIntersection(set1, set2, m);
		int unionI = similarityUnion(set1, set2);
		return (unionI==0) ? 1 : (float)intersectionI/(float)unionI;
	}

	public float macroSimilarity(List<Set<T>> list1, List<Set<T>> list2, MatchRelation<T> m){
		List<Set<T>> list1preproc = m.preProcessOutput(list1);
		List<Set<T>> list2preproc = m.preProcessOutput(list2);

		float avg = 0;
		for (int i=0; i<list1preproc.size(); i++){
			Set<T> set1 = list1preproc.get(i);
			Set<T> set2 = list2preproc.get(i);
			avg += singleSimilarity(set1, set2, m);
		}
		return avg/(float)list1preproc.size();
	}

	public float microSimilarity(List<Set<T>> list1, List<Set<T>> list2, MatchRelation<T> m){
		List<Set<T>> list1preproc = m.preProcessOutput(list1);
		List<Set<T>> list2preproc = m.preProcessOutput(list2);

		long intersections = 0;
		long unions = 0;
		for (int i=0; i<list1preproc.size(); i++){
			Set<T> set1 = list1preproc.get(i);
			Set<T> set2 = list2preproc.get(i);
			intersections += similarityIntersection(set1, set2, m);
			unions += similarityUnion(set1, set2);
		}
		return intersections/unions;
	}

	public int dissimilarityListCount(List<Set<T>> list1, List<Set<T>> list2, MatchRelation<T> m){
		List<Set<T>> list1preproc = m.preProcessOutput(list1);
		List<Set<T>> list2preproc = m.preProcessOutput(list2);

		int dissim = 0;
		for (int i=0; i<list1preproc.size(); i++){
			Set<T> set1 = list1preproc.get(i);
			Set<T> set2 = list2preproc.get(i);
			dissim += dissimilaritySet(set1, set2, m);
		}

		return dissim;
	}

	/**
	 * @param list1
	 * @param list2
	 * @param m
	 * @return the amount of elements of list1 that match with an element of list 2 + viceversa. (is reflexive)
	 */
	public int similarityListCount(List<Set<T>> list1, List<Set<T>> list2, MatchRelation<T> m){
		List<Set<T>> list1preproc = m.preProcessOutput(list1);
		List<Set<T>> list2preproc = m.preProcessOutput(list2);

		int intersect = 0;
		for (int i=0; i<list1preproc.size(); i++){
			Set<T> set1 = list1preproc.get(i);
			Set<T> set2 = list2preproc.get(i);
			intersect += similarityIntersection(set1, set2, m);
		}

		return intersect;
	}

	public long listUnion(List<Set<T>> list1, List<Set<T>> list2, MatchRelation<T> m){
		List<Set<T>> list1preproc = m.preProcessOutput(list1);
		List<Set<T>> list2preproc = m.preProcessOutput(list2);

		long union = 0;
		for (int i=0; i<list1preproc.size(); i++){
			Set<T> set1 = list1preproc.get(i);
			Set<T> set2 = list2preproc.get(i);
			union += similarityUnion(set1, set2);
		}
		return union;
	}


	/**
	 * @param tp the number of true positives, i.e. the number of annotations found by a tagger that are right (match with the gold standard).
	 * @param fp the number of false positives, i.e. the number of annotations found by a tagger that are wrong (mismatch with the gold standard).
	 * @return a value in [0, 1] representing the precision of a tagger, that is the fraction of annotations that are right according to the gold standard.
	 */
	public static float precision(int tp, int fp){
		return tp+fp == 0 ? 1 : (float)tp/(float)(tp+fp);
	}

	/**
	 * @param tp the number of true positives, i.e. the number of annotations found by a tagger that are right (match with the gold standard).
	 * @param fp the number of false positives, i.e. the number of annotations found by a tagger that are wrong (mismatch with the gold standard).
	 * @param fn the number of false negatives, i.e. the number of right annotations (according to the gold standard) that the tagger could not find.
	 * @return a value in [0, 1] representing the precision of a tagger, that is the fraction of annotations that are right according to the gold standard.
	 */
	public static float recall(int tp, int fp, int fn){
		return fn == 0 ? 1 : (float)tp/(float)(fn+tp);
	}

	/**
	 * Compute the F1 measure, a measure that takes in account both recall and precision.
	 * @param recall the recall.
	 * @param precision the precision.
	 * @return the F1 measure in [0, 1].
	 */
	public static float F1(float recall, float precision){
		return (recall+precision == 0) ? 0 : 2*recall*precision/(recall+precision);
	}

	//[{ok1, nok, nok, ok2}, {ok3, ok4, nok}] -> [{ok1 ok2}, {ok3, ok4}] 
	private List<Set<T>> getTpPreprocessed(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> tp = new Vector<Set<T>>();
		for (int i=0; i<expectedResult.size(); i++){
			Set<T> exp = expectedResult.get(i);
			Set<T> comp = computedResult.get(i);
			tp.add(getSingleTp(exp, comp, m));
		}
		return tp;
	}

	public List<Set<T>> getTp(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> computedResultPrep = m.preProcessOutput(computedResult);
		List<Set<T>> expectedResultPrep = m.preProcessGoldStandard(expectedResult);
		return getTpPreprocessed(expectedResultPrep, computedResultPrep, m);
	}

	// {ok1, nok, nok, ok2} -> {ok1 ok2}
	//implementation of the tp function
	public Set<T> getSingleTp(Set<T> expectedResult, Set<T> computedResult, MatchRelation<T> m){
		Set<T> tpsi = new HashSet<T>();
		for (T a1 : computedResult)
			for (T a2: expectedResult)
				if (m.match(a1, a2)){
					tpsi.add(a1);
					break;
				}
		return tpsi;
	}

	//[{ok1, nok1, nok2, ok2}, {ok3, ok4, nok3}] -> [{nok1 nok2}, {nok3}] 
	private List<Set<T>> getFpPreprocessed(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> fp = new Vector<Set<T>>();
		for (int i=0; i<expectedResult.size(); i++){
			Set<T> exp = expectedResult.get(i);
			Set<T> comp = computedResult.get(i);
			fp.add(getSingleFp(exp, comp, m));
		}
		return fp;
	}

	public List<Set<T>> getFp(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> computedResultPrep = m.preProcessOutput(computedResult);
		List<Set<T>> expectedResultPrep = m.preProcessGoldStandard(expectedResult);
		return getFpPreprocessed(expectedResultPrep, computedResultPrep, m);
	}

	// {ok1, nok1, nok2, ok2} -> {nok1, nok2}
	//implementation of the fp function
	public Set<T> getSingleFp(Set<T> expectedResult, Set<T> computedResult, MatchRelation<T> m){
		Set<T> fpsi = new HashSet<T>();
		for (T a1 : computedResult){
			boolean found = false;
			for (T a2: expectedResult)
				if (m.match(a1, a2)){
					found = true;
					break;
				}
			if (!found)
				fpsi.add(a1);
		}
		return fpsi;
	}

	private List<Set<T>> getFnPreprocessed(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> fn = new Vector<Set<T>>();
		for (int i=0; i<expectedResult.size(); i++){
			Set<T> exp = expectedResult.get(i);
			Set<T> comp = computedResult.get(i);
			fn.add(getSingleFn(exp, comp, m));
		}
		return fn;
	}

	public List<Set<T>> getFn(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> computedResultPrep = m.preProcessOutput(computedResult);
		List<Set<T>> expectedResultPrep = m.preProcessGoldStandard(expectedResult);
		return getFnPreprocessed(expectedResultPrep, computedResultPrep, m);
	}

	//implementation of the fn function
	public Set<T> getSingleFn(Set<T> expectedResult, Set<T> computedResult, MatchRelation<T> m){
		Set<T> fnsi = new HashSet<T>();
		for (T a1 : expectedResult){
			boolean found = false;
			for (T a2: computedResult)
				if (m.match(a1, a2)){
					found = true;
					break;
				}
			if (!found)
				fnsi.add(a1);
		}
		return fnsi;
	}

	/**
	 * @param tps the true positives for each document.
	 * @param fps the false positives for each document (in the same ordering as tps).
	 * @return the macro-precision.
	 */
	public float macroPrecision(int[] tps, int[] fps){
		float macroPrec = 0;
		float precisions[] = precisions(tps, fps);
		for (int i=0; i<tps.length; i++)
			macroPrec += precisions[i];
		macroPrec /= tps.length;
		return macroPrec;
	}

	public float[] precisions(int[] tps, int[] fps) {
		float[] precisions = new float[tps.length];
		for (int i=0; i<tps.length; i++)
			precisions[i] = precision(tps[i], fps[i]);
		return precisions;
	}

	
	/**
	 * @param tps the true positives for each document.
	 * @param fps the false positives for each document (in the same ordering as tps).
	 * @param fns the false negatives for each document (in the same ordering as tps).
	 * @return the macro-recall.
	 */
	public float macroRecall(int[] tps, int[] fps, int[] fns){
		float macroRec = 0;
		float[] recalls = recalls(tps, fps, fns);
		for (int i=0; i<tps.length; i++)
			macroRec += recalls[i];
		macroRec /= tps.length;
		return macroRec;
	}
	
	public float[] recalls(int[] tps, int[] fps, int[] fns) {
		float[] recalls = new float[tps.length];
		for (int i=0; i<tps.length; i++)
			recalls[i] = recall(tps[i], fps[i], fns[i]);
		return recalls;
	}


	public float macroF1(int[] tps, int[] fps, int[] fns) {
		float macroF1 = 0;
		float[] f1s = f1s(tps, fps, fns);
		for (int i=0; i<tps.length; i++)
			macroF1 += f1s[i];
		macroF1 /= tps.length;
		return macroF1;
	}

	public float[] f1s(int[] tps, int[] fps, int[] fns) {
		float[] f1s = new float[tps.length];
		for (int i=0; i<tps.length; i++)
			f1s[i] = F1(recall(tps[i], fps[i], fns[i]), precision(tps[i], fps[i]));
		return f1s;
	}


	/**
	 * @param expectedResult the expected results for each document in a dataset, that is, for any document, the set of annotation in the gold standard.
	 * @param computedResult the annotations found by a tagger for each document. The ordering of the documents in the list must be the same as that in expectedResults.
	 * @return the true positives.
	 */
	private int tpCountPreprocessed(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		int tp = 0;
		for (int tpi : singleTpCount(expectedResult, computedResult, m))
			tp+=tpi;
		return tp;
	}

	public int tpCount(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> computedResultPrep = m.preProcessOutput(computedResult);
		List<Set<T>> expectedResultPrep = m.preProcessGoldStandard(expectedResult);
		return tpCountPreprocessed(expectedResultPrep, computedResultPrep, m);
	}

	/**
	 * @param expectedResult the expected results for each document in a dataset, that is, for any document, the set of annotation in the gold standard.
	 * @param computedResult the annotations found by a tagger for each document. The ordering of the documents in the list must be the same as that in expectedResults.
	 * @return the false positives.
	 */
	private int fpCountPreprocessed(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		int fp = 0;
		for (int fpi : singleFpCount(expectedResult, computedResult, m))
			fp+=fpi;
		return fp;
	}

	public int fpCount(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> computedResultPrep = m.preProcessOutput(computedResult);
		List<Set<T>> expectedResultPrep = m.preProcessGoldStandard(expectedResult);
		return fpCountPreprocessed(expectedResultPrep, computedResultPrep, m);
	}


	/**
	 * @param expectedResult the expected results for each document in a dataset, that is, for any document, the set of annotation in the gold standard.
	 * @param computedResult the annotations found by a tagger for each document. The ordering of the documents in the list must be the same as that in expectedResults. Also the size of the two lists must be the same.
	 * @return the false negatives.
	 */
	private int fnCountPreprocessed(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){		
		int fn = 0;
		for (int fni : singleFnCount(expectedResult, computedResult, m))
			fn+=fni;
		return fn;
	}

	public int fnCount(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		List<Set<T>> computedResultPrep = m.preProcessOutput(computedResult);
		List<Set<T>> expectedResultPrep = m.preProcessGoldStandard(expectedResult);
		return fnCountPreprocessed(expectedResultPrep, computedResultPrep, m);
	}

	/**
	 * @param expectedResult the gold standard.
	 * @param computedResult the annotations found by a tagger for each document, in the same ordering as expectedResults.
	 * @return an array that, for each element, contains the number of true positives, keeping the ordering and size of the lists given by argument.
	 */
	private int[] singleTpCount(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		int[] tps = new int[computedResult.size()];
		for (int i=0 ; i < computedResult.size(); i++)
			tps[i] = getSingleTp(expectedResult.get(i), computedResult.get(i), m).size();
		return tps;
	}

	/**
	 * @param expectedResult the gold standard.
	 * @param computedResult the annotations found by a tagger for each document, in the same ordering as expectedResults.
	 * @return an array that, for each element, contains the number of false positives, keeping the ordering and size of the lists given by argument.
	 */
	private int[] singleFpCount(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){
		int[] fps = new int[computedResult.size()];
		for (int i =0 ; i < computedResult.size(); i++)
			fps[i] = getSingleFp(expectedResult.get(i), computedResult.get(i), m).size();
		return fps;
	}

	/**
	 * @param expectedResult the gold standard.
	 * @param computedResult the annotations found by a tagger for each document, in the same ordering as expectedResults.
	 * @return an array that, for each element, contains the number of false negatives, keeping the ordering and size of the lists given by argument.
	 */
	private int[] singleFnCount(List<Set<T>> expectedResult, List<Set<T>> computedResult, MatchRelation<T> m){		
		int[] fns = new int[expectedResult.size()];
		for (int i =0 ; i < expectedResult.size(); i++)
			fns[i] += getSingleFn(expectedResult.get(i), computedResult.get(i), m).size();
		return fns;
	}

}
