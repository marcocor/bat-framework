/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.datasetPlugins;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WDataset;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.ProblemReduction;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;


public class TimerunsDataset implements A2WDataset{
	private List<String> textList = new Vector<String>();
	private List<Set<Annotation>> annList =  new Vector<Set<Annotation>>();

	public TimerunsDataset(String textPath, int step, int count) throws IOException, ParserConfigurationException, SAXException, AnnotationException, XPathExpressionException{
		int currIdx = 0;
		String fullBody= loadBody(textPath);
		for (int i=0; i<count; i++){
			//advance of <step> more words
			for (int j=0; j<step; j++){
				while (currIdx < fullBody.length() && (fullBody.charAt(currIdx) != ' ' && fullBody.charAt(currIdx) != '\n'))
					currIdx++;
				while (currIdx < fullBody.length() && (fullBody.charAt(currIdx) == ' ' || fullBody.charAt(currIdx) == '\n'))
					currIdx++;
				if (currIdx == fullBody.length())
					throw new AnnotationException("Cannot make "+count+" documents of "+step+ " words each (lower one of these values).");
			}
			textList.add(fullBody.substring(0, currIdx));
			annList.add(new HashSet<Annotation>());
		}
		
		System.out.println("Biggest document will be "+currIdx+" chars long.");
	}

	public String loadBody(String textPath) throws IOException{
		File tf = new File(textPath);			

		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(tf), Charset.forName("UTF-8")));
		String line;
		String body="";
		while ((line = r.readLine())!=null)
			body += line.replace((char)0, ' ')+"\n";
		r.close();
		return body;
	}

	@Override
	public int getSize() {
		return this.textList.size();
	}

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
		return this.textList;
	}

	@Override
	public List<Set<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return "TimeRuns";
	}

	@Override
	public List<Set<Annotation>> getA2WGoldStandardList() {
		return annList;
	}

}
