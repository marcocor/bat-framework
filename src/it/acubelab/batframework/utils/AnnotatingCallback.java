/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

/**
 * This interface represent a Callback function that can be used during the annotation of a dataset.
 * Since the annotation process for many documents may take long, a Callback function, that will regularly be
 * called during the annotation process, can be passed as argument.
 */
public interface AnnotatingCallback {
	/**
	 * The method run by this Callback function.
	 * @param msec min milliseconds between two calls of this method.
	 * @param doneDocs documents that have been annotated.
	 * @param totalDocs total number of docs to annotate.
	 * @param foundTags total number of annotations/tags found so far.
	 */
	public void run(long msec, int doneDocs, int totalDocs, int foundTags);
}
