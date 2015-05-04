package it.unipi.di.acube.batframework.problems;

import it.unipi.di.acube.batframework.data.RelatednessRecord;

import java.util.List;


public interface RelatednessDataset {
	public List<RelatednessRecord> getGoldStandard();
}
