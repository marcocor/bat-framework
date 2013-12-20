/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import it.acubelab.batframework.data.*;
import it.acubelab.batframework.problems.*;
import it.acubelab.batframework.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

import CommonSenseWikifier.ReferenceAssistant;
import CommonSenseWikifier.ProblemRepresentationDatastructures.*;
import CommonSenseWikifier.TrainingAndInference.InferenceEngine;


public class IllinoisAnnotator_Server implements Sa2WSystem {
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private static final int port = 1666;
	private long lastTime = -1;

	@Override
	public Set<ScoredAnnotation> solveSa2W(String text) throws AnnotationException {
		if (ois==null)
			try {
				Socket s = new Socket("localhost", port);
				System.out.println("Open socket "+s);
				oos = new ObjectOutputStream(s.getOutputStream());
				ois = new ObjectInputStream(s.getInputStream());
				System.out.println("Open Object input/output streams.");
			} catch (Exception e) {
				e.printStackTrace();
				throw new AnnotationException(e.getMessage());
			}
		try {
			System.out.println("Sending text.");
			oos.writeObject(text);
			oos.flush();
			System.out.println("Text sent, waiting for response.");
			lastTime = Calendar.getInstance().getTimeInMillis();
			@SuppressWarnings("unchecked")
			Set<ScoredAnnotation> res = (Set<ScoredAnnotation>) ois.readObject();
			lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException(e.getMessage());
		}
	}

	@Override
	public Set<Annotation> solveA2W(String text) throws AnnotationException {
		return ProblemReduction.Sa2WToA2W(this.solveSa2W(text), Float.MIN_VALUE);
	}

	@Override
	public Set<Tag> solveC2W(String text) throws AnnotationException {
		return ProblemReduction.A2WToC2W(this.solveA2W(text));
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
	public String getName() {
		return "Illinois Wikifier";
	}

	/* TODO: This is an incredibly awkward workaround to solve the problem of non-backward compatible
	 * apache library. When this problem will be cleanly solved, there will be no need to create an ad-hoc
	 * process for the Illinois wikifier. */
	public static void main (String[] args) throws Exception{
		InferenceEngine inference;

		try {
			ParametersAndGlobalVariables.loadConfig("benchmark/configs/illinois/Benchmark");
			ReferenceAssistant.initCategoryAttributesData(ParametersAndGlobalVariables.pathToTitleCategoryKeywordsInfo);
			inference = new InferenceEngine(false);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AnnotationException("Could not create Illinois Wikifier tagger. Message: "+e.getMessage());
		}

		ServerSocket ss = new ServerSocket(port);
		while(true){
			System.out.println("Waiting for a connection...");
			Socket s = ss.accept();
			System.out.println("Accepted connection from "+s.getInetAddress());
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			while (true){
				System.out.print("Waiting for a string to process...");
				String text;
				try{
					text = (String) ois.readObject();
				} catch (EOFException e){
					ois.close();
					oos.close();
					s.close();
					break;
				}
				System.out.println(" got string: "+text);
				TextAnnotation ta;
				DisambiguationProblem problem = null;
				try {
					ta = ParametersAndGlobalVariables.curator.getTextAnnotation(text);
					problem=new DisambiguationProblem("dummy", ta, new Vector<ReferenceInstance>());
					inference.annotate(problem, null, false, false, 0);
					//				String wikificationString = problem.wikificationString(false);
				} catch (Exception e) {
					e.printStackTrace();
				} 

				Set<ScoredAnnotation> annotations = new HashSet<ScoredAnnotation>();
				for (WikifiableEntity e : problem.components){
					annotations.add(new ScoredAnnotation(e.startOffsetCharsInText, e.entityLengthChars, e.topDisambiguation.wikiData.basicTitleInfo.getTitleId(), ((float) e.linkerScore+3)/7));
					System.out.println("adding annotation: "+e.topDisambiguation.wikiData.basicTitleInfo.getTitleId()+ " linkerScore:"+e.linkerScore+ " normLinkerScore:"+(((float) e.linkerScore+3)/7)+ " rankerscore:"+e.topDisambiguation.rankerScore);
				}

				oos.writeObject(annotations);
				oos.flush();


			}
		}
	}

	@Override
	public long getLastAnnotationTime() {
		return lastTime;
	}

}
