/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.examples;

import java.util.*;

import it.acubelab.batframework.cache.*;
import it.acubelab.batframework.data.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.systemPlugins.*;
import it.acubelab.batframework.utils.*;

public class DumpAnnotator {
	public static void main(String[] args) throws Exception {
		//Do not store the cached results in a file
		BenchmarkCache.useCache(null);
		
		//Create the API to wikipedia, storing retrieved data in two files, and the interface to DBPedia.
		WikipediaApiInterface wikiApi = new WikipediaApiInterface("wid.cache", "redirect.cache");
		DBPediaApi dbpediaApi = new DBPediaApi();
		
		//Create the annotator and the dummy dataset.
		Sa2WSystem annotator = new SpotlightAnnotator(dbpediaApi, wikiApi);
		A2WDataset ds = new DummyDataset();
		
		List<Set<ScoredAnnotation>> computedAnnotations = BenchmarkCache.doSa2WAnnotations(annotator, ds, null, 0);
		
		//Do some basic check on the annotator's output.
		TestAnnotator.checkOutput(ds, computedAnnotations);
		
		//Dump of the output.
		DumpData.dumpOutput(ds.getTextInstanceList(), computedAnnotations, wikiApi);
		
		BenchmarkCache.flush();
		wikiApi.flush();
	}
}
