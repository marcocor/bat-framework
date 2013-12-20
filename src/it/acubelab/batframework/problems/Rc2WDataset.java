/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.Tag;

import java.util.List;

public interface Rc2WDataset extends C2WDataset{
	
	/**Note: the order of the elements in this list must be the same of those returned by getAnnotationsList().
	 * @return a list of lists containing ordered tags for the corresponding document (first tag is more important).
	 */
	public List<List<Tag>> getRc2WGoldStandardList();

}
