package it.unipi.di.acube.batframework.utils.subdataset;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.problems.D2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

public class SubD2WDataset implements D2WDataset {
	private List<String> texts = new Vector<String>();
	private List<HashSet<Annotation>> annotations = new Vector<HashSet<Annotation>>();
	private String name;

	public SubD2WDataset(D2WDataset ds, int first, int last) {
		texts = ds.getTextInstanceList().subList(first, last);
		annotations = ds.getD2WGoldStandardList().subList(first, last);
		name = String.format("%s [%d-%d]", ds.getName(), first, last);
	}

	@Override
	public int getSize() {
		return texts.size();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getTextInstanceList() {
		return texts;
	}

	@Override
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(annotations);
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return annotations;
	}

}