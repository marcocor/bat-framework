/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.metrics;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.*;

public class StrongTagMatch implements MatchRelation<Tag>{
	private WikipediaApiInterface api;
	public StrongTagMatch(WikipediaApiInterface api){
		this.api = api;
	}

	@Override
	public boolean match(Tag t1, Tag t2) {
		try {
			return api.dereference(t1.getConcept()) == api.dereference(t2.getConcept());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Set<Tag>> preProcessOutput(List<Set<Tag>> computedOutput) {
		try {
			Annotation.prefetchRedirectList(computedOutput, api);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		List<Set<Tag>> resolvedConcepts = new Vector<Set<Tag>>();
		for (Set<Tag> s : computedOutput){
			Set<Tag> newRes = new HashSet<Tag>();
			resolvedConcepts.add(newRes);
			for (Tag t: s)
				try {
					newRes.add(new Tag(api.dereference(t.getConcept())));
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
		}
		return resolvedConcepts;
	}

	@Override
	public List<Set<Tag>> preProcessGoldStandard(List<Set<Tag>> goldStandard) {
		return preProcessOutput(goldStandard);
	}

	@Override
	public String getName() {
		return "Strong tag match";
	}

}
