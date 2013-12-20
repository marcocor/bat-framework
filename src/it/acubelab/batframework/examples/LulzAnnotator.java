/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.examples;

import java.util.Calendar;
import java.util.Set;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WSystem;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.ProblemReduction;

public class LulzAnnotator implements A2WSystem{
	private long lastTime = -1;

	@Override
	public Set<Annotation> solveA2W(String text) throws AnnotationException {
		lastTime = Calendar.getInstance().getTimeInMillis();
		Set<Annotation> result = this.retrieveResult(text);
		lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;
		return result;
	}

	@Override
	public Set<Tag> solveC2W(String text) throws AnnotationException {
		Set<Annotation> tags = solveA2W(text);
		return ProblemReduction.A2WToC2W(tags);
	}

	@Override
	public Set<Annotation> solveD2W(String text, Set<Mention> mentions){
		return null;
	}

	@Override
	public String getName() {
		return "<Name of the system>";
	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

	private Set<Annotation> retrieveResult(String text) {
		return null;
	}
}
