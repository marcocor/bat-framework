/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.datasetPlugins;

import java.util.*;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WDataset;
import it.acubelab.batframework.utils.ProblemReduction;

public class EmptyDataset implements A2WDataset {

	@Override
	public int getTagsCount() {
		return 0;
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
	public List<String> getTextInstanceList() {
		List<String> texts = new Vector<String>();
		for (int i=0; i<500; i++){
			String s = "a";
			for (int j=0; j<i; j++)
				s+=" ";
			texts.add(s);
		}
		return texts;
	}

	@Override
	public List<Set<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public int getSize() {
		return 500;
	}

	@Override
	public String getName() {
		return "Dummy empty Dataset";
	}

	@Override
	public List<Set<Annotation>> getA2WGoldStandardList() {
		List<Set<Annotation>> l = new Vector<Set<Annotation>>();
		for (int i=0; i<500; i++)
			l.add(new HashSet<Annotation>());
		return l;
	}

}
