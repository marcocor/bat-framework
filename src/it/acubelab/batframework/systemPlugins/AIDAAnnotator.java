/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import it.acubelab.batframework.data.*;
import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import mpi.aida.config.settings.*;
import mpi.aida.config.settings.preparation.*;
import mpi.aida.data.*;
import mpi.aida.graph.similarity.exception.*;
import mpi.aida.service.*;
import mpi.aida.service.rmi.*;

public abstract class AIDAAnnotator implements Sa2WSystem{

	private WikipediaApiInterface api;
	private String user;
	private String password;
	private String parseServerId;
	private String ip;
	private int port;
	private String serviceId;
	private long lastTime = -1;
	private PreparationSettings pSettings;
	private DisambiguationSettings dSettings;
	private Settings settings;

	public abstract DisambiguationSettings setDisambiguationSettings() throws MissingSettingException;
	
	public AIDAAnnotator(String configFile, WikipediaApiInterface api) throws XPathExpressionException, FileNotFoundException, ParserConfigurationException, SAXException, IOException, AnnotationException{
		loadConfig(configFile);
		setupConnection();
		this.api = api;
	}

	private void loadConfig(String file) throws XPathExpressionException, ParserConfigurationException, FileNotFoundException, SAXException, IOException, AnnotationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new FileInputStream(file));
		user = getConfigValue("access", "user", doc);
		password = getConfigValue("access", "password", doc);
		parseServerId = getConfigValue("access", "parseServerId", doc);
		ip = getConfigValue("access", "ip", doc);
		serviceId = getConfigValue("access", "serviceId", doc);
		String portStr = getConfigValue("access", "port", doc);
		if (user.equals("") || password.equals("") || parseServerId.equals("") || ip.equals("") || serviceId.equals(""))
			throw new AnnotationException("Configuration file "+file+ " has missing values.");
		port = Integer.parseInt(portStr);
	}

	private String getConfigValue(String setting, String name, Document doc) throws XPathExpressionException{
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression userExpr = xpath.compile("aida/setting[@name=\""+setting+"\"]/param[@name=\""+name+"\"]/@value");
		return userExpr.evaluate(doc);
	}

	private void setupConnection() throws AnnotationException{
		AidaRMIClientManager.connect(serviceId, ip, port, user, password);			
		AidaRMIClientManager.setParseServiceId(parseServerId);
		System.out.println("RMI Not connected");
		if (!AidaRMIClientManager.isConnected())
			throw new AnnotationException("Could not connect to AIDA RMI.");
		pSettings = new StanfordHybridPreparationSettings();
		try {
			dSettings = setDisambiguationSettings();
		} catch (MissingSettingException e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		settings = new Settings(pSettings, dSettings);
	}

	@Override
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {

		/* Lazy connection if the connection made by the constructor failed.*/
		if (!AidaRMIClientManager.isConnected())
			setupConnection();

		Set<AIDATag> res = new HashSet<AIDATag>();
		List<String> titlesToPrefetch = new Vector<String>();


		//lastTime = Calendar.getInstance().getTimeInMillis();
		AidaParsePackage input = new AidaParsePackage(text, ""+text.hashCode(), settings);
		AidaResultsPackage result = null;
		try{
			result = AidaRMIClientManager.parse(input);
		} catch (java.lang.NullPointerException e){
			System.out.println("Caught exception while processing text:\n [text beginning]\n"+text+"[text end]");
			throw e;
		} catch (Exception e){
			System.out.println("Caught exception while processing text:\n [text beginning]\n"+text+"[text end]");
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
		//lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;
		//System.out.println(result.getOverallRunTime()+ "\t" + lastTime+"ms");

		if (result == null){
			String noSpaceText = CharUtils.trim(text).toString();
			System.out.println("NULL RESULT: " + noSpaceText.substring(0,Math.min(10, noSpaceText.length())));
		}
		if (result != null){
			Matcher time1 = Pattern.compile("^(\\d*)ms$").matcher(result.getOverallRunTime());
			Matcher time2 = Pattern.compile("^(\\d*)s, (\\d*)ms$").matcher(result.getOverallRunTime());
			Matcher time3 = Pattern.compile("^(\\d*)m, (\\d*)s, (\\d*)ms$").matcher(result.getOverallRunTime());
			Matcher time4 = Pattern.compile("^(\\d*)h, (\\d*)m, (\\d*)s, (\\d*)ms$").matcher(result.getOverallRunTime());
			Matcher time5 = Pattern.compile("^(\\d*)d, (\\d*)h, (\\d*)m, (\\d*)s, (\\d*)ms$").matcher(result.getOverallRunTime());
			if (time1.matches())
				lastTime = Integer.parseInt(time1.group(1));
			else if (time2.matches())
				lastTime = Integer.parseInt(time2.group(1))*1000 + Integer.parseInt(time2.group(2));
			else if (time3.matches())
				lastTime = Integer.parseInt(time3.group(1))*1000*60 + Integer.parseInt(time3.group(2))*1000 + Integer.parseInt(time3.group(3));
			else if (time4.matches())
				lastTime = Integer.parseInt(time4.group(1))*1000*60*60 + Integer.parseInt(time4.group(2))*1000*60 + Integer.parseInt(time4.group(3))*1000 + Integer.parseInt(time4.group(4));
			else if (time5.matches())
				lastTime = Integer.parseInt(time5.group(1))*1000*60*60*24 +Integer.parseInt(time5.group(2))*1000*60*60 + Integer.parseInt(time5.group(3))*1000*60 + Integer.parseInt(time5.group(4))*1000 + Integer.parseInt(time5.group(5));
			else
				throw new AnnotationException("Time value returned by AIDA ["+result.getOverallRunTime()+"] does not match the pattern.");


			DisambiguationResults disRes = result.getDisambiguationResults();
			for (ResultMention mention : disRes.getResultMentions()){
				ResultEntity resEntity = disRes.getBestEntity(mention);

				int position = mention.getCharacterOffset();
				int length = mention.getMention().length();
				String pageTitleEscaped = resEntity.getEntity();
				String pageTitleUnescaped = StringEscapeUtils.unescapeJava(pageTitleEscaped);
				float score = (float) resEntity.getDisambiguationScore();
				if (pageTitleEscaped.equals("--NME--")) //Aida could not identify the topic.
					break;

				res.add(new AIDATag(position, length, pageTitleUnescaped, score));
				titlesToPrefetch.add(pageTitleUnescaped);
			}
		}

		/** Prefetch wids of titles*/
		try {
			api.prefetchTitles(titlesToPrefetch);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}

		/** Convert to Scored Tags*/
		Set<ScoredAnnotation> resScoredAnnotations = new HashSet<ScoredAnnotation>();
		for (AIDATag t: res){
			int wid;
			try {
				wid = api.getIdByTitle(t.title);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AnnotationException(e.getMessage());
			}
			if (wid != -1)
				resScoredAnnotations.add(new ScoredAnnotation(t.position, t.length, wid, t.score));
		}
		return resScoredAnnotations;
	}

	@Override
	public Set<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(solveSa2W(text), Float.MIN_VALUE);
	}

	@Override
	public Set<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(solveA2W(text));
	}

	@Override
	public Set<ScoredTag> solveSc2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToSc2W(this.solveSa2W(text));
	}

	@Override
	public Set<Annotation> solveD2W(String text, Set<Mention> mentions) {
		return ProblemReduction.Sa2WToD2W(solveSa2W(text), mentions, Float.MIN_VALUE);
	}

	@Override
	public abstract String getName();

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}


	private static class AIDATag {
		public AIDATag(int position, int length, String title, float score) {
			this.position = position;
			this.length = length;
			this.title = title;
			this.score = score;
		}
		public float score;
		public int position, length;
		public String title;
	}
}
