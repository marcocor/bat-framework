package it.unipi.di.acube.batframework.problems;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;

import java.util.HashSet;
import java.util.List;

public interface D2WDataset extends TopicDataset {
	public List<HashSet<Mention>> getMentionsInstanceList();

	public List<HashSet<Annotation>> getD2WGoldStandardList();
}
