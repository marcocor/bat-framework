/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.ScoredAnnotation;
import it.acubelab.batframework.data.ScoredTag;
import it.acubelab.batframework.data.Tag;

import java.io.IOException;
import java.util.*;

/**
 * Utility methods to dump a dataset.
 */
public class DumpData {

	/**Dump an entire dataset.
	 * @param texts the instances of the dataset.
	 * @param gs the gold standard (must be of the same size as {@code texts}).
	 * @param api the API to Wikipedia (needed to print information about annotations/tags).
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends Tag> void dumpDataset(List<String> texts, List<Set<T1>> gs, WikipediaApiInterface api) throws IOException{
		for (int i=0; i<texts.size(); i++)
			dumpCompare(texts.get(i), gs.get(i), null, api);
	}

	/**Dump an entire output of a tagger for a dataset.
	 * @param texts the instances of the dataset.
	 * @param output the output computed by a tagger (must be of the same size as {@code texts}).
	 * @param api the API to Wikipedia (needed to print information about annotations/tags).
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends Tag> void dumpOutput(List<String> texts, List<Set<T1>> output, WikipediaApiInterface api) throws IOException{
		for (int i=0; i<texts.size(); i++)
			dumpCompare(texts.get(i), null, output.get(i), api);
	}

	/** Dump, for each document of a dataset, the expected output (gold standard) and the actual output (found by an annotator).
	 * @param texts the instances of the dataset.
	 * @param expectedResult the gold standard provided by a dataset, one for each instance (must have the same size as {@code texts}).
	 * @param computedResult the solution found by an annotator, one for each instance (must have the same size as {@code texts}).
	 * @param api the API to Wikipedia (needed to print information about annotations/tags).
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends Tag> void dumpCompareList(List<String> texts, List<Set<T1>> expectedResult, List<Set<T1>> computedResult, WikipediaApiInterface api) throws IOException {
		for (int i =0 ; i < texts.size(); i++){
			DumpData.dumpCompare(texts.get(i), expectedResult.get(i), computedResult.get(i), api);
			System.out.println();
		}

	}

	/**
	 * Dumps the text, the annotations provided by the gold standard and those found by a tagger for a single document.
	 * @param text the document.
	 * @param expectedResult the expected results provided by a dataset (if {@code null}, it is not printed).
	 * @param computedResult the results found by an annotator (if {@code null}, it is not printed).
	 * @param api the API to Wikipedia (needed to print information about annotations/tags).
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public static <T1 extends Tag, T2 extends Tag> void dumpCompare(String text, Set<T1> expectedResult, Set<T2> computedResult, WikipediaApiInterface api) throws IOException{
		System.out.println("Text: "+text);
		if (expectedResult != null){
			System.out.println();
			System.out.println("Expected output: ");
			for (T1 a: expectedResult){
				printAnnotation(text, a, api);
			}
		}
		if (computedResult != null){
			System.out.println();
			System.out.println("Computed output: ");
			List<T2> list = new Vector<T2>();
			for (T2 t:computedResult) list.add(t);
			Collections.sort(list);
			for (T2 a : list){
				printAnnotation(text, a, api);
			}
		}
	}

	private static <T extends Tag> void printAnnotation(String text, T a, WikipediaApiInterface api) throws IOException{
		if (a instanceof ScoredAnnotation)
			System.out.printf("\t%s -> %s (wid=%d) (score=%.3f)%n", text.substring(((ScoredAnnotation)a).getPosition(), ((Annotation)a).getPosition()+((ScoredAnnotation)a).getLength()), api.getTitlebyId(a.getConcept()), a.getConcept(), ((ScoredAnnotation)a).getScore());
		else if (a instanceof Annotation)
			System.out.printf("\t%s (%d, %d) -> %s (%d)%n", text.substring(((Annotation)a).getPosition(), ((Annotation)a).getPosition()+((Annotation)a).getLength()), ((Annotation)a).getPosition(), ((Annotation)a).getPosition()+((Annotation)a).getLength(), api.getTitlebyId(a.getConcept()), a.getConcept());
		else if (a instanceof ScoredTag)
			System.out.printf("\t%s (wid=%d) (score=%.3f)%n", api.getTitlebyId(a.getConcept()), a.getConcept(), ((ScoredTag)a).getScore());
		else if (a instanceof Tag)
			System.out.printf("\t%s (%d)", api.getTitlebyId(a.getConcept()), a.getConcept());

	}

}
