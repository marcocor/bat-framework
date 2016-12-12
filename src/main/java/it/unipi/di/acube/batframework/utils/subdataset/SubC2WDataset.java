package it.unipi.di.acube.batframework.utils.subdataset;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.C2WDataset;

public class SubC2WDataset implements C2WDataset {
	private List<String> texts = new Vector<String>();
	private List<HashSet<Tag>> tags = new Vector<HashSet<Tag>>();
	private String name;
	private int tagsCount;

	public SubC2WDataset(C2WDataset ds, int first, int last) {
		texts = ds.getTextInstanceList().subList(first, last);
		tags = ds.getC2WGoldStandardList().subList(first, last);
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
}