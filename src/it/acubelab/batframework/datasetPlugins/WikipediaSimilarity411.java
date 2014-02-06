package it.acubelab.batframework.datasetPlugins;

import java.io.*;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import it.acubelab.batframework.data.RelatednessRecord;
import it.acubelab.batframework.problems.RelatednessDataset;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class WikipediaSimilarity411 implements RelatednessDataset {
	List<RelatednessRecord> goldStandard = new Vector<RelatednessRecord>();

	private static class WikipediaSimilarity411Record {
		int id1, id2;
		String title1, title2;
		float relatedness;
	}

	public WikipediaSimilarity411(String filename, WikipediaApiInterface wikiApi)
			throws Exception {
		try {
			CSVReader r = new CSVReader(new FileReader(filename));
			String[] line = r.readNext(); // Skip first line
			Vector<WikipediaSimilarity411Record> records = new Vector<WikipediaSimilarity411Record>();
			while ((line = r.readNext()) != null) {
				if (line.length != 7) {
					r.close();
					throw new RuntimeException("Line does not have 7 tokens.");
				}
				String entity1Str = line[1];
				String entity2Str = line[4];
				if (!entity1Str.equals("null") && !entity2Str.equals("null")) {
					WikipediaSimilarity411Record rec = new WikipediaSimilarity411Record();
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

			for (WikipediaSimilarity411Record rec : records) {
				widsToPrefetch.add(rec.id1);
				widsToPrefetch.add(rec.id2);
				titlesToPrefetch.add(rec.title1);
				titlesToPrefetch.add(rec.title2);
			}
			wikiApi.prefetchTitles(titlesToPrefetch);
			wikiApi.prefetchWids(widsToPrefetch);
			wikiApi.flush();
			for (WikipediaSimilarity411Record rec : records) {
				// If the WID and the title match, use it, otherwise throw an
				// error
				if (wikiApi.getTitlebyId(rec.id1).equals("")
						| wikiApi.getTitlebyId(rec.id2).equals("")
						| wikiApi.getIdByTitle(rec.title1) == -1
						| wikiApi.getIdByTitle(rec.title2) == -1) {
					if (wikiApi.getTitlebyId(rec.id1).equals(""))
						System.err.printf(
								"Dataset is malformed. ID=%d not valid.%n",
								rec.id1);
					if (wikiApi.getTitlebyId(rec.id2).equals(""))
						System.err.printf(
								"Dataset is malformed. ID=%d not valid.%n",
								rec.id2);
					if (wikiApi.getIdByTitle(rec.title1) == -1)
						System.err.printf(
								"Dataset is malformed. Title=%s not valid.%n",
								rec.title1);
					if (wikiApi.getIdByTitle(rec.title2) == -1)
						System.err.printf(
								"Dataset is malformed. Title=%s not valid.%n",
								rec.title2);
					continue;
				}
				if (!wikiApi.getTitlebyId(rec.id1).equals(rec.title1) | wikiApi.getIdByTitle(rec.title1) != rec.id1){
					System.err.printf(
							"Dataset is malformed. Title=%s and ID=%d refer to distinct pages!%n",
							rec.title1, rec.id1);
					continue;
				}
				if (!wikiApi.getTitlebyId(rec.id2).equals(rec.title2) | wikiApi.getIdByTitle(rec.title2) != rec.id2){
					System.err.printf(
							"Dataset is malformed. Title=%s and ID=%d refer to distinct pages!%n",
							rec.title2, rec.id2);
					continue;
				}
					
				// If the WID is valid, use it, otherwise search for the title.
				goldStandard.add(new RelatednessRecord(wikiApi
							.dereference(rec.id1), wikiApi
							.dereference(rec.id2), rec.relatedness));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		if (goldStandard.size() != 411)
			throw new RuntimeException(
					"File should contain 411 records, contains "
							+ goldStandard.size() + ".");
	}

	@Override
	public List<RelatednessRecord> getGoldStandard() {
		return goldStandard;
	}

}
