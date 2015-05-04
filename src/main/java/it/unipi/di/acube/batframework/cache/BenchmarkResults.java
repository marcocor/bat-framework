/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.cache;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.TopicDataset;
import it.unipi.di.acube.batframework.utils.Pair;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Marco Cornolti
 */
public class BenchmarkResults implements Serializable {
	private static final long serialVersionUID = 1L;

	// mapping: tagger name -> <document, mentions> -> set of annotations
	private HashMap<String, HashMap<String, HashSet<Annotation>>> D2WCache = new HashMap<String, HashMap<String, HashSet<Annotation>>>();

	// mapping: tagger name -> document -> set of tags
	private HashMap<String, HashMap<String, HashSet<Annotation>>> A2WCache = new HashMap<String, HashMap<String, HashSet<Annotation>>>();

	// mapping: tagger name -> document -> set of scored tags
	private HashMap<String, HashMap<String, HashSet<ScoredAnnotation>>> Sa2WCache = new HashMap<String, HashMap<String, HashSet<ScoredAnnotation>>>();

	// mapping: tagger name -> document -> set of annotations
	private HashMap<String, HashMap<String, HashSet<Tag>>> C2WCache = new HashMap<String, HashMap<String, HashSet<Tag>>>();

	// mapping: tagger name -> document -> set of annotations
	private HashMap<String, HashMap<String, HashSet<ScoredTag>>> Sc2WCache = new HashMap<String, HashMap<String, HashSet<ScoredTag>>>();

	// mapping: tagger name -> document -> set of mentions
	private HashMap<String, HashMap<String, HashSet<Mention>>> mentionSpotCache = new HashMap<String, HashMap<String, HashSet<Mention>>>();

	// mapping: tagger name -> dataset name -> document -> time in milliseconds
	private HashMap<String, HashMap<String, HashMap<String, Long>>> D2Wtimings = new HashMap<String, HashMap<String, HashMap<String, Long>>>();
	private HashMap<String, HashMap<String, HashMap<String, Long>>> A2Wtimings = new HashMap<String, HashMap<String, HashMap<String, Long>>>();
	private HashMap<String, HashMap<String, HashMap<String, Long>>> Sa2Wtimings = new HashMap<String, HashMap<String, HashMap<String, Long>>>();
	private HashMap<String, HashMap<String, HashMap<String, Long>>> C2Wtimings = new HashMap<String, HashMap<String, HashMap<String, Long>>>();
	private HashMap<String, HashMap<String, HashMap<String, Long>>> Sc2Wtimings = new HashMap<String, HashMap<String, HashMap<String, Long>>>();

	public void putC2WTiming(String taggerName, String datasetName,
			String text, long time) throws Exception {
		putTiming(C2Wtimings, taggerName, datasetName, text, time);
	}

	public void putD2WTiming(String taggerName, String datasetName,
			String text, long time) throws Exception {
		putTiming(D2Wtimings, taggerName, datasetName, text, time);
	}

	public void putSa2WTiming(String taggerName, String datasetName,
			String text, long time) throws Exception {
		putTiming(Sa2Wtimings, taggerName, datasetName, text, time);
	}

	public void putA2WTiming(String taggerName, String datasetName,
			String text, long time) throws Exception {
		putTiming(A2Wtimings, taggerName, datasetName, text, time);
	}

	public void putSc2WTiming(String taggerName, String datasetName,
			String text, long time) throws Exception {
		putTiming(Sc2Wtimings, taggerName, datasetName, text, time);
	}

	private void putTiming(
			HashMap<String, HashMap<String, HashMap<String, Long>>> timings,
			String taggerName, String datasetName, String text, long time)
			throws Exception {
		if (!timings.containsKey(taggerName))
			timings.put(taggerName,
					new HashMap<String, HashMap<String, Long>>());
		HashMap<String, HashMap<String, Long>> firstLevel = timings
				.get(taggerName);

		if (!firstLevel.containsKey(datasetName))
			firstLevel.put(datasetName, new HashMap<String, Long>());
		HashMap<String, Long> secondLevel = firstLevel.get(datasetName);

		if (!secondLevel.containsKey(text))
			secondLevel.put(text, time);
		/*
		 * else if (secondLevel.containsKey(text) &&
		 * secondLevel.get(text)!=time) throw new
		 * Exception("Timing already present for tagger: "
		 * +taggerName+", dataset: "+datasetName+" text:"+text.substring(0,
		 * Math.min(20, text.length()-1)).replace("\n", "")+"...");
		 */}

