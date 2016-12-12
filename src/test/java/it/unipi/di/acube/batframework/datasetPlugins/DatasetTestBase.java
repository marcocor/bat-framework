package it.unipi.di.acube.batframework.datasetPlugins;

import java.util.HashSet;

import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.C2WDataset;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;
import it.unipi.di.acube.batframework.utils.WikipediaInterface;

import org.junit.Test;

import static org.junit.Assert.*;

public abstract class DatasetTestBase {
	public abstract C2WDataset build(WikipediaInterface i) throws Exception;
	
	@Test
	public void test() throws Exception {
		WikipediaInterface i = WikipediaApiInterface.api();
		C2WDataset ds = build(i);
		assertNotNull(ds.getName());
		assertNotNull(ds.getC2WGoldStandardList());
		assertNotNull(ds.getTextInstanceList());
		assertTrue(ds.getSize() >= 1);
		assertTrue(ds.getSize() == ds.getC2WGoldStandardList().size());
		assertTrue(ds.getSize() == ds.getTextInstanceList().size());
		int nConcepts = 0;
		for (HashSet<Tag> c: ds.getC2WGoldStandardList())
			nConcepts += c.size();
		//There must be at least one entity in the whole dataset.
		assertTrue(nConcepts > 0);
	}
}
