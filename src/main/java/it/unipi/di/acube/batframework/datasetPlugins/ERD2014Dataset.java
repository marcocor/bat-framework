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

import java.io.*;
import java.util.*;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.FreebaseApi;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

public class ERD2014Dataset implements A2WDataset {
	List<String> queries = new Vector<>();
	List<HashSet<Annotation>> annotations = new Vector<>();
		
	public ERD2014Dataset(String queryFile, String annotationFile, FreebaseApi freebApi, WikipediaApiInterface wikiApi) throws IOException{
		Map<String, Integer> trecIdToIndex = new HashMap<>();
		
		BufferedReader queryBr = new BufferedReader(new FileReader(queryFile));
		String line;
		while ((line = queryBr.readLine()) != null){
			String[] tokens = line.split("\t");
			if (tokens.length != 2)
				continue;
			System.out.println(line);
			trecIdToIndex.put(tokens[0], queries.size());
			queries.add(tokens[1]);
			annotations.add(new HashSet<Annotation>());
		}
		queryBr.close();

		BufferedReader annotationsBr = new BufferedReader(new FileReader(annotationFile));
		while ((line = annotationsBr.readLine()) != null){
			String[] tokens = line.split("\t");
			int index = trecIdToIndex.get(tokens[0]);
			String query = queries.get(index);
			System.out.println(query);
			System.out.println(tokens[3]);
			int position = query.indexOf(tokens[3]);
			int length =  tokens[3].length();
			String freeBaseMID = tokens[2];
			String title = freebApi.midToTitle(freeBaseMID);
			if (title != null)
				annotations.get(index).add(new Annotation(position, length, wikiApi.getIdByTitle(title)));
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
