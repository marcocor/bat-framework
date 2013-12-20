package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.RelatednessRecord;

import java.util.List;


public interface RelatednessDataset {
	public List<RelatednessRecord> getGoldStandard();
}
