/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.examples;

import it.unipi.di.acube.batframework.data.*;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.Pair;
import it.unipi.di.acube.batframework.utils.ProblemReduction;

import java.util.Calendar;
import java.util.HashSet;

public class LolAbstractAnnotator implements Ab2WSystem{
	private long lastAnnotation = -1;

	@Override
	public Pair<HashSet<ScoredAnnotation>,HashSet<ScoredTag>> getAb2WOutput(String text) {
		lastAnnotation = Calendar.getInstance().getTimeInMillis();
		Pair<HashSet<ScoredAnnotation>,HashSet<ScoredTag>> res =  computeMentionedAnnotations(text);
		lastAnnotation = Calendar.getInstance().getTimeInMillis()-lastAnnotation;
		return res;
	}

	private Pair<HashSet<ScoredAnnotation>,HashSet<ScoredTag>> computeMentionedAnnotations(String text) {
		return null;
	}

	@Override
	public HashSet<ScoredAnnotation> solveSa2W(String text)
			throws AnnotationException {
		return Ab2WProblemsReduction.Ab2WToSa2W(getAb2WOutput(text));
	}

	@Override
	public HashSet<Annotation> solveA2W(String text) throws AnnotationException {
		return Ab2WProblemsReduction.Sa2WToA2W(solveSa2W(text));
	}

	@Override
	public HashSet<Tag> solveC2W(String text) throws AnnotationException {
		return Ab2WProblemsReduction.A2WToC2W(solveA2W(text));
	}

	@Override
	public String getName() {
		return "Lol Abstract Annotator";
	}

	@Override
	public long getLastAnnotationTime() {
		return lastAnnotation;
	}

	@Override
	public HashSet<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return Ab2WProblemsReduction.Sa2WToSc2W(solveSa2W(text));
	}

	@Override
	public HashSet<Annotation> solveD2W(String text, HashSet<Mention> mentions){
		return ProblemReduction.Sa2WToD2W(this.solveSa2W(text), mentions, Float.MIN_VALUE);
	}
	
}
