package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.Mention;

import java.util.HashSet;

/**
 * A Mention spotter is a system that, given a text, returns the mentions
 * spotted in the text. Implementing this interface, one can test the accuracy
 * of the system ability to spot mentions (i.e. its parser). Class
 * {@link it.acubelab.batframework.utils.RunExperiments} contains methods for
 * such a test.
 * 
 */
public interface MentionSpotter extends TopicSystem {
	/**
	 * @param text the text to process.
	 * @return a set of mentions spotted in the text.
	 */
	public HashSet<Mention> getSpottedMentions(String text);
}
