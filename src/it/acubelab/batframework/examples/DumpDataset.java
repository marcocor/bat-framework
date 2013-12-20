/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.examples;

import it.acubelab.batframework.datasetPlugins.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

import java.io.*;

public class DumpDataset {

	public static void main(String[] args) throws Exception {
		//Creating the API to Wikipedia.
		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid.cache", "benchmark/cache/redirect.cache");
		
		//Creating the IITB dataset.
		A2WDataset ds = new IITBDataset("benchmark/datasets/iitb/crawledDocs", "benchmark/datasets/iitb/CSAW_Annotations.xml", api);

		System.out.println("Printing basic information about dataset " + ds.getName());
		//Basic check & Dump basic information
		TestDataset.dumpInfo(ds, api);
		
		//Dump of the whole dataset
		DumpData.dumpDataset(ds.getTextInstanceList(), ds.getA2WGoldStandardList(), api);
		
		//Export to XML
		Exporter.exportA2WDataset(ds, new FileOutputStream("iitb.xml"), api);
		api.flush();
	}
}
