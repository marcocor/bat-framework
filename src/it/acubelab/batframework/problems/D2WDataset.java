package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;

import java.util.List;
import java.util.Set;

public interface D2WDataset extends TopicDataset {
	public List<Set<Mention>> getMentionsInstanceList();

	public List<Set<Annotation>> getD2WGoldStandardList();
}
