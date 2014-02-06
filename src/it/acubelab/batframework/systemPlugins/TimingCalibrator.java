/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.systemPlugins;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import it.acubelab.batframework.data.Mention;
import it.acubelab.batframework.problems.*;

public class TimingCalibrator {
	public static long getOffset(TopicSystem s){
		System.out.println("Calibrating "+s.getName()+"...");
		int minsize=3000;
		int n=50;
		Vector<Long> timings = new Vector<Long>();
		for (int i=0; i<n; i++){
			String t = "a";
			for (int j=0;j < minsize+i; j++)
				t+=" ";
			long lastTime = Calendar.getInstance().getTimeInMillis();
			if (s instanceof C2WSystem)
				((C2WSystem)s).solveC2W(t);
			else if (s instanceof D2WSystem)
				((D2WSystem)s).solveD2W(t, new HashSet<Mention>());
			lastTime = Calendar.getInstance().getTimeInMillis() - lastTime;
			timings.add(lastTime);
			//System.out.println("Time:"+lastTime);	
		}
		
		Collections.sort(timings);
		//remove 2 best and 2 worst
		timings.remove(0);
		timings.remove(0);
		timings.remove(timings.size()-1);
		timings.remove(timings.size()-1);
		
		//return avg.
		long sum = 0;
		for (Long t: timings)
			sum+=t;
		System.out.println("Avg. time for an empty query: "+((float)sum/(float)timings.size()));	

		return (long)((float)sum/(float)timings.size());
		
	}

}
