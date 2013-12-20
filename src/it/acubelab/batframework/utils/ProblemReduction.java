/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import it.acubelab.batframework.data.*;

import java.util.*;

public class ProblemReduction {
	
	/** Adapt a solution for an A2W problem instance to C2W, discarding the mentions and keeping the set
	 * of concepts.
	 * @param anns a A2W solution.
	 * @return the adapted solution for the C2W problem.
	 */
	public static Set<Tag> A2WToC2W (Set<Annotation> anns){
		Set<Tag> tags = new HashSet<Tag>();
		for (Annotation a : anns)
			tags.add(new Tag(a.getConcept()));
		return tags;
	}

	

	/** Adapt a list of solutions for an A2W problem instance to C2W, discarding the mentions and keeping the set
	 * of concepts.
	 * @param tags a A2W solution.
	 * @return the list of adapted solutions for the C2W problem.
	 */
	public static List<Set<Tag>> A2WToC2WList(List<Set<Annotation>> tagsList) {
		List<Set<Tag>> anns = new Vector<Set<Tag>>();
		for(Set<Annotation> s : tagsList)
			anns.add(A2WToC2W(s));
		return anns;
	}

	/** Adapt a solution for a Sa2W problem instance to a A2W one, keeping all annotations independently from their score.
	 * @param scoredAnns the solution of the Sa2W problem.
	 * @param threshold all annotations scored under this threshold will be discarded.
	 * @return the adapted solution of the A2W problem.
	 */
	public static Set<Annotation> Sa2WToA2W(Set<ScoredAnnotation> scoredAnns) throws AnnotationException {
		return Sa2WToA2W(scoredAnns, Float.MIN_VALUE);
	}

	/** Adapt a solution for a Sa2W problem instance to a A2W one, keeping only annotations with a score higher than a given threshold.
	 * @param scoredAnns the solution of the Sa2W problem.
	 * @param threshold all annotations scored under this threshold will be discarded.
	 * @return the adapted solution for the A2W problem.
	 */
	public static Set<Annotation> Sa2WToA2W (Set<ScoredAnnotation> scoredAnns, float threshold) {
		Set<Annotation> tags = new HashSet<Annotation>();
		for (ScoredAnnotation t : scoredAnns)
			if (t.getScore() >= threshold)
			tags.add(new Annotation(t.getPosition(),t.getLength(),t.getConcept()));
		return tags;
	}

	/** Adapt a list of solutions for a Sa2W problem instance to A2W, keeping only annotations with a score higher than a given threshold.
	 * @param scoredAnnsList the list of solutions of the Sa2W problem.
	 * @param threshold all annotations scored under this threshold will be discarded.
	 * @return the list of adapted solutions for the A2W problem.
	 */
	public static List<Set<Annotation>> Sa2WToA2WList(List<Set<ScoredAnnotation>> scoredAnnsList, float threshold) {
		List<Set<Annotation>> tags = new Vector<Set<Annotation>>();
		for(Set<ScoredAnnotation> s : scoredAnnsList)
			tags.add(Sa2WToA2W(s,threshold));
		return tags;
	}

	/**Adapt a list of solutions for a Sa2W problem instance to Sc2W, discarding the mentions.
	 * @param scoredAnnotations the solution of the Sa2W problem.
	 * @return the adapted solution for the Sc2W problem.
	 */
	public static Set<ScoredTag> Sa2WToSc2W (Set<ScoredAnnotation> scoredAnnotations) {
		Set<ScoredTag> tags = new HashSet<ScoredTag>();
		for (ScoredAnnotation t : scoredAnnotations)
			tags.add(new ScoredTag(t.getConcept(), t.getScore()));
		return tags;
	}

	/** Adapt a list of solutions for the Sa2W problem instance to Sc2W, discarding the mentions.
	 * @param scoredAnnList the list of solutions of the Sa2W problem.
	 * @return  the list of adapted solutions for the Sc2W problem.
	 */
	public static List<Set<ScoredTag>> Sa2WToSc2WList(List<Set<ScoredAnnotation>> scoredAnnList) {
		List<Set<ScoredTag>> tags = new Vector<Set<ScoredTag>>();
		for(Set<ScoredAnnotation> s : scoredAnnList)
			tags.add(Sa2WToSc2W(s));
		return tags;
	}

	/** Adapt a solution for a Sc2W problem instance to C2W, keeping all concepts independently from their score.
	 * @param scoredTags the solution of the Sc2W problem.
	 * @return the adapted solution for the C2W problem.
	 */
	public static Set<Tag> Sc2WToC2W(Set<ScoredTag> scoredTags) {
		return Sc2WToC2W(scoredTags, Float.MIN_VALUE);
	}
	
	/** Adapt a solution for a Sc2W problem instance to C2W, keeping only concepts with a score higher than a given threshold.
	 * @param scoredTags the solution of the Sc2W problem.
	 * @param threshold all concepts scored under this threshold will be discarded.
	 * @return the adapted solution for the C2W problem.
	 */
	public static Set<Tag> Sc2WToC2W (Set<ScoredTag> scoredTags, float threshold) {
		Set<Tag> annotations = new HashSet<Tag>();
		for (ScoredTag t : scoredTags)
			if (t.getScore() >= threshold)
			annotations.add(new Tag(t.getConcept()));
		return annotations;
	}

	/** Adapt a list of solutions for the Sc2W problem to C2W, keeping only concepts with a score higher than a given threshold.
	 * @param scoredTagsList list of solutions of the Sc2W problem.
	 * @param threshold all concepts scored under this threshold will be discarded.
	 * @return the list of adapted solution for the C2W problem.
	 */
	public static List<Set<Tag>> Sc2WToC2WList(List<Set<ScoredTag>> scoredTagsList, float threshold) {
		List<Set<Tag>> tags = new Vector<Set<Tag>>();
		for(Set<ScoredTag> s : scoredTagsList)
			tags.add(Sc2WToC2W(s,threshold));
		return tags;
	}



	public static Set<Annotation> Sa2WToD2W(Set<ScoredAnnotation> set, Set<Mention> mentions, float theshold) {
		Set<Annotation> res = new HashSet<Annotation>();
		
		//take only annotations that overlap with a mention
		for (Mention m: mentions){
			ScoredAnnotation bestCand = null;
			for (ScoredAnnotation a: set){
				if (a.getScore() < theshold) continue;
				if (a.overlaps(m))
					if (bestCand == null || bestCand.getScore() < a.getScore())
						bestCand = a;
			}
			if (bestCand != null)
				res.add(new Annotation(m.getPosition(), m.getLength(), bestCand.getConcept()));
			
		}
		return res;
	}



	public static List<Set<Annotation>> Sa2WToD2WList(List<Set<ScoredAnnotation>> scoredAnnotationList, List<Set<Mention>> mentions, float threshold) {
		List<Set<Annotation>> res = new Vector<Set<Annotation>>();
		for(int i=0; i<scoredAnnotationList.size(); i++)
			res.add(Sa2WToD2W(scoredAnnotationList.get(i), mentions.get(i), threshold));
		return res;
	}



	public static List<Set<Mention>> A2WToD2WMentionsInstance(List<Set<Annotation>> annotations) {
		List<Set<Mention>> result = new Vector<Set<Mention>>();
		for (Set<Annotation> as: annotations){
			Set<Mention> resSet = new HashSet<Mention>();
			result.add(resSet);
			for (Annotation a : as)
				resSet.add(new Mention(a.getPosition(), a.getLength()));
		}
		return result;
	}
	

}
