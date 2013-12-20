/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.problems;

import it.acubelab.batframework.data.ScoredAnnotation;
import it.acubelab.batframework.utils.AnnotationException;

import java.util.Set;

/**
 * A system that tags parts of a natural language text to Wikipedia concepts.
 *
 */
public interface Sa2WSystem extends A2WSystem, Sc2WSystem{
	
	/**
	 * @param text a text to tag.
	 * @return a set containing the tags found for the given text, with a score associated to it representing
	 * the relevance of the topic to the text.
	 * @throws AnnotationException 
	 */
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException;

}
