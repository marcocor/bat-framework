/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.datasetPlugins;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import it.acubelab.batframework.data.Annotation;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.A2WDataset;
import it.acubelab.batframework.utils.AnnotationException;
import it.acubelab.batframework.utils.ProblemReduction;
import it.acubelab.batframework.utils.WikipediaApiInterface;
import it.unimi.dsi.lang.MutableString;

public class KddDataset implements A2WDataset{
	private List<Set<Annotation>> tags = new Vector<Set<Annotation>>();
	private List<MutableString> documents = new Vector<MutableString>();
	private Pattern nonePattern = Pattern.compile("^([^\t]*)\t([^\t]*)\tO\tB-.*NONE\tNONE$");
	private Pattern nonePattern2 = Pattern.compile("^([^\t]*)\tO\tO\tO\t.*NONE\tNONE$");
	private Pattern tagPattern = Pattern.compile("^([^\t]*)\t([^\t]*)\tO\tB-([^\t]*)\t([^\t]*)\t([^\t]*)(?:\t([^\t]*)\t([^\t]*))?$");
	private Pattern nonTagPattern = Pattern.compile("^([^\t]*)\t([^\t]*)\tO\tB-([^\t]*)");
	private Pattern skipPattern = Pattern.compile("^([^\t]*)\tO\tO\tI-([^\t]*)$");
	private Pattern endPattern = Pattern.compile("^\\.\tO\tO\t#$");
	private Pattern nonTagPattern2 = Pattern.compile("^([^\t]*)\tO\tO\tO$");


	public KddDataset (String[] files, WikipediaApiInterface api) throws IOException, AnnotationException, XPathExpressionException, ParserConfigurationException, SAXException{
		List<Set<KddAnnotation>> kddAnns = new Vector<Set<KddAnnotation>>();
		List<String> titlesToPrefetch = new Vector<String>();
		for (String file: files){
			BufferedReader r = new BufferedReader(new FileReader(file));
			String line;
			MutableString currentDoc = new MutableString();
			Set<KddAnnotation> currentAnns = new HashSet<KddAnnotation>();
			int currentPos = 0;
			while ((line = r.readLine()) != null){
				Matcher noneMatch = nonePattern.matcher(line);
				Matcher none2Match = nonePattern2.matcher(line);
				Matcher tagMatch = tagPattern.matcher(line);
				Matcher nonAnnMatch = nonTagPattern.matcher(line);
				Matcher skipMatch = skipPattern.matcher(line);
				Matcher endMatch = endPattern.matcher(line);
				Matcher nonAnn2Match = nonTagPattern2.matcher(line);

				if (endMatch.matches()){ // a new document
					if (currentDoc.length() >0){
						documents.add(currentDoc.trimRight());
						kddAnns.add(currentAnns);
						currentDoc = new MutableString();
						currentAnns = new HashSet<KddAnnotation>();
					}
					currentPos = 0;
				}
				else if (noneMatch.matches()){ //tag with none concept
					currentDoc.append(noneMatch.group(2).replace('_', ' ')+" ");
					currentPos += noneMatch.group(2).length()+1;
				}
				else if (none2Match.matches()){ //tag with none concept 2
					currentDoc.append(none2Match.group(1)+" ");
					currentPos += none2Match.group(1).length()+1;
				}
				else if (tagMatch.matches()){ //tag with concept
					currentDoc.append(tagMatch.group(2).replace('_', ' ')+" ");
					currentAnns.add(new KddAnnotation(currentPos, tagMatch.group(2).length(), tagMatch.group(4)));				
					currentPos += tagMatch.group(2).length()+1;
					titlesToPrefetch.add(tagMatch.group(4));
				}
				else if (nonAnnMatch.matches()){ //tag with no concept
					currentDoc.append(nonAnnMatch.group(2).replace('_', ' ')+" ");
					currentPos += nonAnnMatch.group(2).length()+1;
				}
				else if (skipMatch.matches()){
					// a word part of continuing tag (has already been added)
				}
				else if (nonAnn2Match.matches()){ //tag with no concept 2
					currentDoc.append(nonAnn2Match.group(1)+" ");
					currentPos += nonAnn2Match.group(1).length()+1;
				}
				else{
					r.close();
					throw new AnnotationException("Dataset is malformed: string '"+line+ "' not recognized.");
				}
			}
			r.close();
		}
		
		/** Prefetch titles */
		api.prefetchTitles(titlesToPrefetch);

		/** Create annotation list */
		for (Set<KddAnnotation> s : kddAnns){
			HashSet<Annotation> sA = new HashSet<Annotation>();
			tags.add(sA);
			for (KddAnnotation aA: s){
				int wid = api.getIdByTitle(aA.title);
				if (wid == -1)
					System.out.println("ERROR: Dataset is malformed: Wikipedia API could not find page "+aA.title);
				else
					sA.add(new Annotation(aA.position, aA.length, wid));

			}

		}



	}
	
	@Override
	public int getSize() {
		return tags.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (Set<Annotation> s : tags)
			count += s.size();
		return count;
	}

	@Override
	public List<Set<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(tags);
	}

	@Override
	public List<Set<Annotation>> getD2WGoldStandardList() {
		return getA2WGoldStandardList();
	}

	@Override
	public List<String> getTextInstanceList() {
		List<String> stringDocuments = new Vector<String>();
		for (MutableString s : documents){
			stringDocuments.add(s.toString());
		}
		return stringDocuments;
	}
	
	@Override
	public List<Set<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return "KDD";
	}

	@Override
	public List<Set<Annotation>> getA2WGoldStandardList() {
		return tags;
	}

	private class KddAnnotation{
		public KddAnnotation(int pos, int len, String title) {
			this.length = len;
			this.position = pos;
			this.title = title;
		}
		public int length, position;
		public String title;
	}

}
