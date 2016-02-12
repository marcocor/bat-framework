package it.unipi.di.acube.batframework.systemPlugins;

import java.util.HashMap;
import java.util.HashSet;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Sa2WSystem;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

public class MockAnnotator implements Sa2WSystem {

	private HashMap<String, HashSet<ScoredAnnotation>> answers;
	private String name;

	public MockAnnotator(HashMap<String, HashSet<ScoredAnnotation>> answers,
			String name) {
		this.answers = answers;
		this.name = name;
	}

	@Override
	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(answers.get(text));
	}

	@Override
	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	@Override
	public String getName() {
		return name;
	}
	
	public void appendName(String suffix){
		name += suffix;
	}

	@Override
	public long getLastAnnotationTime() {
		return 0;
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions)
			throws AnnotationException {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions,
				Float.NEGATIVE_INFINITY);
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(solveSa2W(text));
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String text)
			throws AnnotationException {
		return answers.get(text);
	}

}
