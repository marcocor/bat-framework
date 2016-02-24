/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.datasetPlugins;

import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class AQUAINTDataset extends MSNBCDataset {
	public AQUAINTDataset(String textPath, String annotationsPath, WikipediaApiInterface api) throws IOException,
	        ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException {
		this(getFilesAndInputStreams(textPath, ".+\\.htm"), getFilesAndInputStreams(annotationsPath, ".+\\.htm"), api);
	}

	public AQUAINTDataset(Map<String, InputStream> bodyFilenameToInputstream,
	        Map<String, InputStream> anchorsFilenameToInputstream, WikipediaApiInterface api) throws IOException,
	        AnnotationException, XPathExpressionException, ParserConfigurationException, SAXException {
		// load the bodies
		HashMap<String, String> filenameToBody = loadBody(bodyFilenameToInputstream);

		// load the annotations
		HashMap<String, HashSet<Annotation>> filenameToAnnotations = loadTags(anchorsFilenameToInputstream, api);

		// check that files are coherent.
		checkConsistency(filenameToBody, filenameToAnnotations);

		// unify the two mappings and generate the lists.
		unifyMaps(filenameToBody, filenameToAnnotations);
	}

	@Override
	public String getName() {
		return "AQUAINT";
	}

}
