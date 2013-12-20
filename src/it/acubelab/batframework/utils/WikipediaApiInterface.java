/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import it.unimi.dsi.fastutil.ints.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;


public class WikipediaApiInterface {
	BidiObjectIntHashMap<String> bidiTitle2wid = null;
	File bidiTitle2widCache = null;
	Int2IntMap wid2redirect = null;// mapping between source and destination of a redirect. If the page identified by id is not a redirect, then wid2redirect.get(id) == id
	File wid2redirectCache = null;
	private int queries = 0; //counter for the sent queries

	/**
	 * @param bidiTitle2widCacheFileName pass null to avoid the cache.
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public WikipediaApiInterface(String bidiTitle2widCacheFileName,String wid2redirectCacheFileName) throws FileNotFoundException, IOException, ClassNotFoundException{
		if (bidiTitle2widCacheFileName == null)
			//will not load/store the mapping to a file, keeping all the data in memory
			bidiTitle2wid = new BidiObjectIntHashMap<String>();
		else {
			bidiTitle2widCache = new File(bidiTitle2widCacheFileName);
			if (bidiTitle2widCache.exists() && bidiTitle2widCache.length()>0)
				//load the cached data
				try{
					bidiTitle2wid = (BidiObjectIntHashMap<String>) new ObjectInputStream(new FileInputStream(bidiTitle2widCache)).readObject();
				} catch (Exception e){
					throw new RuntimeException("Could not load cache file "+bidiTitle2widCache.getAbsolutePath()+". Try to manually delete the file to clear the cache. Message: "+e.getMessage());
				}
			else
				//create a new empty mapping to fill in.
				bidiTitle2wid = new BidiObjectIntHashMap<String>();
		}


		if (wid2redirectCacheFileName == null)
			//will not load/store the mapping to a file, keeping all the data in memory
			wid2redirect = new Int2IntOpenHashMap();
		else {
			wid2redirectCache = new File(wid2redirectCacheFileName);
			if (wid2redirectCache.exists() && wid2redirectCache.length()>0)
				//load the cached data
				try{
					wid2redirect = (Int2IntOpenHashMap) new ObjectInputStream(new FileInputStream(wid2redirectCache)).readObject();
				} catch (Exception e){
					throw new RuntimeException("Could not load cache file "+bidiTitle2widCache.getAbsolutePath()+". Try to manually delete the file to clear the cache. Message: "+e.getMessage());
				}
			else
				//create a new empty mapping to fill in.
				wid2redirect = new Int2IntOpenHashMap();
		}
	}

	public void flush() throws FileNotFoundException, IOException{
		if (bidiTitle2widCache != null){
			bidiTitle2widCache.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(bidiTitle2widCache));
			oos.writeObject(bidiTitle2wid);
			oos.close();
		}

		if (wid2redirectCache != null){
			wid2redirectCache.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(wid2redirectCache));
			oos.writeObject(wid2redirect);
			oos.close();
		}
	}

	/**
	 * Query the public wikipedia API to ask the binding between an article name and its unique wikipedia ID.
	 * Note that if the given title redirects to a second page, the id of the second page if returned.
	 * @param title the wikipedia article title. (Eg. "Barack Obama")
	 * @return the wikipedia id for the article, or -1 if the article was not found.
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public int getIdByTitle(String title) throws IOException{
		title = normalize(title);
		if (bidiTitle2wid.hasObject(title))
			return bidiTitle2wid.getByObject(title);
		try {
			Vector<String> v = new Vector<String>(); 
			v.add(title);
			prefetchTitles(v);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new IOException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException("Could not parse wikipedia API response. Message:" + e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		if (!bidiTitle2wid.hasObject(title)) return -1;
		return bidiTitle2wid.getByObject(title);
	}

	/**Query (if the information is not stored in the cache) the Wikipedia API converting a Wikipedia ID to the title of the page.
	 * @param wid the Wikipedia ID.
	 * @return the title of the page whose ID is {@code wid}.
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public String getTitlebyId(int wid) throws IOException{
		if (bidiTitle2wid.hasInt(wid))
			return bidiTitle2wid.getByInt(wid);
		try {
			Vector<Integer> v = new Vector<Integer>();
			v.add(wid);
			prefetchWids(v);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new IOException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException("Could not parse wikipedia API response. Message:" + e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return bidiTitle2wid.getByInt(wid);
	}

	/**
	 * @param wid a Wikipedia ID
	 * @return true iff {@code wid} is a redirect.
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public boolean isRedirect(int wid) throws IOException {
		return (wid != dereference(wid));
	}

	/**Implements the de-reference function: queries the wikipedia API to resolve a redirection.
	 * @param wid a Wikipedia ID.
	 * @return {@code wid} if the page is not a redirect, the Wikipedia ID of the page pointed by the redirection otherwise.
	 * @throws IOException if something went wrong while querying the Wikipedia API.
	 */
	public int dereference(int wid) throws IOException {
		if (wid2redirect.containsKey(wid))
			return wid2redirect.get(wid);
		try {
			Vector<Integer> v = new Vector<Integer>();
			v.add(wid);
			prefetchWids(v);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			throw new IOException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException("Could not parse wikipedia API response. Message:" + e.getMessage());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
		return wid2redirect.get(wid);
	}

	private void incrementAutoFlushCounter() throws FileNotFoundException, IOException{
		if (queries++ % 30 == 0)
			this.flush();
	}

	/**
	 * Note: when this method is called, the titles whose id are in widsToPrefetch must have already been
	 * resolved and cached in bidiTitle2wid
	 * @param widsToPrefetch
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	private void prefetchRedirects(List<Integer> widsToPrefetch) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		if (widsToPrefetch.isEmpty()) return;
		String requestList = "";
		for (int j=0; j < widsToPrefetch.size(); j++)
			requestList += (j==0 ? widsToPrefetch.get(j) : ("|"+widsToPrefetch.get(j)));

		//query to resolve redirections
		incrementAutoFlushCounter();
		URL wikiApi = new URL("http://en.wikipedia.org/w/api.php?format=xml&action=query&prop=info&redirects=&pageids="+requestList);
		System.out.println("Querying "+wikiApi);
		URLConnection wikiConnection = wikiApi.openConnection();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(wikiConnection.getInputStream());

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		/** Non-existing wids */
		XPathExpression expr = xpath.compile("//page[@missing]/pageid");
		NodeList missing = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j=0; j < missing.getLength(); j++){
			String idMissing = missing.item(j).getNodeValue();
			wid2redirect.put(Integer.parseInt(idMissing), -1);
		}

