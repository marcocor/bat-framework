package it.unipi.di.acube.batframework.systemPlugins;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.systemPlugins.AgdistisAnnotator;

import org.junit.Test;

public class TestAgdistisAnnotator {

	@Test
	public void testCreateTextWithMentions() {
		String expected = "The <entity>University of Leipzig</entity> in <entity>Barack Obama</entity>.";

		String text = "The University of Leipzig in Barack Obama.";

		HashSet<Mention> mentions = new HashSet<>();
		mentions.add(new Mention(4, 21));
		mentions.add(new Mention(29, 12));

		String actual = AgdistisAnnotator.createTextWithMentions(text, mentions);

		assertEquals(expected, actual);
	}
}
