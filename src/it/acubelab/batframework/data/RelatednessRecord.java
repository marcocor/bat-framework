package it.acubelab.batframework.data;

import java.io.Serializable;

public class RelatednessRecord implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	private Tag tag1;
	private Tag tag2;
	private float relatedness;

	public RelatednessRecord(int entity1, int entity2, float relatedness) {
		this.tag1 = new Tag(entity1);
		this.tag2 = new Tag(entity2);
		this.relatedness = relatedness;
	}

	public int getEntity1() {
		return tag1.getConcept();
	}

	public int getEntity2() {
		return tag2.getConcept();
	}

	public float getRelatedness() {
		return relatedness;
	}

	@Override
	public Object clone() {
		return new RelatednessRecord(this.getEntity1(), this.getEntity2(),
				this.getRelatedness());
	}
	
	@Override
	public String toString(){
		return "("+tag1.getConcept()+","+tag2.getConcept()+") -> "+relatedness;
	}
}
