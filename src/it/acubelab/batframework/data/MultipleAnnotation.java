package it.acubelab.batframework.data;

public class MultipleAnnotation extends Mention {
	private static final long serialVersionUID = 1L;
	
	private int[] candidates;
	
	public MultipleAnnotation(int position, int length, int[] candidates){
		super(position, length);
		this.candidates = candidates;
	}
	
	public int[] getCandidates(){
		return candidates;
	}

}