	public long getC2WTiming(String taggerName, String datasetName, String text)
			throws Exception {
		return getTiming(C2Wtimings, taggerName, datasetName, text);
	}

	public long getD2WTiming(String taggerName, String datasetName, String text)
			throws Exception {
		return getTiming(D2Wtimings, taggerName, datasetName, text);
	}

	public long getA2WTiming(String taggerName, String datasetName, String text)
			throws Exception {
		return getTiming(A2Wtimings, taggerName, datasetName, text);
	}

	public long getSa2WTiming(String taggerName, String datasetName, String text)
			throws Exception {
		return getTiming(Sa2Wtimings, taggerName, datasetName, text);
	}

	private long getTiming(
			HashMap<String, HashMap<String, HashMap<String, Long>>> timings,
			String taggerName, String datasetName, String text)
			throws Exception {
		HashMap<String, HashMap<String, Long>> firstLevel = timings
				.get(taggerName);
		if (firstLevel == null)
			throw new Exception(
					"Could not retrieve information about timing for tagger "
							+ taggerName);

		HashMap<String, Long> secondLevel = firstLevel.get(datasetName);
		if (secondLevel == null)
			throw new Exception(
					"Could not retrieve information about timing for tagger "
							+ taggerName + " and dataset " + datasetName);

		Long timing = secondLevel.get(text);
		if (timing == null)
			throw new Exception(
					"Could not retrieve information about timing for tagger "
							+ taggerName + ", dataset " + datasetName
							+ ", and text: "
							+ text.substring(0, 20).replace("\n", "") + "...");

		return timing;
	}

	public void putD2WResult(String annotatorName, String doc,
			HashSet<Mention> mentions, HashSet<Annotation> res) {
		HashMap<String, HashSet<Annotation>> annotatorCache;
		if ((annotatorCache = D2WCache.get(annotatorName)) == null) {
			annotatorCache = new HashMap<String, HashSet<Annotation>>();
			D2WCache.put(annotatorName, annotatorCache);
		}
		annotatorCache.put(d2wInputRepresentation(doc, mentions), res);
	}

	public HashSet<Annotation> getD2WResult(String annotatorName, String text,
			HashSet<Mention> mentions) {
		HashMap<String, HashSet<Annotation>> annotatorCache;
		if ((annotatorCache = D2WCache.get(annotatorName)) == null) {
			annotatorCache = new HashMap<String, HashSet<Annotation>>();
			D2WCache.put(annotatorName, annotatorCache);
		}
		return annotatorCache.get(d2wInputRepresentation(text, mentions));
	}

	public void putSa2WResult(String annotatorName, String doc,
			HashSet<ScoredAnnotation> res) {
		HashMap<String, HashSet<ScoredAnnotation>> annotatorCache;
		if ((annotatorCache = Sa2WCache.get(annotatorName)) == null) {
			annotatorCache = new HashMap<String, HashSet<ScoredAnnotation>>();
			Sa2WCache.put(annotatorName, annotatorCache);
		}
		annotatorCache.put(doc, res);
	}

	public HashSet<ScoredAnnotation> getSa2WResult(String annotatorName,
			String text) {
		HashMap<String, HashSet<ScoredAnnotation>> annotatorCache;
		if ((annotatorCache = Sa2WCache.get(annotatorName)) == null) {
			annotatorCache = new HashMap<String, HashSet<ScoredAnnotation>>();
			Sa2WCache.put(annotatorName, annotatorCache);
		}
		return annotatorCache.get(text);
	}

	public void putA2WResult(String annotatorName, String doc,
			HashSet<Annotation> res) {
		HashMap<String, HashSet<Annotation>> annotatorCache;
		if ((annotatorCache = A2WCache.get(annotatorName)) == null) {
			annotatorCache = new HashMap<String, HashSet<Annotation>>();
			A2WCache.put(annotatorName, annotatorCache);
		}
		annotatorCache.put(doc, res);
	}

