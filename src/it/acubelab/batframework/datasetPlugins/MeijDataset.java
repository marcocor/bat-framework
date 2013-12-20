/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.datasetPlugins;

import it.acubelab.batframework.data.Tag;
import it.acubelab.batframework.problems.Rc2WDataset;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.io.FileLinesCollection;
import it.unimi.dsi.io.FileLinesCollection.FileLinesIterator;
import it.unimi.dsi.lang.MutableString;

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;


public class MeijDataset implements Rc2WDataset{
	private List<String> texts;
	private List<Set<Tag>> tags;
	private List<List<Tag>> rankedTags;

	public MeijDataset(String tweetsFile, String tagsFile, String rankFile){
		Object2ObjectOpenHashMap<String,MeijDocument> docs=ReadTweetFile(tweetsFile);
		readTagFile(tagsFile,docs);
		loadRankedTags(rankFile, docs);
		 //Dump information about parsed tweets
		 
/*		System.out.println(docs.keySet().size()+" tweets found");
		int annotated=0;
		int n_annots=0;
		for(MeijDocument d: docs.values()){
			if(d.annotations.size()>0 && !d.text.equals("null")) {
				annotated++;
				n_annots+=d.annotations.size();
				System.out.println(d.id+":"+d.text);
			}
		}

		System.out.println(annotated+" tweets in the dataset have at least one annotation and their text is not null");
		System.out.println("Anntoation for tweet (average) "+(float)n_annots/(float)annotated);
*/
		this.texts = new Vector<String>();
		
		this.tags = new Vector<Set<Tag>>();
		for (Map.Entry<String, MeijDocument> e: docs.entrySet()){
			texts.add(e.getValue().text);
			Set<Tag> anns = new HashSet<Tag>();
			tags.add(anns);
			for (int a: e.getValue().tags){
				anns.add(new Tag(a));
			}
		}
		
		this.rankedTags = new Vector<List<Tag>>();
		for (Map.Entry<String, MeijDocument> e: docs.entrySet()){
			List<Tag> rankedAnns = new Vector<Tag>();
			rankedTags.add(rankedAnns);
			for (int a: e.getValue().ranked){
				rankedAnns.add(new Tag(a));
			}
		}
	}

	private static Object2ObjectOpenHashMap<String,MeijDocument> ReadTweetFile(String path){
		FileLinesIterator iter = new FileLinesCollection(path, "UTF-8").iterator();
		Object2ObjectOpenHashMap<String,MeijDocument> docs= new Object2ObjectOpenHashMap<String,MeijDocument>();

		while(iter.hasNext()){
			MutableString l=iter.next();
			String[] seq= l.toString().split("\t");

			MeijDocument d= new MeijDocument();
			d.id=seq[0];
			//d.author=seq[1];
			d.text=CleanTweet(seq[4]);
			//d.text=seq[4];

			docs.put(d.id, d);

		}
		return docs;
	}

	private static void readTagFile(String path,Object2ObjectOpenHashMap<String,MeijDocument> docs){
		FileLinesIterator iter = new FileLinesCollection(path, "UTF-8").iterator();
		while(iter.hasNext()){
			MutableString l=iter.next();
			String[] seq= l.toString().split("\t");
			//long id=Long.parseLong(seq[0]);
			if(Integer.parseInt(seq[1])>=0)
				docs.get(seq[0]).tags.add(Integer.parseInt(seq[1]));
//			if(!seq[2].equals("-"))
//				docs.get(seq[0]).annotations.add(HTMLParser.html2Unicode(seq[2]));
			
		}
	}

	private static String CleanTweet(String original){
		Pattern PAT_DOC = Pattern.compile("http://|bit|yfrog|tinyurl|twitpic|justgiving|plixi");
		Matcher m = PAT_DOC.matcher(original);

		while(m.find()){
			int start=m.start(0);
			int end=start;

			while(end<original.length()){
				if(original.charAt(end) ==' ') break;
				end++;
			}
			original=original.replace(original.substring(start,end)," ");
			m = PAT_DOC.matcher(original);
		}
		return original;
	}


	private static void loadRankedTags(String path, Object2ObjectOpenHashMap<String,MeijDocument> docs){
		FileLinesIterator iter = new FileLinesCollection(path, "UTF-8").iterator();

		while(iter.hasNext()){
			MutableString l=iter.next();
			String[] seq= l.toString().split(" ");
			if(docs.containsKey(seq[0]))
				docs.get(seq[0]).ranked.add(new Integer(Integer.parseInt(seq[2])));

		}

	}
	
	@Override
	public int getSize() {
		return texts.size();
	}


	@Override
	public int getTagsCount() {
		int c=0;
		for (Set<Tag> s: tags){
			c+=s.size();
		}
		return c;
	}

	@Override
	public List<Set<Tag>> getC2WGoldStandardList() {
		return tags;
	}


	@Override
	public List<String> getTextInstanceList() {
		return texts;
	}


	private static class MeijDocument implements Serializable {

		private static final long serialVersionUID = 6977622102826151597L;
		//String author;
		String text;
		String id;
		Set<Integer> tags;
		Vector<Integer> ranked;
		public MeijDocument(){
			tags=new HashSet<Integer>();
			ranked=new Vector<Integer>();
		}
	}


	@Override
	public String getName() {
		return "Meij";
	}

	@Override
	public List<List<Tag>> getRc2WGoldStandardList() {
		return rankedTags;
	}

}
