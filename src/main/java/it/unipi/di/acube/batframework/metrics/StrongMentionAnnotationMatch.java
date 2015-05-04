package it.unipi.di.acube.batframework.metrics;

import it.unipi.di.acube.batframework.data.Annotation;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class StrongMentionAnnotationMatch implements MatchRelation<Annotation>{
	@Override
	public boolean match(Annotation t1, Annotation t2) {
			return t1.getLength() == t2.getLength() && t1.getPosition() == t1.getPosition();
	}

	@Override
	public List<HashSet<Annotation>> preProcessOutput(List<HashSet<Annotation>> computedOutput) {
		List<HashSet<Annotation>> nonOverlappingOutput = new Vector<HashSet<Annotation>>();
		for (HashSet<Annotation> s: computedOutput)
			nonOverlappingOutput.add(Annotation.deleteOverlappingAnnotations(s));
		return nonOverlappingOutput;
	}

	@Override
	public List<HashSet<Annotation>> preProcessGoldStandard(List<HashSet<Annotation>> goldStandard) {
		return preProcessOutput(goldStandard);
	}

	@Override
	public String getName() {
		return "Strong mention annotation match";
	}
}