	public void putSc2WResult(String taggerName, String doc,
			HashSet<ScoredTag> res) {
		HashMap<String, HashSet<ScoredTag>> taggerCache;
		if ((taggerCache = Sc2WCache.get(taggerName)) == null) {
			taggerCache = new HashMap<String, HashSet<ScoredTag>>();
			Sc2WCache.put(taggerName, taggerCache);
		}
		taggerCache.put(doc, res);

	}

	public HashSet<Annotation> getA2WResult(String annotatorName, String doc) {
		HashMap<String, HashSet<Annotation>> annotatorCache;
		if ((annotatorCache = A2WCache.get(annotatorName)) == null) {
			annotatorCache = new HashMap<String, HashSet<Annotation>>();
			A2WCache.put(annotatorName, annotatorCache);
		}
		return annotatorCache.get(doc);
	}

	public void putC2WResult(String taggerName, String doc, HashSet<Tag> res) {
		HashMap<String, HashSet<Tag>> taggerCache;
		if ((taggerCache = C2WCache.get(taggerName)) == null) {
			taggerCache = new HashMap<String, HashSet<Tag>>();
			C2WCache.put(taggerName, taggerCache);
		}
		taggerCache.put(doc, res);
	}

	public HashSet<Tag> getC2WResult(String taggerName, String doc) {
		HashMap<String, HashSet<Tag>> taggerCache;
		if ((taggerCache = C2WCache.get(taggerName)) == null) {
			taggerCache = new HashMap<String, HashSet<Tag>>();
			C2WCache.put(taggerName, taggerCache);
		}
		return taggerCache.get(doc);
	}

	public HashSet<ScoredTag> getSc2WResult(String taggerName, String doc) {
		HashMap<String, HashSet<ScoredTag>> taggerCache;
		if ((taggerCache = Sc2WCache.get(taggerName)) == null) {
			taggerCache = new HashMap<String, HashSet<ScoredTag>>();
			Sc2WCache.put(taggerName, taggerCache);
		}
		return taggerCache.get(doc);
	}

	public void putSpotMentionsResult(String spotterName, String doc,
			HashSet<Mention> res) {
		HashMap<String, HashSet<Mention>> taggerCache;
		if ((taggerCache = mentionSpotCache.get(spotterName)) == null) {
			taggerCache = new HashMap<String, HashSet<Mention>>();
			mentionSpotCache.put(spotterName, taggerCache);
		}
		taggerCache.put(doc, res);
	}

	public HashSet<Mention> getSpotMentionsResult(String spotterName, String doc) {
		HashMap<String, HashSet<Mention>> taggerCache;
		if ((taggerCache = mentionSpotCache.get(spotterName)) == null) {
			taggerCache = new HashMap<String, HashSet<Mention>>();
			mentionSpotCache.put(spotterName, taggerCache);
		}
		return taggerCache.get(doc);
	}

	public Vector<Long> getC2WTimingsForDataset(String taggerName,
			String datasetName) {
		return getTimingsForDataset(C2Wtimings, taggerName, datasetName);
	}

	public Vector<Long> getA2WTimingsForDataset(String annotatorName,
			String datasetName) {
		return getTimingsForDataset(A2Wtimings, annotatorName, datasetName);
	}

	public Vector<Long> getSa2WTimingsForDataset(String annotatorName,
			String datasetName) {
		return getTimingsForDataset(Sa2Wtimings, annotatorName, datasetName);
	}

	private Vector<Long> getTimingsForDataset(
			HashMap<String, HashMap<String, HashMap<String, Long>>> timings,
			String taggerName, String datasetName) {
		return new Vector<Long>(timings.get(taggerName).get(datasetName)
				.values());
	}

	public void invalidateResults(String taggerName) {
		A2Wtimings.remove(taggerName);
		D2Wtimings.remove(taggerName);
		Sa2Wtimings.remove(taggerName);
		Sc2Wtimings.remove(taggerName);
		C2Wtimings.remove(taggerName);

		A2WCache.remove(taggerName);
		D2WCache.remove(taggerName);
		C2WCache.remove(taggerName);
		Sa2WCache.remove(taggerName);
		Sc2WCache.remove(taggerName);
		mentionSpotCache.remove(taggerName);
	}

