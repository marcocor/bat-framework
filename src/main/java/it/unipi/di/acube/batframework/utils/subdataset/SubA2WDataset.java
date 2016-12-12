package it.unipi.di.acube.batframework.utils.subdataset;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

public class SubA2WDataset implements A2WDataset {
	private List<String> texts = new Vector<String>();
	private List<HashSet<Tag>> tags = new Vector<HashSet<Tag>>();
	private List<HashSet<Annotation>> annotations = new Vector<HashSet<Annotation>>();
	private String name;
	private int tagsCount;

	public SubA2WDataset(A2WDataset ds, int first, int last) {
		texts = ds.getTextInstanceList().subList(first, last);
		tags = ds.getC2WGoldStandardList().subList(first, last);
		annotations = ds.getA2WGoldStandardList().subList(first, last);
		name = String.format("%s [%d-%d]", ds.getName(), first, last);

		tagsCount = 0;
		for (HashSet<Tag> tagSet : tags)
			tagsCount += tagSet.size();

	}

	@Override
	public int getTagsCount() {
		return tagsCount;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return tags;
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
		return ProblemReduction.A2WToD2WMentionsInstance(this.getA2WGoldStandardList());
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return annotations;
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return annotations;
	}

}