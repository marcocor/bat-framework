package it.acubelab.batframework.systemPlugins;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import it.acubelab.batframework.data.Mention;

import org.junit.Test;

public class TestAgdistisAnnotator {

	@Test
	public void testCreateTextWithMentions() {
		String expected = "The <entity>University of Leipzig</entity> in <entity>Barack Obama</entity>.";

		String text = "The University of Leipzig in Barack Obama.";

		Set<Mention> mentions = new HashSet<>();
		mentions.add(new Mention(4, 21));
		mentions.add(new Mention(29, 12));

		String actual = AgdistisAnnotator.createTextWithMentions(text, mentions);

		assertEquals(expected, actual);
	}
}