	public void invalidateResults(String taggerName, TopicDataset dataset) {
		for (String doc : dataset.getTextInstanceList()) {
			if (A2WCache.containsKey(taggerName))
				A2WCache.get(taggerName).remove(doc);
			if (C2WCache.containsKey(taggerName))
				C2WCache.get(taggerName).remove(doc);
			if (Sa2WCache.containsKey(taggerName))
				Sa2WCache.get(taggerName).remove(doc);
			if (Sc2WCache.containsKey(taggerName))
				Sc2WCache.get(taggerName).remove(doc);
		}

		if (A2Wtimings.containsKey(taggerName))
			A2Wtimings.get(taggerName).remove(dataset.getName());
		if (C2Wtimings.containsKey(taggerName))
			C2Wtimings.get(taggerName).remove(dataset.getName());
		if (Sa2Wtimings.containsKey(taggerName))
			Sa2Wtimings.get(taggerName).remove(dataset.getName());
		if (Sc2Wtimings.containsKey(taggerName))
			Sc2Wtimings.get(taggerName).remove(dataset.getName());

	}

	public void merge(BenchmarkResults resultsCache2) throws Exception {
		for (String tagger : resultsCache2.A2WCache.keySet())
			for (String doc : resultsCache2.A2WCache.get(tagger).keySet())
				this.putA2WResult(tagger, doc,
						resultsCache2.A2WCache.get(tagger).get(doc));
		for (String tagger : resultsCache2.Sa2WCache.keySet())
			for (String doc : resultsCache2.Sa2WCache.get(tagger).keySet())
				this.putSa2WResult(tagger, doc,
						resultsCache2.Sa2WCache.get(tagger).get(doc));
		for (String tagger : resultsCache2.C2WCache.keySet())
			for (String doc : resultsCache2.C2WCache.get(tagger).keySet())
				this.putC2WResult(tagger, doc,
						resultsCache2.C2WCache.get(tagger).get(doc));
		for (String tagger : resultsCache2.Sc2WCache.keySet())
			for (String doc : resultsCache2.Sc2WCache.get(tagger).keySet())
				this.putSc2WResult(tagger, doc,
						resultsCache2.Sc2WCache.get(tagger).get(doc));

		for (String tagger : resultsCache2.A2Wtimings.keySet())
			for (String ds : resultsCache2.A2Wtimings.get(tagger).keySet())
				for (String doc : resultsCache2.A2Wtimings.get(tagger).get(ds)
						.keySet())
					this.putA2WTiming(tagger, ds, doc, resultsCache2.A2Wtimings
							.get(tagger).get(ds).get(doc));
		for (String tagger : resultsCache2.Sa2Wtimings.keySet())
			for (String ds : resultsCache2.Sa2Wtimings.get(tagger).keySet())
				for (String doc : resultsCache2.Sa2Wtimings.get(tagger).get(ds)
						.keySet())
					this.putSa2WTiming(
							tagger,
							ds,
							doc,
							resultsCache2.Sa2Wtimings.get(tagger).get(ds)
									.get(doc));
		for (String tagger : resultsCache2.C2Wtimings.keySet())
			for (String ds : resultsCache2.C2Wtimings.get(tagger).keySet())
				for (String doc : resultsCache2.C2Wtimings.get(tagger).get(ds)
						.keySet())
					this.putC2WTiming(tagger, ds, doc, resultsCache2.C2Wtimings
							.get(tagger).get(ds).get(doc));
		for (String tagger : resultsCache2.Sc2Wtimings.keySet())
			for (String ds : resultsCache2.Sc2Wtimings.get(tagger).keySet())
				for (String doc : resultsCache2.Sc2Wtimings.get(tagger).get(ds)
						.keySet())
					this.putSc2WTiming(
							tagger,
							ds,
							doc,
							resultsCache2.Sc2Wtimings.get(tagger).get(ds)
									.get(doc));

	}

