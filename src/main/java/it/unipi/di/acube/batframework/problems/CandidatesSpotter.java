package it.unipi.di.acube.batframework.problems;

import it.unipi.di.acube.batframework.data.*;

import java.util.HashSet;

/**
 * A Candidate spotter is a system that, given a text, returns the mentions
 * spotted in the text plus a list of candidates for each mention. Implementing
 * this interface, one can test the coverage (recall) of the candidates
 * selection during the development of a system. Class
 * {@link it.unipi.di.acube.batframework.utils.RunExperiments} contains methods for
 * such a test.
 * 
 */
public interface CandidatesSpotter extends TopicSystem {
	/**
	 * @param text
	 *            the text to process
	 * @return the set of spotted multi-annotations, which are an association
	 *         between a mention and a set of candidates.
	 */
	public HashSet<MultipleAnnotation> getSpottedCandidates(String text);

}
