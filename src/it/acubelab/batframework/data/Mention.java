package it.acubelab.batframework.data;

import java.io.Serializable;

public class Mention implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private int position; //starting position of the annotation in the original text
	private int length; //length of the annotation in the original text

	public Mention (int position, int length){
		this.position = position;
		this.length = length;
	}
	
	public int getPosition(){
		return position;
	}

	public int getLength() {
		return length;
	}

	@Override public boolean equals(Object m){
		Mention men = (Mention) m;
		return (this.position == men.position && this.length == men.length);
	}

	@Override public int hashCode() {
		return (""+position+length).hashCode();
	}
	
	@Override public Object clone(){
		return new Mention(this.getPosition(), this.getLength());
	}

	public boolean overlaps(Mention m) {
		int p1 = this.getPosition();
		int l1 = this.getLength();		
		int e1 = p1+l1-1;
		int p2 = m.getPosition();
		int l2 = m.getLength();
		int e2 = p2+l2-1;
		return (  (p1 <= p2 &&  p2 <= e1)
				|| (p1 <= e2 &&  e2 <= e1)
				|| (p2 <= p1 &&  p1 <= e2)
				|| (p2 <= e1 &&  e1 <= e2)
				);

	}


}
