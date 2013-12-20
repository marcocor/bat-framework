/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.examples;


import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.metrics.StrongTagMatch;
import it.acubelab.batframework.utils.WikipediaApiInterface;

public class CloseTagMatch extends StrongTagMatch{

	public CloseTagMatch(WikipediaApiInterface api) {
		super(api);
	}

	@Override
	public boolean match(Tag t1, Tag t2) {
		return closeness(t1, t2) > 0.5;
	}

	private float closeness(Tag t1, Tag t2){
		return 0;
	}
}
