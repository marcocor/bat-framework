/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.scripts;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.datasetPlugins.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

import java.io.*;
import java.util.Set;

public class DumpDataset {

	public static void main(String[] args) throws Exception {
		WikipediaApiInterface api = new WikipediaApiInterface("benchmark/cache/wid2.cache", "benchmark/cache/redirect2.cache");
		//WikipediaApiInterface api = new WikipediaApiInterface(null, null);

		System.out.println("Creating dataset...");
		//Rc2WDataset ds = new MeijDataset("benchmark/datasets/meij/original_tweets.list", "benchmark/datasets/meij/wsdm2012_annotations.txt", "benchmark/datasets/meij/wsdm2012_qrels.txt");
		//T2WDataset ds = new MSNBCDataset("benchmark/datasets/MSNBC/RawTextsSimpleChars_utf8", "benchmark/datasets/MSNBC/Problems", api);
		//T2WDataset ds = new AQUAINTDataset("benchmark/datasets/AQUAINT/RawTexts", "benchmark/datasets/AQUAINT/Problems", api);
		//T2WDataset ds = new ACE2004Dataset("benchmark/datasets/ACE2004_Coref_Turking/Dev/RawTextsNoTranscripts/", "benchmark/datasets/ACE2004_Coref_Turking/Dev/ProblemsNoTranscripts/", api);
		//A2WDataset ds = new ConllAidaDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset-update.tsv", api);
		//A2WDataset ds = new ConllAidaTestBDataset("benchmark/datasets/aida/AIDA-YAGO2-dataset-update.tsv", api);
		//T2WDataset ds = new DummyDataset();
		//A2WDataset ds = new KddDataset(new String[]{"benchmark/datasets/kdd/kdd_amt_d_1.txt","benchmark/datasets/kdd/kdd_amt_t_1.txt"}, api);
		A2WDataset ds = new KddDataset(new String[]{"benchmark/datasets/kdd/kdd_edit.txt"}, api);
		//A2WDataset ds = new IITBDataset("benchmark/datasets/iitb/crawledDocs", "benchmark/datasets/iitb/CSAW_Annotations.xml", api);
		//A2WDataset ds = new TimerunsDataset("benchmark/datasets/timeruns/balrog2.txt", 20,70);
		System.out.println("Printing basic information about dataset " + ds.getName());
		TestDataset.dumpInfo(ds, api);

		//A2WDataset dsBlind = new BlindDataset(ds);
		//TestDataset.dumpInfo(dsBlind, api);
		
		//DumpData.dumpDataset(ds.getTextInstanceList(), ds.getA2WGoldStandardList(), api);
		Exporter.exportA2WDataset(ds, new FileOutputStream("kdd_edit.xml"), api);
		//Exporter.exportC2WDataset(ds, System.out, api);
		/*for (int i=0; i<ds.getTextInstanceList().size(); i++){
			String text = ds.getTextInstanceList().get(i);
			System.out.printf("%s",text);
			for (int j=0; j<10; j++){
				Annotation[] gs = ds.getA2WGoldStandardList().get(i).toArray(new Annotation[0]);
				if (j < gs.length){
					System.out.printf("\t%s\t%s",
							text.substring(gs[j].getPosition(),
									gs[j].getPosition()+gs[j].getLength()), 
									"http://en.wikipedia.org/wiki/"+api.getTitlebyId(gs[j].getConcept()).replaceAll(" ", "_"));}
				else
					System.out.printf("\t-\t-");
			}
			System.out.println();
		}*/
		api.flush();
	}

}
