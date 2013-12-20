/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import mpi.aida.config.settings.DisambiguationSettings;
import mpi.aida.config.settings.disambiguation.CocktailPartyDisambiguationSettings;
import mpi.aida.graph.similarity.exception.MissingSettingException;

public class AIDACockailPartyAnnotator extends AIDAAnnotator {

	public AIDACockailPartyAnnotator(String configFile, WikipediaApiInterface api)
			throws XPathExpressionException, FileNotFoundException,
			ParserConfigurationException, SAXException, IOException,
			AnnotationException {
		super(configFile, api);
	}

	@Override
	public DisambiguationSettings setDisambiguationSettings() throws MissingSettingException {
		return new CocktailPartyDisambiguationSettings();
	}

	@Override
	public String getName() {
		return "AIDA-CocktailParty";
	}

}
