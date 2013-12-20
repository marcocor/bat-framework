/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.data;

public class ScoredTag extends Tag implements Cloneable{
	private static final long serialVersionUID = 1L;
	private float score;

	public ScoredTag(int wikipediaArticle, float score) {
		super(wikipediaArticle);
		this.score = score;
	}
	
	public float getScore(){
		return score;
	}
	

	@Override public Object clone(){
		return new ScoredTag(getConcept(), score);
	}

}
