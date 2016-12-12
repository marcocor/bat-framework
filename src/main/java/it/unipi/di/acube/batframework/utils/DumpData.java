/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.utils;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.ScoredAnnotation;
import it.unipi.di.acube.batframework.data.ScoredTag;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.metrics.MatchRelation;

import java.io.IOException;
import java.util.*;

/**
 * Utility methods to dump a dataset.
 */
public class DumpData {

	/**
	 * Dump an entire dataset.
	 * 
	 * @param texts
	 *            the instances of the dataset.
	 * @param gs
	 *            the gold standard (must be of the same size as {@code texts}).
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param <T1>
	 *            the type of this dataset's gold standard.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends Tag> void dumpDataset(List<String> texts,
			List<HashSet<T1>> gs, WikipediaInterface api) throws IOException {
		for (int i = 0; i < texts.size(); i++)
			dumpCompare(texts.get(i), gs.get(i), null, api);
	}

	/**
	 * Dump an entire output of a tagger for a dataset.
	 * 
	 * @param texts
	 *            the instances of the dataset.
	 * @param output
	 *            the output computed by a tagger (must be of the same size as
	 *            {@code texts}).
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param <T1>
	 *            the type of this output's gold standard.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends Tag> void dumpOutput(List<String> texts,
			List<HashSet<T1>> output, WikipediaInterface api)
			throws IOException {
		for (int i = 0; i < texts.size(); i++)
			dumpCompare(texts.get(i), null, output.get(i), api);
	}

	/**
	 * Dump, for each document of a dataset, the expected output (gold standard)
	 * and the actual output (found by an annotator).
	 * 
	 * @param texts
	 *            the instances of the dataset.
	 * @param expectedResult
	 *            the gold standard provided by a dataset, one for each instance
	 *            (must have the same size as {@code texts}).
	 * @param computedResult
	 *            the solution found by an annotator, one for each instance
	 *            (must have the same size as {@code texts}).
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param <T>
	 *            the type of this list elements.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T extends Tag> void dumpCompareList(List<String> texts,
			List<HashSet<T>> expectedResult, List<HashSet<T>> computedResult,
			WikipediaInterface api) throws IOException {
		dumpCompareList(texts, expectedResult, computedResult, api, true);

	}

	/**
	 * Dump, for each document of a dataset, the expected output (gold standard)
	 * and the actual output (found by an annotator).
	 * 
	 * @param texts
	 *            the instances of the dataset.
	 * @param expectedResult
	 *            the gold standard provided by a dataset, one for each instance
	 *            (must have the same size as {@code texts}).
	 * @param computedResult
	 *            the solution found by an annotator, one for each instance
	 *            (must have the same size as {@code texts}).
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param printEmptyDocs
	 *            whether or not to print documents with an empty gold standard
	 *            and an empty solution.
	 * @param <T>
	 *            the type of these lists elements.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T extends Tag> void dumpCompareList(List<String> texts,
			List<HashSet<T>> expectedResult, List<HashSet<T>> computedResult,
			WikipediaInterface api, boolean printEmptyDocs)
			throws IOException {
		dumpCompareList(texts, expectedResult, computedResult, api, printEmptyDocs, null);
	}
	
	/**
	 * Dump, for each document of a dataset, the expected output (gold standard)
	 * and the actual output (found by an annotator).
	 * 
	 * @param texts
	 *            the instances of the dataset.
	 * @param expectedResult
	 *            the gold standard provided by a dataset, one for each instance
	 *            (must have the same size as {@code texts}).
	 * @param computedResult
	 *            the solution found by an annotator, one for each instance
	 *            (must have the same size as {@code texts}).
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param printEmptyDocs
	 *            whether or not to print documents with an empty gold standard
	 *            and an empty solution.
	 * @param mr
	 *            match relation used to dump annotations about TP/FP/FN
	 * @param <T>
	 *            the type of these lists elements.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T extends Tag> void dumpCompareList(List<String> texts,
			List<HashSet<T>> expectedResult, List<HashSet<T>> computedResult,
			WikipediaInterface api, boolean printEmptyDocs, MatchRelation<T> mr)
			throws IOException {
		for (int i = 0; i < texts.size(); i++) {
			if (printEmptyDocs
					|| (!printEmptyDocs && (!expectedResult.get(i).isEmpty() || !computedResult
							.get(i).isEmpty()))) {
				DumpData.dumpCompareMatch(texts.get(i), expectedResult.get(i),
						computedResult.get(i), mr, api);
				System.out.println();
			}
		}

	}

	/**
	 * Dumps the text, the annotations provided by the gold standard and those
	 * found by a tagger for a single document.
	 * 
	 * @param text
	 *            the document.
	 * @param expectedResult
	 *            the expected results provided by a dataset (if {@code null},
	 *            it is not printed).
	 * @param computedResult
	 *            the results found by an annotator (if {@code null}, it is not
	 *            printed).
	 * @param api
	 *            the API to Wikipedia (needed to print information about
	 *            annotations/tags).
	 * @param mr
	 *            a match relation to compare the results.
	 * @param <T>
	 *            the type of result.
	 * @throws IOException
	 *             if something went wrong while querying the Wikipedia API.
	 */
	public static <T extends Tag> void dumpCompareMatch(String text,
			HashSet<T> expectedResult, HashSet<T> computedResult,
			MatchRelation<T> mr, WikipediaInterface api) throws IOException {
		System.out.println("Text: " + text);
		if (expectedResult != null) {
			System.out.println();
			System.out.println("Gold standard: ");
			for (T a : expectedResult) {
				String note = "";
				if (mr != null) {
					note = "FN";
					for (T t : computedResult)
						if (mr.match(t, a))
							note = "";
				}
				printAnnotation(text, a, api, note);
			}
		}
		if (computedResult != null) {
			System.out.println();
			System.out.println("System output: ");
			List<T> list = new Vector<T>();
			for (T t : computedResult)
				list.add(t);
			Collections.sort(list);
			for (T a : list) {
				String note = "";
				if (mr != null) {
					note = "FP";
					for (T t : expectedResult)
						if (mr.match(a, t))
							note = "TP";
				}
				printAnnotation(text, a, api, note);
			}
		}
	}

