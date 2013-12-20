/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WDataset;

import java.util.List;
import java.util.Set;

/**
 * This class provides methods to test an annotator.
 */
public class TestAnnotator {
	/**
	 * Check that the output of an annotator is correctly formed.
	 * @param ds a dataset.
	 * @param computedAnnotations the output of the annotator for dataset {@code ds}.
	 * @throws AnnotationException if the output is malformed.
	 */
	public static <T extends Tag> void checkOutput(A2WDataset ds, List<Set<T>> computedAnnotations) throws AnnotationException {
		if (ds.getTextInstanceList().size() != computedAnnotations.size())
			throw new AnnotationException("Size of dataset ("+ds.getTextInstanceList().size()+" instances) and size of output ("+computedAnnotations.size()+" solutions) differ.");
	}
}
