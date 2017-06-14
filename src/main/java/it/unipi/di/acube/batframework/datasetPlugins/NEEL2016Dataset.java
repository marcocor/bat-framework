package it.unipi.di.acube.batframework.datasetPlugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaInterface;
import it.unipi.di.acube.batframework.utils.WikipediaLocalInterface;

public class NEEL2016Dataset implements A2WDataset {
	private static Pattern tweetsRE = Pattern.compile("^\\|(\\d+)\\|,\\|(.*)\\|$");
	private List<String> text;
	private List<HashSet<Annotation>> gold;
	private String portion;
	  private static final Charset UTF_8 = Charset.forName("UTF-8");

	public NEEL2016Dataset(InputStream annotationsIs, InputStream textIs, WikipediaInterface wikiApi, String portion)
	        throws IOException {
		this.portion = portion;
		HashMap<Long, String> idToBody = new HashMap<>();
		{
			LineIterator itText = IOUtils.lineIterator(textIs, "utf8");
			try {
				while (itText.hasNext()) {
					String line = itText.nextLine();
					Matcher m = tweetsRE.matcher(line);
					if (!m.matches())
						throw new IllegalArgumentException();
					long docId = Long.parseLong(m.group(1));
					String body = new String(m.group(2).getBytes(UTF_8), UTF_8);
					idToBody.put(docId, body);
				}
			} finally {
				LineIterator.closeQuietly(itText);
			}
		}

		HashMap<Long, HashSet<Annotation>> idToAnnotations = new HashMap<>();
		{
			LineIterator itAnnotations = IOUtils.lineIterator(annotationsIs, "utf8");
			try {
				while (itAnnotations.hasNext()) {
					String[] fields = itAnnotations.nextLine().split("\t");
					if (fields.length != 6)
						throw new IllegalArgumentException();
					long docId = Long.parseLong(fields[0]);
					int start = Integer.parseInt(fields[1]);
					int end = Integer.parseInt(fields[2]);
					String entity = fields[3];
					if (entity.startsWith("NIL"))
						continue;
					int wid = wikiApi.dereference(wikiApi.getIdByTitle(WikipediaLocalInterface.dbPediaUrlToTitle(entity)));

					if (!idToAnnotations.containsKey(docId))
						idToAnnotations.put(docId, new HashSet<Annotation>());
					idToAnnotations.get(docId).add(new Annotation(start, end - start, wid));
				}
			} finally {
				LineIterator.closeQuietly(itAnnotations);
			}
		}

		List<Long> docIds = new Vector<>(idToBody.keySet());
		Collections.sort(docIds);

		text = new Vector<>();
		for (long docId : docIds)
			text.add(idToBody.get(docId));

		gold = new Vector<>();
		for (long docId : docIds)
			if (idToAnnotations.containsKey(docId))
				gold.add(idToAnnotations.get(docId));
			else
				gold.add(new HashSet<Annotation>());
	}

	@Override
	public int getSize() {
		return text.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (HashSet<Annotation> s : gold)
			count += s.size();
		return count;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(this.getA2WGoldStandardList());
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return getA2WGoldStandardList();
	}

	@Override
	public List<String> getTextInstanceList() {
		return text;
	}

	@Override
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return "#Microposts2016 NEEL " + portion;
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return gold;
	}

}
