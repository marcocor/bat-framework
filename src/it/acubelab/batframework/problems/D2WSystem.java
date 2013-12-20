package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.utils.AnnotationException;

import java.util.Set;

public interface D2WSystem extends TopicSystem {
	public Set<Annotation> solveD2W(String text, Set<Mention> mentions) throws AnnotationException;
}
