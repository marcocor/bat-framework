package it.unipi.di.acube.batframework.data;

import java.io.Serializable;

public class Mention implements Serializable, Cloneable, Comparable<Mention> {
	private static final long serialVersionUID = 1L;
	private int position; // starting position of the annotation in the original
							// text
	private int length; // length of the annotation in the original text

	public Mention(int position, int length) {
		this.position = position;
		this.length = length;
	}

	/**
	 * Returns the index of the first Unicode code point of this mention. The index is in terms of Unicode code points. In other words, this function returns the number of Unicode code points before the beginning of this mention. Note that this corresponds to the position of the corresponding character only if the text does not contain Unicode pair surrogates.
	 * @return the position of the first unicode code point of this mention. 
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Returns the length of this mention in terms of Unicode code points. Note that this corresponds to the length of the corresponding string only if it does not contain Unicode pair surrogates.
	 * @return the length of this mention. 
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Returns the index of the first Unicode code point after this mention. The index is in terms of Unicode code points. In other words, this function returns the number of Unicode code points before the first point after this mention. Note that this corresponds to the position of the corresponding character only if the text does not contain Unicode pair surrogates.
	 * @return the position of the first unicode code point after this mention. 
	 */
	public int getEnd() {
		return position + length;
	}

	@Override
	public boolean equals(Object m) {
		Mention men = (Mention) m;
		return (this.position == men.position && this.length == men.length);
	}

	@Override
	public int hashCode() {
		return new Integer(position).hashCode()
				^ new Integer(length).hashCode();
	}

	@Override
	public Object clone() {
		return new Mention(this.getPosition(), this.getLength());
	}

	public boolean overlaps(Mention m) {
		int p1 = this.getPosition();
		int l1 = this.getLength();
		int e1 = p1 + l1 - 1;
		int p2 = m.getPosition();
		int l2 = m.getLength();
		int e2 = p2 + l2 - 1;
		return ((p1 <= p2 && p2 <= e1) || (p1 <= e2 && e2 <= e1)
				|| (p2 <= p1 && p1 <= e2) || (p2 <= e1 && e1 <= e2));

	}

	@Override
	public int compareTo(Mention m) {
		return this.getPosition() - m.getPosition();
	}

	@Override
	public String toString() {
		return String.format("(%d, %d)", this.position, this.length);
	}

	public String getMentionString(String text) {
		int startCharIndex = text.offsetByCodePoints(0, position);
		int endCharIndex = text.offsetByCodePoints(startCharIndex, length);
		return text.substring(startCharIndex, endCharIndex);
	}
}
