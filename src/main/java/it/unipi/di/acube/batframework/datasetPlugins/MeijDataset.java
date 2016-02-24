/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.unipi.di.acube.batframework.datasetPlugins;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unipi.di.acube.batframework.data.Tag;
import it.unipi.di.acube.batframework.problems.Rc2WDataset;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MeijDataset implements Rc2WDataset{
	private List<String> texts;
	private List<HashSet<Tag>> tags;
	private List<List<Tag>> rankedTags;

	public MeijDataset(String tweetsFile, String tagsFile, String rankFile) throws FileNotFoundException, IOException {
		this(new FileInputStream(tweetsFile), new FileInputStream(tagsFile), new FileInputStream(rankFile));
	}

	public MeijDataset(InputStream tweetsIs, InputStream tagsIs, InputStream rankIs) throws IOException {
		Object2ObjectOpenHashMap<String, MeijDocument> docs = ReadTweetFile(tweetsIs);
		readTagFile(tagsIs, docs);
		loadRankedTags(rankIs, docs);

		this.texts = new Vector<String>();
		
		this.tags = new Vector<HashSet<Tag>>();
		for (Map.Entry<String, MeijDocument> e: docs.entrySet()){
			texts.add(e.getValue().text);
			HashSet<Tag> anns = new HashSet<Tag>();
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

	private static Object2ObjectOpenHashMap<String,MeijDocument> ReadTweetFile(InputStream inputStream) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		Object2ObjectOpenHashMap<String,MeijDocument> docs= new Object2ObjectOpenHashMap<String,MeijDocument>();

		String l;
		while((l = br.readLine())!=null){
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

	private static void readTagFile(InputStream inputStream, Object2ObjectOpenHashMap<String, MeijDocument> docs)
	        throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String l;
		while ((l = br.readLine()) != null) {
			String[] seq = l.toString().split("\t");
			// long id=Long.parseLong(seq[0]);
			if (Integer.parseInt(seq[1]) >= 0)
				docs.get(seq[0]).tags.add(Integer.parseInt(seq[1]));
			// if(!seq[2].equals("-"))
			// docs.get(seq[0]).annotations.add(HTMLParser.html2Unicode(seq[2]));
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


	private static void loadRankedTags(InputStream inputStream, Object2ObjectOpenHashMap<String,MeijDocument> docs) throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String l;
		while ((l = br.readLine()) != null) {
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
		for (HashSet<Tag> s: tags){
			c+=s.size();
		}
		return c;
	}

	@Override
	public List<HashSet<Tag>> getC2WGoldStandardList() {
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
		HashSet<Integer> tags;
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
