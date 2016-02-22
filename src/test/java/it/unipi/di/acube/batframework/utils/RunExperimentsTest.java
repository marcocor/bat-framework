package it.unipi.di.acube.batframework.utils;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.metrics.MetricsResultSet;
import it.unipi.di.acube.batframework.problems.D2WDataset;
import it.unipi.di.acube.batframework.problems.MentionSpotter;
import it.unipi.di.acube.batframework.utils.RunExperiments;

import org.junit.Before;
import org.junit.Test;

public class RunExperimentsTest {
	private static final double DELTA = 1e-6;

	private final String textA = "Obama saw Silvio Berlusconi in a pink suite.";
	private final HashSet<Mention> mentionA1 = new HashSet<Mention>();
	private final HashSet<Mention> mentionA2 = new HashSet<Mention>();

	private final String textB = "The cat is an intelligent feline.";
	private final HashSet<Mention> mentionB1 = new HashSet<Mention>();
	private final HashSet<Mention> mentionB2 = new HashSet<Mention>();

	@Before
	public void setUp() throws Exception {
		mentionA1.add(new Mention(0, 5));
		mentionA1.add(new Mention(10, 17));

		mentionA2.add(new Mention(0, 5));
		mentionA2.add(new Mention(10, 10));
		mentionA2.add(new Mention(38, 5));

		mentionB1.add(new Mention(4, 3));

		mentionB2.add(new Mention(4, 3));
		mentionB2.add(new Mention(14, 11));

	}

	@Test
	public void testPerformMentionSpottingExp() throws Exception {
		MetricsResultSet res = RunExperiments.performMentionSpottingExp(
				new TestAnnotator(), new TestDataset());
		assertEquals(1 / 3f, res.getPrecisions(0), DELTA);
		assertEquals(1 / 2f, res.getRecalls(0), DELTA);
		assertEquals(2f / 5, res.getF1s(0), DELTA);
		assertEquals(1 / 2f, res.getPrecisions(1), DELTA);
		assertEquals(1f, res.getRecalls(1), DELTA);
		assertEquals(2f / 3, res.getF1s(1), DELTA);
		assertEquals(1, res.getGlobalFn());
		assertEquals(3, res.getGlobalFp());
		assertEquals(2, res.getGlobalTp());
		assertEquals(5 / 12f, res.getMacroPrecision(), DELTA);
		assertEquals(3 / 4f, res.getMacroRecall(), DELTA);
		assertEquals(8 / 15f, res.getMacroF1(), DELTA);
	}

	private class TestDataset implements D2WDataset {

		@Override
		public int getSize() {
			return 2;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public List<String> getTextInstanceList() {
			List<String> texts = new Vector<String>();
			texts.add(textA);
			texts.add(textB);
			return texts;
		}

		@Override
		public List<HashSet<Mention>> getMentionsInstanceList() {
			List<HashSet<Mention>> mentions = new Vector<HashSet<Mention>>();
			mentions.add(mentionA1);
			mentions.add(mentionB1);
			return mentions;
		}

		@Override
		public List<HashSet<Annotation>> getD2WGoldStandardList() {
			return null;
		}

	}

	private class TestAnnotator implements MentionSpotter {
		@Override
		public String getName() {
			return "Test Annotator";
		}

		@Override
		public long getLastAnnotationTime() {
			return 0;
		}

		@Override
		public HashSet<Mention> getSpottedMentions(String text) {
			if (text.equals(textA))
				return mentionA2;
			else
				return mentionB2;
		}
	}
}
