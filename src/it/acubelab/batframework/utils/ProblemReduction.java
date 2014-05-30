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

	/**
	 * Adapt a solution for an A2W problem instance to C2W, discarding the
	 * mentions and keeping the set of concepts.
	 * 
	 * @param anns
	 *            a A2W solution.
	 * @return the adapted solution for the C2W problem.
	 */
	public static HashSet<Tag> A2WToC2W(HashSet<Annotation> anns) {
		HashSet<Tag> tags = new HashSet<Tag>();
		for (Annotation a : anns)
			tags.add(new Tag(a.getConcept()));
		return tags;
	}

	/**
	 * Adapt a list of solutions for an A2W problem instance to C2W, discarding
	 * the mentions and keeping the set of concepts.
	 * 
	 * @param tags
	 *            a A2W solution.
	 * @return the list of adapted solutions for the C2W problem.
	 */
	public static List<HashSet<Tag>> A2WToC2WList(
			List<HashSet<Annotation>> tagsList) {
		List<HashSet<Tag>> anns = new Vector<HashSet<Tag>>();
		for (HashSet<Annotation> s : tagsList)
			anns.add(A2WToC2W(s));
		return anns;
	}

	/**
	 * Adapt a solution for a Sa2W problem instance to a A2W one, keeping all
	 * annotations independently from their score.
	 * 
	 * @param scoredAnns
	 *            the solution of the Sa2W problem.
	 * @param threshold
	 *            all annotations scored under this threshold will be discarded.
	 * @return the adapted solution of the A2W problem.
	 */
	public static HashSet<Annotation> Sa2WToA2W(
			HashSet<ScoredAnnotation> scoredAnns) throws AnnotationException {
		return Sa2WToA2W(scoredAnns, Float.MIN_VALUE);
	}

	/**
	 * Adapt a solution for a Sa2W problem instance to a A2W one, keeping only
	 * annotations with a score higher than a given threshold.
	 * 
	 * @param scoredAnns
	 *            the solution of the Sa2W problem.
	 * @param threshold
	 *            all annotations scored under this threshold will be discarded.
	 * @return the adapted solution for the A2W problem.
	 */
	public static HashSet<Annotation> Sa2WToA2W(
			HashSet<ScoredAnnotation> scoredAnns, float threshold) {
		HashSet<Annotation> tags = new HashSet<Annotation>();
		for (ScoredAnnotation t : scoredAnns)
			if (t.getScore() >= threshold)
				tags.add(new Annotation(t.getPosition(), t.getLength(), t
						.getConcept()));
		return tags;
	}

	/**
	 * Adapt a list of solutions for a Sa2W problem instance to A2W, keeping
	 * only annotations with a score higher than a given threshold.
	 * 
	 * @param scoredAnnsList
	 *            the list of solutions of the Sa2W problem.
	 * @param threshold
	 *            all annotations scored under this threshold will be discarded.
	 * @return the list of adapted solutions for the A2W problem.
	 */
	public static List<HashSet<Annotation>> Sa2WToA2WList(
			List<HashSet<ScoredAnnotation>> scoredAnnsList, float threshold) {
		List<HashSet<Annotation>> tags = new Vector<HashSet<Annotation>>();
		for (HashSet<ScoredAnnotation> s : scoredAnnsList)
			tags.add(Sa2WToA2W(s, threshold));
		return tags;
	}

	/**
	 * Adapt a list of solutions for a Sa2W problem instance to Sc2W, discarding
	 * the mentions.
	 * 
	 * @param scoredAnnotations
	 *            the solution of the Sa2W problem.
	 * @return the adapted solution for the Sc2W problem.
	 */
	public static HashSet<ScoredTag> Sa2WToSc2W(
			HashSet<ScoredAnnotation> scoredAnnotations) {
		HashSet<ScoredTag> tags = new HashSet<ScoredTag>();
		for (ScoredAnnotation t : scoredAnnotations)
			tags.add(new ScoredTag(t.getConcept(), t.getScore()));
		return tags;
	}

	/**
	 * Adapt a list of solutions for the Sa2W problem instance to Sc2W,
	 * discarding the mentions.
	 * 
	 * @param scoredAnnList
	 *            the list of solutions of the Sa2W problem.
	 * @return the list of adapted solutions for the Sc2W problem.
	 */
	public static List<HashSet<ScoredTag>> Sa2WToSc2WList(
			List<HashSet<ScoredAnnotation>> scoredAnnList) {
		List<HashSet<ScoredTag>> tags = new Vector<HashSet<ScoredTag>>();
		for (HashSet<ScoredAnnotation> s : scoredAnnList)
			tags.add(Sa2WToSc2W(s));
		return tags;
	}

	/**
	 * Adapt a solution for a Sc2W problem instance to C2W, keeping all concepts
	 * independently from their score.
	 * 
	 * @param scoredTags
	 *            the solution of the Sc2W problem.
	 * @return the adapted solution for the C2W problem.
	 */
	public static HashSet<Tag> Sc2WToC2W(HashSet<ScoredTag> scoredTags) {
		return Sc2WToC2W(scoredTags, Float.MIN_VALUE);
	}

	/**
	 * Adapt a solution for a Sc2W problem instance to C2W, keeping only
	 * concepts with a score higher than a given threshold.
	 * 
	 * @param scoredTags
	 *            the solution of the Sc2W problem.
	 * @param threshold
	 *            all concepts scored under this threshold will be discarded.
	 * @return the adapted solution for the C2W problem.
	 */
	public static HashSet<Tag> Sc2WToC2W(HashSet<ScoredTag> scoredTags,
			float threshold) {
		HashSet<Tag> annotations = new HashSet<Tag>();
		for (ScoredTag t : scoredTags)
			if (t.getScore() >= threshold)
				annotations.add(new Tag(t.getConcept()));
		return annotations;
	}

	/**
	 * Adapt a list of solutions for the Sc2W problem to C2W, keeping only
	 * concepts with a score higher than a given threshold.
	 * 
	 * @param scoredTagsList
	 *            list of solutions of the Sc2W problem.
	 * @param threshold
	 *            all concepts scored under this threshold will be discarded.
	 * @return the list of adapted solution for the C2W problem.
	 */
	public static List<HashSet<Tag>> Sc2WToC2WList(
			List<HashSet<ScoredTag>> scoredTagsList, float threshold) {
		List<HashSet<Tag>> tags = new Vector<HashSet<Tag>>();
		for (HashSet<ScoredTag> s : scoredTagsList)
			tags.add(Sc2WToC2W(s, threshold));
		return tags;
	}

	public static HashSet<Annotation> Sa2WToD2W(HashSet<ScoredAnnotation> set,
			HashSet<Mention> mentions, float theshold) {
		HashSet<Annotation> res = new HashSet<Annotation>();

		// take only annotations that overlap with a mention
		for (Mention m : mentions) {
			ScoredAnnotation bestCand = null;
			for (ScoredAnnotation a : set) {
				if (a.getScore() < theshold)
					continue;
				if (a.overlaps(m))
					if (bestCand == null || bestCand.getScore() < a.getScore())
						bestCand = a;
			}
			if (bestCand != null)
				res.add(new Annotation(m.getPosition(), m.getLength(), bestCand
						.getConcept()));

		}
		return res;
	}

	public static List<HashSet<Annotation>> Sa2WToD2WList(
			List<HashSet<ScoredAnnotation>> scoredAnnotationList,
			List<HashSet<Mention>> mentions, float threshold) {
		List<HashSet<Annotation>> res = new Vector<HashSet<Annotation>>();
		for (int i = 0; i < scoredAnnotationList.size(); i++)
			res.add(Sa2WToD2W(scoredAnnotationList.get(i), mentions.get(i),
					threshold));
		return res;
	}

	public static List<HashSet<Mention>> A2WToD2WMentionsInstance(
			List<HashSet<Annotation>> annotations) {
		List<HashSet<Mention>> result = new Vector<HashSet<Mention>>();
		for (HashSet<Annotation> as : annotations) {
			HashSet<Mention> resSet = new HashSet<Mention>();
			result.add(resSet);
			for (Annotation a : as)
				resSet.add(new Mention(a.getPosition(), a.getLength()));
		}
		return result;
	}

}
