package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.*;

import java.util.Set;

/**
 * A Candidate spotter is a system that, given a text, returns the mentions
 * spotted in the text plus a list of candidates for each mention. Implementing
 * this interface, one can test the coverage (recall) of the candidates
 * selection during the development of a system. Class
 * {@link it.acubelab.batframework.utils.RunExperiments} contains methods for
 * such a test.
 * 
 */
public interface CandidatesSpotter {
	/**
	 * @param text
	 *            the text to process
	 * @return the set of spotted multi-annotations, which are an association
	 *         between a mention and a set of candidates.
	 */
	public Set<MultipleAnnotation> getSpottedCandidates(String text);

}
