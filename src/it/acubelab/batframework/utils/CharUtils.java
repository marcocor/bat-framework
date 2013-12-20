/**
 * (C) Copyright 2012-2013 A-cube lab - Università di Pisa - Dipartimento di Informatica. 
 * BAT-Framework is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * BAT-Framework is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with BAT-Framework.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.acubelab.batframework.utils;

public class CharUtils {

	/**
	 * @param c a sequence of chars. Must be in the form "nnn" or "-nnn".
	 * @return the numerical value associated to the char sequence, considered in base 10.
	 * @throws NumberFormatException if the char sequence is not well formed.
	 */
	public static int parseInt(CharSequence c){
		int res = 0;
		int pow = 1;
		boolean negative = false;
		for(int i=c.length()-1; i>=0; i--)
		{
			char n = c.charAt(i);
			int nn=0;
			switch(n){
			case '0': nn=0; break; 
			case '1': nn=1; break;
			case '2': nn=2; break;
			case '3': nn=3; break;
			case '4': nn=4; break;
			case '5': nn=5; break;
			case '6': nn=6; break;
			case '7': nn=7; break;
			case '8': nn=8; break;
			case '9': nn=9; break;
			case '-':
				if (i!=0) throw new NumberFormatException(c.toString());
				negative = true;
				break;
			default: throw new NumberFormatException(c.toString());
			}
			res += pow*nn;
			pow*=10;
		}
		
		return negative? -res : res;
	}

	/**
	 * @param c a sequence of characters.
	 * @return the sequence of chars with leading and trailing spaces removed.
	 */
	public static CharSequence trim(CharSequence c){
		int first = 0;
		int last = c.length()-1;
		while (first<c.length() && Character.isWhitespace(c.charAt(first)))
			first++;
		while (last>=0 && Character.isWhitespace(c.charAt(last)))
				last--;
		last++;
		if (last<first) return c.subSequence(0, 0);
		else return c.subSequence(first, last);
		
	}

}
