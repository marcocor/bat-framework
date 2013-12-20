/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package test;


import it.acubelab.batframework.utils.WikipediaApiInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;


public class TestWikipediaApi {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws Exception {
		WikipediaApiInterface api = new WikipediaApiInterface(null, null);
		dumpTitle("Obama", api);
		dumpTitle("Unintended pregnancy", api);
		dumpTitle("Unintended pregnancy", api);
		dumpTitle("Unintended Pregnancy", api);
		dumpTitle("Unintended Pregnancy", api);
		dumpTitle("Barack Obama", api);
		dumpTitle("Fidel", api);
		dumpTitle("Fidel Castro", api);
		dumpTitle("Google", api);
		dumpTitle("Google, Inc.", api);
		dumpWid(534366, api);
		dumpWid(12736609, api);
		dumpWid(534366, api);
		dumpWid(12736609, api);
		
		dumpRedirect(534366, api);
		System.out.println("(should not be redirect)");
		
		dumpRedirect(12736609, api);
		System.out.println("(should be redirect)");
		
		List<String> titlesToPrefetch = new Vector<String>();
		titlesToPrefetch.add("Barack Hussein Obama");
		titlesToPrefetch.add("Barack Hussein Obama");
		titlesToPrefetch.add("Barack Hussein Obama");
		titlesToPrefetch.add("Barack Obama");
		titlesToPrefetch.add("barack Obama");
		titlesToPrefetch.add("obama");
		titlesToPrefetch.add("Obama");
		titlesToPrefetch.add("The New Testament");
		titlesToPrefetch.add("New Testament");
		titlesToPrefetch.add("Google, Inc.");
		titlesToPrefetch.add("Fidel Castro");
		titlesToPrefetch.add("IBM");
		titlesToPrefetch.add("asdadsa");
		
		
		api.prefetchTitles(titlesToPrefetch);
		
		dumpTitle("asdadsa", api);
		dumpTitle("asdadsa", api);
		dumpTitle("asdadsa", api);
		dumpTitle("asdadsa", api);
		
		api.flush();
		
	}
	
	public static void dumpTitle (String str, WikipediaApiInterface api) throws Exception{
		int wid = api.getIdByTitle(str);
		System.out.println(str + " -> "+wid);
	}
	
	public static void dumpWid (int wid, WikipediaApiInterface api) throws Exception{
		String title = api.getTitlebyId(wid);
		System.out.println(wid + " -> "+title);
	}
	
	public static void dumpRedirect (int wid, WikipediaApiInterface api) throws Exception{
		boolean redirect = api.isRedirect(wid);
		System.out.println(wid + " -> "+(redirect? "Redirect" : "not-redirect"));
	}
	
	

}