	public String getLongestSa2WTimeDoc(String taggerName, String datasetName) {
		HashMap<String, HashMap<String, Long>> firstLevel = Sa2Wtimings
				.get(taggerName);
		HashMap<String, Long> secondLevel = firstLevel.get(datasetName);
		long max = -1;
		String maxDoc = null;
		for (String s : secondLevel.keySet())
			if (secondLevel.get(s) > max) {
				max = secondLevel.get(s);
				maxDoc = s;
			}
		return maxDoc;
	}

	public String dumpInfo() {
		String a2wInfo = "";
		for (String tagger : A2Wtimings.keySet())
			for (String ds : A2Wtimings.get(tagger).keySet())
				a2wInfo += String.format("%s/%s (%d stored results)\n", tagger,
						ds, A2Wtimings.get(tagger).get(ds).size());
		if (a2wInfo.equals(""))
			a2wInfo = "No A2W records.\n";

		String d2wInfo = "";
		for (String tagger : D2Wtimings.keySet())
			for (String ds : D2Wtimings.get(tagger).keySet())
				d2wInfo += String.format("%s/%s (%d stored results)\n", tagger,
						ds, D2Wtimings.get(tagger).get(ds).size());
		if (d2wInfo.equals(""))
			d2wInfo = "No D2W records.\n";

		String sa2wInfo = "";
		for (String tagger : Sa2Wtimings.keySet())
			for (String ds : Sa2Wtimings.get(tagger).keySet())
				sa2wInfo += String.format("%s/%s (%d stored results)\n",
						tagger, ds, Sa2Wtimings.get(tagger).get(ds).size());
		if (sa2wInfo.equals(""))
			sa2wInfo = "No Sa2W records.\n";

		String c2wInfo = "";
		for (String tagger : C2Wtimings.keySet())
			for (String ds : C2Wtimings.get(tagger).keySet())
				c2wInfo += String.format("%s/%s (%d stored results)\n", tagger,
						ds, C2Wtimings.get(tagger).get(ds).size());
		if (c2wInfo.equals(""))
			c2wInfo = "No C2W records.\n";

		String sc2wInfo = "";
		for (String tagger : Sc2Wtimings.keySet())
			for (String ds : Sc2Wtimings.get(tagger).keySet())
				sc2wInfo += String.format("%s/%s (%d stored results)\n",
						tagger, ds, Sc2Wtimings.get(tagger).get(ds).size());
		if (sc2wInfo.equals(""))
			sc2wInfo = "No Sc2W records.\n";

		String spotterInfo = "";
		for (String tagger : mentionSpotCache.keySet())
			spotterInfo += String.format("%s (%d stored results)\n", tagger,
					mentionSpotCache.get(tagger).size());
		if (spotterInfo.equals(""))
			spotterInfo = "No Mention Spotter records.\n";

		return String
				.format("C2W annotators/dataset:%n%s%nSc2W annotators/dataset:%n%s%nA2W annotators/dataset:%n%s%nD2W annotators/dataset:%n%s%nSa2W annotators/dataset:%n%s%nMention spotters/dataset:%n%s%n",
						c2wInfo, sc2wInfo, a2wInfo, d2wInfo, sa2wInfo,
						spotterInfo);
	}

	public void invalidateD2WResults() {
		this.D2WCache.clear();
		this.D2Wtimings.clear();
	}

	public void invalidateC2WResults() {
		this.C2WCache.clear();
		this.C2Wtimings.clear();
	}

	public void invalidateSa2WResults() {
		this.Sa2WCache.clear();
		this.Sa2Wtimings.clear();
	}

	public void invalidateA2WResults() {
		this.A2WCache.clear();
		this.A2Wtimings.clear();
	}

	public void invalidateSc2WResults() {
		this.Sc2WCache.clear();
		this.Sc2Wtimings.clear();
	}

	private String d2wInputRepresentation(String doc, HashSet<Mention> mentions) {
		Vector<Mention> mentionsVec = new Vector<>();
		mentionsVec.addAll(mentions);
		Collections.sort(mentionsVec);
		String mentionsString = "";
		for (Mention m : mentionsVec)
			mentionsString += m.toString();
		try {
			return URLEncoder.encode(doc, "utf-8") + " " + mentionsString;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
