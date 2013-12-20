package it.acubelab.batframework.datasetPlugins;

import java.io.*;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import it.acubelab.batframework.data.RelatednessRecord;
import it.acubelab.batframework.problems.RelatednessDataset;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class WikipediaSimilarity353 implements RelatednessDataset {
	List<RelatednessRecord> goldStandard = new Vector<RelatednessRecord>();

	private static class WikipediaSimilarity353Record {
		int id1, id2;
		String title1, title2;
		float relatedness;
	}

	public WikipediaSimilarity353(String filename, WikipediaApiInterface wikiApi)
			throws Exception {
		try {
			CSVReader r = new CSVReader(new FileReader(filename));
			String[] line = r.readNext(); // Skip first line
			Vector<WikipediaSimilarity353Record> records = new Vector<WikipediaSimilarity353Record>();
			while ((line = r.readNext()) != null) {
				if (line.length != 7) {
					r.close();
					throw new RuntimeException("Line does not have 7 tokens.");
				}
				String entity1Str = line[1];
				String entity2Str = line[4];
				if (!entity1Str.equals("null") && !entity2Str.equals("null")) {
					WikipediaSimilarity353Record rec = new WikipediaSimilarity353Record();
					records.add(rec);
					rec.id1 = Integer.parseInt(entity1Str);
					rec.id2 = Integer.parseInt(entity2Str);
					rec.title1 = line[2];
					rec.title2 = line[5];
					rec.relatedness = Float.parseFloat(line[6]) / 10f;
				}
			}
			r.close();
			List<Integer> widsToPrefetch = new Vector<Integer>();
			List<String> titlesToPrefetch = new Vector<String>();

			for (WikipediaSimilarity353Record rec : records) {
				widsToPrefetch.add(rec.id1);
				widsToPrefetch.add(rec.id2);
				titlesToPrefetch.add(rec.title1);
				titlesToPrefetch.add(rec.title2);
			}
			wikiApi.prefetchTitles(titlesToPrefetch);
			wikiApi.prefetchWids(widsToPrefetch);
			wikiApi.flush();
			for (WikipediaSimilarity353Record rec : records) {
				// If the WID is valid, use it, otherwise search for the title.
				int actualId1 = !wikiApi.getTitlebyId(rec.id1).equals("") ? rec.id1
						: wikiApi.getIdByTitle(rec.title1);
				int actualId2 = !wikiApi.getTitlebyId(rec.id2).equals("") ? rec.id2
						: wikiApi.getIdByTitle(rec.title2);
				if (actualId1 != -1 && actualId2 != -1)
					goldStandard.add(new RelatednessRecord(wikiApi.dereference(actualId1), wikiApi.dereference(actualId2),
							rec.relatedness));
				else if (actualId1 == -1)
					System.err
							.printf("Dataset is malformed. Nor id=%d, nor title=%s exist.%n",
									rec.id1, rec.title1);
				else if (actualId2 == -1)
					System.err
							.printf("Dataset is malformed. Nor id=%d, nor title=%s exist.%n",
									rec.id2, rec.title2);

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		if (goldStandard.size() != 311)
			throw new RuntimeException(
					"File should contain 311 records, contains "
							+ goldStandard.size() + ".");
	}

	@Override
	public List<RelatednessRecord> getGoldStandard() {
		return goldStandard;
	}

}
