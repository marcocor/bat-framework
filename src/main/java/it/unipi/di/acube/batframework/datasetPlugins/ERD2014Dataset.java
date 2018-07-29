/**
 *  Copyright 2014 Marco Cornolti
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package it.unipi.di.acube.batframework.datasetPlugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaInterface;

public class ERD2014Dataset implements A2WDataset {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	List<String> queries = new Vector<>();
	List<HashSet<Annotation>> annotations = new Vector<>();

	public ERD2014Dataset(String queryFile, String annotationFile, WikipediaInterface wikiApi) throws IOException, JSONException {
		this(new FileInputStream(new File(queryFile)), new FileInputStream(new File(annotationFile)), wikiApi);
	}

	public ERD2014Dataset(InputStream queryStream, InputStream annotationStream, WikipediaInterface wikiApi)
	        throws IOException, JSONException {
		Map<String, Integer> trecIdToIndex = new HashMap<>();

		BufferedReader queryBr = new BufferedReader(new InputStreamReader(queryStream));
		String line;
		while ((line = queryBr.readLine()) != null) {
			String[] tokens = line.split("\t");
			if (tokens.length != 2)
				continue;
			trecIdToIndex.put(tokens[0], queries.size());
			queries.add(tokens[1]);
			annotations.add(new HashSet<Annotation>());
		}
		queryBr.close();

		BufferedReader annotationsBr = new BufferedReader(new InputStreamReader(annotationStream));
		while ((line = annotationsBr.readLine()) != null) {
			String[] tokens = line.split("\t");
			int index = trecIdToIndex.get(tokens[0]);
			String query = queries.get(index);
			int position = query.indexOf(tokens[3]);
			int length = tokens[3].length();
			String title = tokens[2];
			int wid = wikiApi.getIdByTitle(title);
			if (wid == -1) {
				LOG.warn("Could not resolve Wikipedia title " + title);
				continue;
			}
			Annotation ann = new Annotation(position, length, wid);
			if (annotations.get(index).stream().noneMatch(a -> a.overlaps(ann)))
				annotations.get(index).add(ann);
		}
		annotationsBr.close();
	}

	@Override
	public int getSize() {
		return queries.size();
	}

	@Override
	public String getName() {
		return "ERD2014";
	}

	@Override
	public List<String> getTextInstanceList() {
		return queries;
	}

	@Override
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getD2WGoldStandardList());
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
		return annotations;
	}

	@Override
	public int getTagsCount() {
		int sum = 0;
		for (HashSet<Annotation> annS : annotations)
			sum += annS.size();
		return sum;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(getA2WGoldStandardList());
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
		return annotations;
	}

}
