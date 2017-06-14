package it.unipi.di.acube.batframework.utils.datasets;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.problems.D2WDataset;
import it.unipi.di.acube.batframework.problems.C2WDataset;

public class MergeDatasets {

	public static A2WDataset mergeA2WDatasets(final A2WDataset... dss) {
		return new A2WDataset() {
			private int tagsCount = 0;
			private List<HashSet<Tag>> c2WGoldStandardList = new Vector<>();
			private int size = 0;
			private String name;
			private List<String> textInstances = new Vector<>();
			private List<HashSet<Mention>> mentionsInstances = new Vector<>();
			private List<HashSet<Annotation>> d2wGold = new Vector<>();
			private List<HashSet<Annotation>> a2wGold = new Vector<>();

			{
				List<String> names = new Vector<>();
				for (A2WDataset ds : dss) {
					tagsCount += ds.getTagsCount();
					c2WGoldStandardList.addAll(ds.getC2WGoldStandardList());
					size += ds.getSize();
					names.add(ds.getName());
					textInstances.addAll(ds.getTextInstanceList());
					mentionsInstances.addAll(ds.getMentionsInstanceList());
					d2wGold.addAll(ds.getD2WGoldStandardList());
					a2wGold.addAll(ds.getA2WGoldStandardList());
				}
				name = "Merged: " + String.join(",", names);
			}

			@Override
			public int getTagsCount() {
				return tagsCount;
			}

			@Override
			public List<HashSet<Tag>> getC2WGoldStandardList() {
				return c2WGoldStandardList;
			}

			@Override
			public int getSize() {
				return size;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<String> getTextInstanceList() {
				return textInstances;
			}

			@Override
			public List<HashSet<Mention>> getMentionsInstanceList() {
				return mentionsInstances;
			}

			@Override
			public List<HashSet<Annotation>> getD2WGoldStandardList() {
				return d2wGold;
			}

			@Override
			public List<HashSet<Annotation>> getA2WGoldStandardList() {
				return a2wGold;
			}
		};

	}
	

	public static C2WDataset mergeC2WDatasets(final C2WDataset... dss) {
		return new C2WDataset() {
			private int tagsCount = 0;
			private List<HashSet<Tag>> c2WGoldStandardList = new Vector<>();
			private int size = 0;
			private String name;
			private List<String> textInstances = new Vector<>();

			{
				List<String> names = new Vector<>();
				for (C2WDataset ds : dss) {
					tagsCount += ds.getTagsCount();
					c2WGoldStandardList.addAll(ds.getC2WGoldStandardList());
					size += ds.getSize();
					names.add(ds.getName());
					textInstances.addAll(ds.getTextInstanceList());
				}
				name = "Merged: " + String.join(",", names);
			}

			@Override
			public int getTagsCount() {
				return tagsCount;
			}

			@Override
			public List<HashSet<Tag>> getC2WGoldStandardList() {
				return c2WGoldStandardList;
			}

			@Override
			public int getSize() {
				return size;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<String> getTextInstanceList() {
				return textInstances;
			}
		};

	}
	
	public static D2WDataset mergeD2WDatasets(final D2WDataset... dss) {
		return new D2WDataset() {
			private int size = 0;
			private String name;
			private List<String> textInstances = new Vector<>();
			private List<HashSet<Mention>> mentionsInstances = new Vector<>();
			private List<HashSet<Annotation>> d2wGold = new Vector<>();

			{
				List<String> names = new Vector<>();
				for (D2WDataset ds : dss) {
					size += ds.getSize();
					names.add(ds.getName());
					textInstances.addAll(ds.getTextInstanceList());
					mentionsInstances.addAll(ds.getMentionsInstanceList());
					d2wGold.addAll(ds.getD2WGoldStandardList());
				}
				name = "Merged: " + String.join(",", names);
			}

			@Override
			public int getSize() {
				return size;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<String> getTextInstanceList() {
				return textInstances;
			}

			@Override
			public List<HashSet<Mention>> getMentionsInstanceList() {
				return mentionsInstances;
			}

			@Override
			public List<HashSet<Annotation>> getD2WGoldStandardList() {
				return d2wGold;
			}
		};

	}
}