	public static <T extends Tag> void dumpCompare(String text,
			HashSet<T> expectedResult, HashSet<T> computedResult,
			WikipediaInterface api) throws IOException {
		dumpCompareMatch(text, expectedResult, computedResult, null, api);
	}

	private static <T extends Tag> void printAnnotation(String text, T a,
			WikipediaInterface api, String note) throws IOException {
		if (a instanceof ScoredAnnotation)
			System.out.printf("\t%s: %s -> %s (wid=%d) (score=%.3f)%n", note,
					text.substring(((ScoredAnnotation) a).getPosition(),
							((Annotation) a).getPosition()
									+ ((ScoredAnnotation) a).getLength()), api
							.getTitlebyId(a.getConcept()), a.getConcept(),
					((ScoredAnnotation) a).getScore());
		else if (a instanceof Annotation)
			System.out.printf(
					"\t%s: %s (%d, %d) -> %s (%d)%n",
					note,
					text.substring(
							((Annotation) a).getPosition(),
							((Annotation) a).getPosition()
									+ ((Annotation) a).getLength()),
					((Annotation) a).getPosition(),
					((Annotation) a).getPosition()
							+ ((Annotation) a).getLength(), api.getTitlebyId(a
							.getConcept()), a.getConcept());
		else if (a instanceof ScoredTag)
			System.out.printf("\t%s: %s (wid=%d) (score=%.3f)%n", note,
					api.getTitlebyId(a.getConcept()), a.getConcept(),
					((ScoredTag) a).getScore());
		else if (a instanceof Tag)
			System.out.printf("\t%s: %s (%d)", note,
					api.getTitlebyId(a.getConcept()), a.getConcept());

	}

}
