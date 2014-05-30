package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.utils.AnnotationException;

import java.util.HashSet;

public interface D2WSystem extends TopicSystem {
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException;
}
