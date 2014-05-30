package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;

import java.util.HashSet;
import java.util.List;
import java.util.HashSet;

public interface D2WDataset extends TopicDataset {
	public List<HashSet<Mention>> getMentionsInstanceList();

	public List<HashSet<Annotation>> getD2WGoldStandardList();
}