		/** Existing wids */
		expr = xpath.compile("//page/@title");
		NodeList toTitlesNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		for (int i=0; i < toTitlesNodes.getLength(); i++){
			String toTitle = toTitlesNodes.item(i).getNodeValue();
			
			expr = xpath.compile("//page[@title="+escape(toTitle)+"]/@pageid");
			int toId = Integer.parseInt((String) expr.evaluate(doc, XPathConstants.STRING));


			expr = xpath.compile("//r[@to="+escape(toTitle)+"]/@from");
			NodeList fromTitleNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
			/** Wids that are redirects */
			if (fromTitleNodes.getLength() >0) 
				for (int j=0; j < fromTitleNodes.getLength(); j++){
					String fromTitle = fromTitleNodes.item(j).getNodeValue();


					int fromId = getIdByTitle(fromTitle);
					wid2redirect.put(fromId, toId);
					wid2redirect.put(toId, toId);
					bidiTitle2wid.put(toTitle, toId);

				}
			/** Wids that are not redirects */
			else{ 
				wid2redirect.put(toId, toId);
				bidiTitle2wid.put(toTitle, toId);
			}
		}	

	}


	private void processNonRedirectQueryResult(URL query) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		incrementAutoFlushCounter();
		URLConnection wikiConnection = query.openConnection();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(wikiConnection.getInputStream());

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();

		/** Normalized titles */
		HashMap<String, String> normalization = new HashMap<String, String>();
		XPathExpression expr = xpath.compile("//normalized/n/@from");
		NodeList normalizedFromNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j=0; j < normalizedFromNodes.getLength(); j++){
			String normalizedFrom = normalizedFromNodes.item(j).getNodeValue();
			expr = xpath.compile("//normalized/n[@from="+escape(normalizedFrom)+"]/@to");
			String normalizedTo = (String) expr.evaluate(doc, XPathConstants.STRING);
			normalization.put(normalizedTo, normalizedFrom);
		}
		
		/** Missing pages (for title queries) */
		expr = xpath.compile("//page[@missing][@title]/@title");
		NodeList missingTitles = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j=0; j < missingTitles.getLength(); j++)
			bidiTitle2wid.put(missingTitles.item(j).getNodeValue(), -1);
		
		/** Missing pages (for pageids queries) */
		expr = xpath.compile("//page[@missing][@pageid]/@pageid");
		NodeList missingIds = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j=0; j < missingIds.getLength(); j++)
			wid2redirect.put(Integer.parseInt(missingIds.item(j).getNodeValue()), -1);
		

		/** Populate title <-> ID  */
		expr = xpath.compile("//page/@pageid");
		NodeList idNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		for (int j=0; j < idNodes.getLength(); j++){
			String pageid = idNodes.item(j).getNodeValue();
			expr = xpath.compile("//page[@pageid="+escape(pageid)+"]/@title");
			String title = expr.evaluate(doc);
			bidiTitle2wid.put(title, Integer.parseInt(pageid));
		}
		

		/** create references for normalizations*/
		for (String to: normalization.keySet()){
			String from = normalization.get(to);
			try {
				bidiTitle2wid.put(from, bidiTitle2wid.getByObject(to));}
			catch (Exception e){
				System.out.println(to);
				e.printStackTrace();
			}
		}

		/** populate wid->redirect_wid for non-redirect pages */
		expr = xpath.compile("//page[not(@redirect)]/@pageid");
		NodeList nonRedirectIdNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int j=0; j < nonRedirectIdNodes.getLength(); j++){
			int pageid = Integer.parseInt(nonRedirectIdNodes.item(j).getNodeValue());
			wid2redirect.put(pageid, pageid);
		}
			
		/** populate wid->redirect_wid for redirect pages */
		expr = xpath.compile("//page[@redirect]/@pageid");
		NodeList redirectIdNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		Vector<Integer> idsToDereference =  new Vector<Integer>();
		for (int j=0; j < redirectIdNodes.getLength(); j++)
			idsToDereference.add(Integer.parseInt(redirectIdNodes.item(j).getNodeValue()));
		prefetchRedirects(idsToDereference);
		
			
	}

	/**Prefetch the information (id, redirect, ...) of a set of Wikipedia Titles for faster caching.
	 * @param titlesToPrefetch the titles to prefetch.
	 * @throws IOException is the query processing failed.
	 * @throws ParserConfigurationException if the reply parsing failed.
	 * @throws SAXException if the reply parsing failed.
	 * @throws XPathExpressionException if the reply parsing failed.
	 */
	public void prefetchTitles(List<String> titlesToPrefetch) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		final int titlesPerRequest = 50;
		List<String> titlesToActuallyPrefetch = new Vector<String>();
		for (String t: titlesToPrefetch){ // only pre-fetch titles that have not been pre-fetched yet.
			t = normalize(t);
			if (!bidiTitle2wid.hasObject(t))
				titlesToActuallyPrefetch.add(t);
		}

		for (int i=0; i<titlesToActuallyPrefetch.size(); i+=titlesPerRequest){
			String titlesQuery = "";
			for (int j=i; j<titlesToActuallyPrefetch.size() && j < i+titlesPerRequest; j++)
				titlesQuery += (j==i ? "":"|")+URLEncoder.encode(titlesToActuallyPrefetch.get(j), "UTF-8");


			URL wikiApi = new URL("http://en.wikipedia.org/w/api.php?format=xml&action=query&prop=info&titles="+titlesQuery);
			System.out.println("Querying "+wikiApi);
			processNonRedirectQueryResult(wikiApi);
		}
	}

	/**Prefetch the information (title, redirects) of a list of Wikipedia IDs for faster caching.
	 * @param widsToPrefetch the IDs to prefetch.
	 * @throws IOException is the query processing failed.
	 * @throws ParserConfigurationException if the reply parsing failed.
	 * @throws SAXException if the reply parsing failed.
	 * @throws XPathExpressionException if the reply parsing failed.
	 */
	public void prefetchWids(List<Integer> widsToPrefetch) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		final int widsPerRequest = 50;
		Set<Integer> widsToActuallyPrefetchSet = new HashSet<Integer>();
		for (int wid: widsToPrefetch) // only pre-fetch titles that have not been pre-fetched yet.
			if (wid != -1 && !bidiTitle2wid.hasInt(wid))
				widsToActuallyPrefetchSet.add(wid);

		Integer[] widsToActuallyPrefetch = widsToActuallyPrefetchSet.toArray(new Integer[0]);
		for (int i=0; i<widsToActuallyPrefetch.length; i+=widsPerRequest){
			String widsQuery = "";
			for (int j=i; j<widsToActuallyPrefetch.length && j < i+widsPerRequest; j++)
				widsQuery += (j==i ? "":"|") + widsToActuallyPrefetch[j];


			URL wikiApi = new URL("http://en.wikipedia.org/w/api.php?format=xml&action=query&prop=info&pageids="+widsQuery);
			System.out.println("Querying "+wikiApi);
			processNonRedirectQueryResult(wikiApi);
		}
	}
	
	private static String escape(String s) {
	    Matcher matcher = Pattern.compile("['\"]")
	        .matcher(s);
	    StringBuilder buffer = new StringBuilder("concat(");
	    int start = 0;
	    while (matcher.find()) {
	      buffer.append("'")
	          .append(s.substring(start, matcher.start()))
	          .append("',");
	      buffer.append("'".equals(matcher.group()) ? "\"'\"," : "'\"',");
	      start = matcher.end();
	    }
	    if (start == 0) {
	      return "'" + s + "'";
	    }
	    return buffer.append("'")
	        .append(s.substring(start))
	        .append("'")
	        .append(")")
	        .toString();
	  }
	
	/** Normalize a title of a Wikipedia page, replacing substrings formed by underscores `_' to one single space ` '.
	 * @param title the title to normalize.
	 * @return the normalized title.
	 */
	public static String normalize(String title){
		return title.replaceAll("_+", " ");
	}

}
