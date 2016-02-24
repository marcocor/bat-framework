/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.datasetPlugins;

import it.unimi.dsi.lang.MutableString;
import it.unipi.di.acube.batframework.data.Annotation;
import it.unipi.di.acube.batframework.data.Mention;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.A2WDataset;
import it.unipi.di.acube.batframework.utils.AnnotationException;
import it.unipi.di.acube.batframework.utils.ProblemReduction;
import it.unipi.di.acube.batframework.utils.WikipediaApiInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

public class KddDataset implements A2WDataset{
	private List<HashSet<Annotation>> tags = new Vector<HashSet<Annotation>>();
	private List<MutableString> documents = new Vector<MutableString>();
	private Pattern nonePattern = Pattern.compile("^([^\t]*)\t([^\t]*)\tO\tB-.*NONE\tNONE$");
	private Pattern nonePattern2 = Pattern.compile("^([^\t]*)\tO\tO\tO\t.*NONE\tNONE$");
	private Pattern tagPattern = Pattern.compile("^([^\t]*)\t([^\t]*)\tO\tB-([^\t]*)\t([^\t]*)\t([^\t]*)(?:\t([^\t]*)\t([^\t]*))?$");
	private Pattern nonTagPattern = Pattern.compile("^([^\t]*)\t([^\t]*)\tO\tB-([^\t]*)");
	private Pattern skipPattern = Pattern.compile("^([^\t]*)\tO\tO\tI-([^\t]*)$");
	private Pattern endPattern = Pattern.compile("^\\.\tO\tO\t#$");
	private Pattern nonTagPattern2 = Pattern.compile("^([^\t]*)\tO\tO\tO$");

	public KddDataset (InputStream[] inputstreams, WikipediaApiInterface api) throws IOException, AnnotationException, XPathExpressionException, ParserConfigurationException, SAXException{
		List<HashSet<KddAnnotation>> kddAnns = new Vector<HashSet<KddAnnotation>>();
		List<String> titlesToPrefetch = new Vector<String>();
		for (InputStream is: inputstreams){
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			String line;
			MutableString currentDoc = new MutableString();
			HashSet<KddAnnotation> currentAnns = new HashSet<KddAnnotation>();
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
		for (HashSet<KddAnnotation> s : kddAnns){
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

	public static InputStream[] filesToInputStreams(String[] files) throws FileNotFoundException {
		InputStream[] iss = new InputStream[files.length];
		for (int i = 0; i < files.length; i++)
			iss[i] = new FileInputStream(files[i]);
		return iss;
	}
	
	public KddDataset (String[] files, WikipediaApiInterface api) throws IOException, AnnotationException, XPathExpressionException, ParserConfigurationException, SAXException{
		this(filesToInputStreams(files), api);
	}
	
	@Override
	public int getSize() {
		return tags.size();
	}

	@Override
	public int getTagsCount() {
		int count = 0;
		for (HashSet<Annotation> s : tags)
			count += s.size();
		return count;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
		return ProblemReduction.A2WToC2WList(tags);
	}

	@Override
	public List<HashSet<Annotation>> getD2WGoldStandardList() {
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
	public List<HashSet<Mention>> getMentionsInstanceList() {
		return ProblemReduction.A2WToD2WMentionsInstance(getA2WGoldStandardList());
	}

	@Override
	public String getName() {
		return "KDD";
	}

	@Override
	public List<HashSet<Annotation>> getA2WGoldStandardList() {
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
