/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.metrics;

import it.acubelab.batframework.data.Mention;

import java.util.*;

public class MentionMatch implements MatchRelation<Mention>{
	@Override
	public boolean match(Mention t1, Mention t2) {
			return t1.equals(t2);
	}

	@Override
	public List<Set<Mention>> preProcessOutput(List<Set<Mention>> computedOutput) {
		return computedOutput;
	}

	@Override
	public List<Set<Mention>> preProcessGoldStandard(List<Set<Mention>> goldStandard) {
		return goldStandard;
	}

	@Override
	public String getName() {
		return "Mention match";
	}
}
