/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.metrics;

import it.acubelab.batframework.data.MultipleAnnotation;
import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.IOException;
import java.util.*;

public class MultiEntityMatch implements MatchRelation<MultipleAnnotation> {
	private WikipediaApiInterface api;

	public MultiEntityMatch(WikipediaApiInterface api) {
		this.api = api;
	}

	@Override
	public boolean match(MultipleAnnotation t1, MultipleAnnotation t2) {
		if (t1.getPosition() != t2.getPosition()
				|| t1.getLength() != t2.getLength())
			return false;
		for (int c1 : t1.getCandidates())
			for (int c2 : t2.getCandidates())
				try {
					if (api.dereference(c1) == api.dereference(c2))
						return true;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		return false;
	}

	@Override
	public List<Set<MultipleAnnotation>> preProcessOutput(
			List<Set<MultipleAnnotation>> computedOutput) {
		/** Prefetch redirect values */
		List<Integer> widsToCheck = new Vector<Integer>();
		for (Set<MultipleAnnotation> s : computedOutput)
			for (MultipleAnnotation a : s)
				for (int candidate : a.getCandidates())
					widsToCheck.add(candidate);

		try {
			api.prefetchWids(widsToCheck);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return computedOutput;
	}

	@Override
	public List<Set<MultipleAnnotation>> preProcessGoldStandard(
			List<Set<MultipleAnnotation>> goldStandard) {
		return preProcessOutput(goldStandard);
	}

	@Override
	public String getName() {
		return "Multi-Entity match";
	}
}
