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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class ConceptAnnotationMatch implements MatchRelation<Annotation>{
	private WikipediaApiInterface api = null;

	public ConceptAnnotationMatch(WikipediaApiInterface api) {
		this.api = api;
	}

	@Override
	public boolean match(Annotation t1, Annotation t2) {
		try {
			return api.dereference(t1.getConcept()) == api.dereference(t2.getConcept());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
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

		List<Set<Annotation>> noDoubleConcepts = new Vector<Set<Annotation>>();
		for (Set<Annotation> s : computedOutput){
			HashSet<Integer> alreadyInsertedConcepts = new HashSet<Integer>();
			Set<Annotation> noDoubleSet = new HashSet<Annotation>();
			noDoubleConcepts.add(noDoubleSet);
			for (Annotation a: s)
				try {
					if (!alreadyInsertedConcepts.contains(api.dereference(a.getConcept()))){
						noDoubleSet.add(new Annotation(a.getPosition(), a.getLength(), a.getConcept()));
						alreadyInsertedConcepts.add(api.dereference(a.getConcept()));
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
					
				}
			}

		return noDoubleConcepts;
	}

	@Override
	public List<Set<Annotation>> preProcessGoldStandard(List<Set<Annotation>> goldStandard) {
		return preProcessOutput(goldStandard);
	}


	@Override
	public String getName() {
		return "Concept annotation match";
	}

}
