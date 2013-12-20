/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.examples;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WDataset;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.ProblemReduction;

import java.util.*;


public class DummyDataset implements A2WDataset{
	List<String> texts;
	List<Set<Annotation>> annotations;
	
	public DummyDataset() throws AnnotationException{
		texts = new Vector<String>();
		annotations = new Vector<Set<Annotation>>();
		Set<Annotation> ann1 = new HashSet<Annotation>();
		Set<Annotation> ann2 = new HashSet<Annotation>();
		String text1 = "Cats can hear sounds too faint or too high in frequency for human ears, such as those made by mice and other small game.";
		String text2 = "Serenity is a 2005 space western film written and directed by Joss Whedon.";

		ann1.add(new Annotation(text1.indexOf("Cats"), "Cats".length(), 6678));
		ann1.add(new Annotation(text1.indexOf("sounds"), "sounds".length(), 18994087));
		ann1.add(new Annotation(text1.indexOf("frequency"), "frequency".length(), 10779));
		ann1.add(new Annotation(text1.indexOf("human"), "human".length(), 682482));
		ann1.add(new Annotation(text1.indexOf("ears"), "ears".length(), 768413));
		ann1.add(new Annotation(text1.indexOf("mice"), "mice".length(), 18845));
		ann1.add(new Annotation(text1.indexOf("game"), "game".length(), 771717));
		
		ann2.add(new Annotation(text2.indexOf("Serenity"), "Serenity".length(), 504242));
		ann2.add(new Annotation(text2.indexOf("2005"), "2005".length(), 35984));
		ann2.add(new Annotation(text2.indexOf("space western"), "space western".length(), 3766056));
		ann2.add(new Annotation(text2.indexOf("film"), "film".length(), 21555729));
		ann2.add(new Annotation(text2.indexOf("Joss Whedon"), "Joss Whedon".length(), 158079));
		
		texts.add(text1);
		texts.add(text2);
		annotations.add(ann1);
		annotations.add(ann2);
	}

	@Override
	public int getSize() {
		return texts.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (Set<Annotation> a : annotations)
			count += a.size();
		return count;
	}

	@Override
	public List<Set<Annotation>> getA2WGoldStandardList() {
		return annotations;
	}

	@Override
	public List<String> getTextInstanceList() {
		return texts;
	}

	@Override
	public List<Set<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public List<Set<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(this.getA2WGoldStandardList());
	}

	@Override
	public List<Set<Annotation>> getD2WGoldStandardList() {
		return getA2WGoldStandardList();
	}

	@Override
	public String getName() {
		return "Dummy";
	}

}
