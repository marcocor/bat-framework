/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.metrics;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.*;

public class StrongAnnotationMatch implements MatchRelation<Annotation>{
	private WikipediaApiInterface api;
	
	public StrongAnnotationMatch(WikipediaApiInterface api){
		this.api = api;
	}
	
	@Override
	public boolean match(Annotation a1, Annotation a2) {
		try {
			return a1.getLength() == a2.getLength() &&
					a1.getPosition() == a2.getPosition() &&
					api.dereference(a1.getConcept()) == api.dereference(a2.getConcept());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Set<Annotation>> preProcessOutput(List<Set<Annotation>> computedOutput) {
		try {
			Annotation.prefetchRedirectList(computedOutput, api);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return computedOutput;
	}

	@Override
	public List<Set<Annotation>> preProcessGoldStandard(List<Set<Annotation>> goldStandard) {
		return preProcessOutput(goldStandard);
	}

	@Override
	public String getName() {
		return "Strong annotation match";
	}

}
