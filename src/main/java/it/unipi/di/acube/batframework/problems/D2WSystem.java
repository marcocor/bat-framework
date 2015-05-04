package it.unipi.di.acube.batframework.problems;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.utils.AnnotationException;

import java.util.HashSet;

public interface D2WSystem extends TopicSystem {
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions) throws AnnotationException;
}
