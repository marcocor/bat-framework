package it.unipi.di.acube.batframework.datasetPlugins;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.Pair;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

public class CsvDataset implements A2WDataset {
	String name;
	List<String> docIds;
	List<HashSet<Annotation>> gold;

	public CsvDataset(String filename, String name)
			throws NumberFormatException, AnnotationException, IOException {
		this.name = name;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		String line = null;
		HashMap<String, HashSet<Annotation>> dsHm = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(",");
			if (tokens.length != 4){
				br.close();
				throw new RuntimeException(String.format(
						"Line in file %s malformed: [%s]", filename, line));
			}
			String docId = tokens[0];
			int start = Integer.parseInt(tokens[1]);
			int end = Integer.parseInt(tokens[2]);
			int wikiId = Integer.parseInt(tokens[3]);
			if (start < 0 || end < 0 || wikiId < 0){
				br.close();
				throw new RuntimeException(
						"start, end and wikipediaId must be greater that zero.");
			}
			if (!dsHm.containsKey(docId))
				dsHm.put(docId, new HashSet<Annotation>());
			dsHm.get(docId).add(
					new Annotation(start, end - start, wikiId));
		}
		br.close();
		
		docIds = new Vector<>(dsHm.keySet());
		Collections.sort(docIds);
		
		gold = new Vector<>();
		for (String docId : docIds)
			gold.add(dsHm.get(docId));
	}

	@Override
	public int getSize() {
		return docIds.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (HashSet<Annotation> s: gold)
			count += s.size();
		return count;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(this.getA2WGoldStandardList());
	}
	
	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return getA2WGoldStandardList();
	}

	@Override
	public List<String> getTextInstanceList() {
		return this.docIds;
	}
	
	@Override
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return gold;
	}

}
