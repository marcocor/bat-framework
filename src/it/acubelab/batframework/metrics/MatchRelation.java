/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.metrics;

import java.util.List;
import java.util.Set;

public interface MatchRelation <E>{
	
	public boolean match(E t1, E t2);

	public List<Set<E>> preProcessOutput(List<Set<E>> computedOutput);

	public List<Set<E>> preProcessGoldStandard(List<Set<E>> goldStandard);

	public String getName();

}
