package it.unipi.di.acube.batframework.datasetPlugins;

import it.unipi.di.acube.batframework.problems.C2WDataset;


public class MSNBCDatasetTest extends DatasetTestBase{

	@Override
	public C2WDataset build() throws Exception {
		return DatasetBuilder.getMSNBC();
	}

}
