/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import java.io.Serializable;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;

public class BidiObjectIntHashMap<E> implements Serializable {
	private static final long serialVersionUID = 1L;
	private Object2IntMap<E> o2i = new Object2IntOpenHashMap<E>();
	private Int2ObjectMap<E> i2o = new Int2ObjectOpenHashMap<E>();
	
	public boolean hasObject (E o){
		return o2i.containsKey(o);
	}

	public boolean hasInt (int n){
		return i2o.containsKey(n);
	}
	
	public E getByInt(int n){
		return i2o.get(n);
	}

	public int getByObject(E o){
		return o2i.get(o);
	}
	
	/**Use n==-1 to indicate a missing value.
	 * @param o
	 * @param n must be positive.
	 */
	public void put(E o, int n){
		if (n==-1)
			o2i.put(o, -1);
		else{
			o2i.put(o, n);
			i2o.put(n, o);
		}
	}
	

}
