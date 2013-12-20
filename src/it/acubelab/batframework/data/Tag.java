/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.data;

import java.io.Serializable;

/**
 * An annotation is a concept associated to a text.
 *
 */
public class Tag implements Comparable<Tag>, Serializable, Cloneable{
	private static final long serialVersionUID = 1L;
	private int concept; //the wikipedia article to which the annotation is bound

	public int getConcept(){
		return this.concept;
	}

	public void setWikipediaArticle(int wid){
		this.concept = wid;
	}

	public Tag(int wikipediaArticle){
		this.concept = wikipediaArticle;
	}


	@Override public boolean equals(Object t){
		Tag tag = (Tag) t;
		return this.concept == tag.concept;
	}

	@Override public int hashCode() {
		return this.concept;
	}

	@Override public Object clone(){
		Tag cloned = new Tag(this.concept);
		return cloned;
	}

	@Override
	public int compareTo(Tag arg0) {
		return this.concept - arg0.concept;
	}
}
