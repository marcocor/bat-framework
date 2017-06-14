/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.utils;

import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.problems.*;

import java.util.*;

/**
 * This class provides methods to test a dataset.
 */
public class TestDataset {

	/**Checks if the dataset contains redirect concepts, printing an informational message in case.
	 * @param ds the dataset.
	 * @param api the API to Wikipedia.
	 * @throws Exception if the Wikipedia API could not be queried.
	 */
	private static void checkRedirects(C2WDataset ds, WikipediaInterface api) throws Exception {
		for (HashSet<Tag> s : ds.getC2WGoldStandardList())
			for (Tag a : s)
				if (api.isRedirect(a.getConcept()))
					System.out.println("INFO: An annotation points to a redirect page! wid="+a.getConcept());
	}

	/**Makes basic testing on the dataset.
	 * @param ds the dataset to test.
	 * @return true iff the test has passed.
	 */
	private static boolean checkBasicData(C2WDataset ds) {
		if (ds.getC2WGoldStandardList().size() != ds.getSize()) {
			System.out.println("ERROR: list of texts and list of annotations sets have different size! texts="+ds.getSize()+ "anns="+ds.getC2WGoldStandardList().size());
			return false;
		}
		return true;
	}

	/**Check that dataset annotations are legit.
	 * @param ds the dataset to test.
	 * @return true iff the test has passed.
	 */
	private static void checkAnnotations(A2WDataset ds) {
		for (int docid = 0; docid < ds.getSize(); docid++) {
			String text = ds.getTextInstanceList().get(docid);
			for (Annotation a : ds.getA2WGoldStandardList().get(docid)) {
				if (a.getPosition() < 0 || a.getEnd() > text.codePointCount(0, text.length()))
					System.err.printf("ERROR: Annotation (pos=%d len=%d -> %d) is out of text boundaries. Document: %s%n",
					        a.getPosition(), a.getLength(), a.getConcept(), text);
				if (a.getPosition() >= 1
				        && Character.isLetterOrDigit(text.codePointAt(text.offsetByCodePoints(0, a.getPosition() - 1))))
					System.out.printf("WARNING: Annotation (pos=%d last=%d [%s] -> %d) breaks words on the left. Text: %s%n",
					        a.getPosition(), a.getEnd(),
					        a.getMentionString(text),
					        a.getConcept(), text);
				if (a.getEnd() <= text.codePointCount(0, text.length() - 1)
				        && Character.isLetterOrDigit(text.codePointAt(text.offsetByCodePoints(0, a.getEnd()))))
					System.out.printf("WARNING: Annotation (pos=%d last=%d [%s] -> %d) breaks words on the right. Text: %s%n",
					        a.getPosition(), a.getEnd(),
					        a.getMentionString(text),
					        a.getConcept(), text);
			}
		}
	}

	/**Dump some information about a dataset.
	 * @param ds the dataset.
	 * @param api the API to Wikipedia.
	 * @throws Exception if the Wikipedia API could not be queried.
	 */
	public static void dumpInfo(C2WDataset ds, WikipediaInterface api) throws Exception{
		
		System.out.println("Basic check on dataset " + ds.getName());
		if (!checkBasicData(ds)) return;
		System.out.println("Checking that no pages are redirects on dataset " + ds.getName());
		checkRedirects(ds, api);
		if (ds instanceof A2WDataset){
			System.out.println("Checking that all annotations are legit on dataset " + ds.getName());
			checkAnnotations((A2WDataset) ds);
		}
		
		long len = 0;
		long longest = 0;
		for (String s : ds.getTextInstanceList()) {
			if (s.length() > longest) longest = s.length();
			len+=s.length();
		}
		System.out.println("Annotations: "+ ds.getTagsCount()+ " Documents:"+ds.getSize()+ " avg. ann/doc: "+(float)ds.getTagsCount()/(float)ds.getSize()+" avg len:"+(int)((float)len/(float)ds.getSize())+" longest doc:"+longest);
		
		HashSet<Integer> distinctTopics = new HashSet<Integer>();
		int annDocs = 0;
		for (HashSet<Tag> s : ds.getC2WGoldStandardList()){
			for (Tag a : s)
				distinctTopics.add(api.dereference(a.getConcept()));
			if (!s.isEmpty())
				annDocs++;
		}
		System.out.println("Distinct Topics: "+distinctTopics.size());
		System.out.println("Dataset contains "+ annDocs + " documents with at least 1 annotation. These documents have an average number of annotations = "+ (float)ds.getTagsCount()/(float)annDocs);
	}

}
