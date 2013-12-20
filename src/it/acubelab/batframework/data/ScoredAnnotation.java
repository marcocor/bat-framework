/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.data;

import it.acubelab.batframework.utils.AnnotationException;

public class ScoredAnnotation extends Annotation implements Cloneable{
	private static final long serialVersionUID = 1L;
	private float score;

	public ScoredAnnotation(int position, int length, int wikipediaArticle, float score) throws AnnotationException {
		super(position, length, wikipediaArticle);
		this.score = score;
	}
	
	public float getScore(){
		return score;
	}
	

	@Override public Object clone(){
		ScoredAnnotation cloned;
		try {
			cloned = new ScoredAnnotation(this.getPosition(), this.getLength(), this.getConcept(), this.score);
		} catch (AnnotationException e) {
			e.printStackTrace();
			return null;
		}
		return cloned;
	}

}
