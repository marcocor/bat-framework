/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.cache;

import it.acubelab.batframework.data.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.AnnotatingCallback;

import java.io.*;
import java.util.*;

 /** 
 * @author Marco Cornolti
 */
public class BenchmarkCache {

	private static String resultsCacheFilename;
	private static BenchmarkResults resultsCache = new BenchmarkResults();
	private static boolean modified = false;
	private static Date lastFlush = new Date();

	public static void merge(String resultsCache2Filename) throws Exception{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resultsCache2Filename));
		BenchmarkResults resultsCache2 = (BenchmarkResults) ois.readObject();
		ois.close();
		resultsCache.merge(resultsCache2);
		modified = true;
		flush();
	}

	public static void useCache(String resultsCacheFilename) throws FileNotFoundException, IOException, ClassNotFoundException{
		BenchmarkCache.resultsCacheFilename = resultsCacheFilename;

		if (new File(resultsCacheFilename).exists()){
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resultsCacheFilename));
			resultsCache = (BenchmarkResults) ois.readObject();
			ois.close();
		}
	}

	public static void flush() throws FileNotFoundException, IOException{
		lastFlush = new Date();
		if (modified && resultsCacheFilename != null){
			new File(resultsCacheFilename).createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(resultsCacheFilename));
			oos.writeObject(resultsCache);
			oos.close();
		}
	}

	public static List<Set<ScoredAnnotation>> doSa2WAnnotations(Sa2WSystem annotator, TopicDataset ds, AnnotatingCallback callback, int msec) throws Exception {
		List<Set<ScoredAnnotation>> computedAnns = new Vector<Set<ScoredAnnotation>>();
		Date lastCallback = new Date();
		int doneDocs = 0;
		int foundAnns = 0;
		for (String doc: ds.getTextInstanceList()){
			Set<ScoredAnnotation> res = resultsCache.getSa2WResult(annotator.getName(), doc);
			if (res == null){
				modified = true;
				res = annotator.solveSa2W(doc);
				resultsCache.putSa2WResult(annotator.getName(), doc, res);
				resultsCache.putSa2WTiming(annotator.getName(), ds.getName(), doc, annotator.getLastAnnotationTime());
			}
			computedAnns.add(res);
			doneDocs++;
			foundAnns += res.size();
			
			//if more than 10 minutes have passed since last flush, flush again!
			if (new Date().getTime() - lastFlush.getTime() > 1000*60*10)
				flush();
			//if more than <msec> seconds have passed, call the callback function.
			if (callback != null && new Date().getTime() - lastCallback.getTime() > msec){
				lastCallback = new Date();
				callback.run(msec, doneDocs, ds.getSize(), foundAnns);
			}
		}
		return computedAnns;
	}

	/**Use the given tagger to annotate the whole dataset.
	 * @param annotator the system used to tag the dataset.
	 * @param datasetTexts the documents of the dataset.
	 * @return a list containing the annotations found by the tagger.
	 * The annotations are in the same order of the documents given by argument.
	 * @throws Exception 
	 */
	public static List<Set<Annotation>> doA2WAnnotations(A2WSystem annotator, C2WDataset ds) throws Exception{
		List<Set<Annotation>> computedAnns = new Vector<Set<Annotation>>();
		for (String doc : ds.getTextInstanceList()){
			Set<Annotation> res = resultsCache.getA2WResult(annotator.getName(), doc);
			if (res == null){
				modified = true;
				res = annotator.solveA2W(doc);
				resultsCache.putA2WResult(annotator.getName(), doc, res);
				resultsCache.putA2WTiming(annotator.getName(), ds.getName(), doc, annotator.getLastAnnotationTime());
			}
			computedAnns.add(res);
		}
		return computedAnns;
	}
	
	public static List<Set<Annotation>> doD2WAnnotations(D2WSystem annotator, D2WDataset ds, AnnotatingCallback callback, int msec) throws Exception {
		List<Set<Annotation>> computedAnns = new ArrayList<Set<Annotation>>();
		Date lastCallback = new Date();
		int doneDocs = 0;
		int foundAnns = 0;
		for (int i = 0; i < ds.getTextInstanceList().size(); i++){
			String doc = ds.getTextInstanceList().get(i);
			Set<Mention> mentions = ds.getMentionsInstanceList().get(i);
			
			Set<Annotation> res = resultsCache.getD2WResult(annotator.getName(), doc);
			if (res == null){
				modified = true;
				res = annotator.solveD2W(doc, mentions);
				resultsCache.putD2WResult(annotator.getName(), doc, res);
				resultsCache.putD2WTiming(annotator.getName(), ds.getName(), doc, annotator.getLastAnnotationTime());
			}
			computedAnns.add(res);
			doneDocs++;
			foundAnns += res.size();
			
			//if more than 10 minutes have passed since last flush, flush again!
			if (new Date().getTime() - lastFlush.getTime() > 1000*60*10)
				flush();
			//if more than <msec> seconds have passed, call the callback function.
			if (callback != null && new Date().getTime() - lastCallback.getTime() > msec){
				lastCallback = new Date();
				callback.run(msec, doneDocs, ds.getSize(), foundAnns);
			}
		}
		return computedAnns;
	}

	public static List<Set<Tag>> doC2WTags(C2WSystem tagger, C2WDataset ds) throws Exception{
		List<Set<Tag>> computedTags = new Vector<Set<Tag>>();
		for (String doc: ds.getTextInstanceList()){
			Set<Tag> res = resultsCache.getC2WResult(tagger.getName(), doc);
			if (res == null){
				modified = true;
				res = tagger.solveC2W(doc);
				resultsCache.putC2WResult(tagger.getName(), doc, res);
				resultsCache.putC2WTiming(tagger.getName(), ds.getName(), doc, tagger.getLastAnnotationTime());
			}
			computedTags.add(res);
		}
		return computedTags;
	}

	public static List<Set<ScoredTag>> doSc2WTags(Sc2WSystem tagger, C2WDataset ds) throws Exception {
		List<Set<ScoredTag>> computedTags = new Vector<Set<ScoredTag>>();
		for (String doc: ds.getTextInstanceList()){
			Set<ScoredTag> res = resultsCache.getSc2WResult(tagger.getName(), doc);
			if (res == null){
				modified = true;
				res = tagger.solveSc2W(doc);
				resultsCache.putSc2WResult(tagger.getName(), doc, res);
				resultsCache.putSc2WTiming(tagger.getName(), ds.getName(), doc, tagger.getLastAnnotationTime());
			}
			computedTags.add(res);
		}
		return computedTags;
	}

	public static long getC2WTiming(String annotatorName, String datasetName, String text) throws Exception{
		return resultsCache.getC2WTiming(annotatorName, datasetName, text);
	}
	public static long getA2WTiming(String annotatorName, String datasetName, String text) throws Exception{
		return resultsCache.getA2WTiming(annotatorName, datasetName, text);
	}
	public static long getSa2WTiming(String annotatorName, String datasetName, String text) throws Exception{
		return resultsCache.getSa2WTiming(annotatorName, datasetName, text);
	}

	public static List<Long> getC2WTimingsForDataset(String taggerName, String datasetName) throws Exception{
		return resultsCache.getC2WTimingsForDataset(taggerName, datasetName);
	}

	public static List<Long> getA2WTimingsForDataset(String taggerName, String datasetName) throws Exception{
		return resultsCache.getA2WTimingsForDataset(taggerName, datasetName);
	}

	public static List<Long> getSa2WTimingsForDataset(String taggerName, String datasetName) throws Exception{
		return resultsCache.getSa2WTimingsForDataset(taggerName, datasetName);
	}

	public static float getAvgC2WTimingsForDataset(String taggerName, String datasetName) throws Exception{
		long sum=0;
		List<Long> timings = getC2WTimingsForDataset(taggerName, datasetName);
		for (long t: timings)
			sum += t;
		return (float)sum/(float)timings.size();
	}

	public static float getAvgA2WTimingsForDataset(String taggerName, String datasetName) throws Exception{
		long sum=0;
		List<Long> timings = getA2WTimingsForDataset(taggerName, datasetName);
		for (long t: timings)
			sum += t;
		return (float)sum/(float)timings.size();
	}

	public static float getAvgSa2WTimingsForDataset(String taggerName, String datasetName) throws Exception{
		long sum=0;
		List<Long> timings = getSa2WTimingsForDataset(taggerName, datasetName);
		for (long t: timings)
			sum += t;
		return (float)sum/(float)timings.size();
	}
	
	public static String getLongestSa2WTimeDoc(String taggerName, String datasetName){
		return resultsCache.getLongestSa2WTimeDoc(taggerName, datasetName);
	}
	
	public static String getCacheInfo(){
		return resultsCache.dumpInfo();
	}
}
