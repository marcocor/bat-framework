package it.unipi.di.acube.batframework.datasetPlugins;

import it.unipi.di.acube.batframework.problems.C2WDataset;
import it.unipi.di.acube.batframework.utils.WikipediaInterface;


public class MSNBCDatasetTest extends DatasetTestBase{

	@Override
	public C2WDataset build(WikipediaInterface i) throws Exception {
		return DatasetBuilder.getMSNBC(i);
	}

}
