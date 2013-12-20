/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.datasetPlugins;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;


public class ACE2004Dataset extends MSNBCDataset {
	public ACE2004Dataset(String textPath, String annotationsPath, WikipediaApiInterface api) throws IOException, ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException{
		//load the bodies
		HashMap<String, String> filenameToBody= loadBody(textPath, "^[^\\.]+.*");

		//load the annotations
		HashMap<String, Set<Annotation>> filenameToAnnotations= loadTags(annotationsPath, "^[^\\.]+.*", api);

		//check that files are coherent.
		checkConsistency(filenameToBody, filenameToAnnotations);

		//unify the two mappings and generate the lists.
		unifyMaps(filenameToBody, filenameToAnnotations);
	}

	@Override
	public String getName(){
		return "ACE2004";
	}

}
