/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.CompareToBuilder;

public class Pair<T1 extends Serializable, T2 extends Serializable> implements
		Serializable {
	private static final long serialVersionUID = 1L;
	public T1 first;
	public T2 second;

	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	public int compareTo(Pair<T1, T2> other) {
		return new CompareToBuilder().append(first, other.first)
				.append(second, other.second).toComparison();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Pair<?, ?>) {
			Pair<?, ?> other = (Pair<?, ?>) obj;
			return ObjectUtils.equals(first, other.first)
					&& ObjectUtils.equals(second, other.second);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode())
				^ (second == null ? 0 : second.hashCode());
	}
	
	@Override
	public String toString() {
		return String.format("(%s, %s)", this.first.toString(), this.second.toString());
	}

}
