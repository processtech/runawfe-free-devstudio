package ru.runa.gpd.algorithms;

public class TransitionVector {
	private Vector fromVector;
	private Vector toVector;
	
	public TransitionVector(Vector fromVector, Vector toVector) {
		this.fromVector = new Vector(fromVector.getElements());
		this.toVector = new Vector(toVector.getElements());
	}

	public Vector getFromVector() {
		return fromVector;
	}

	public Vector getToVector() {
		return toVector;
	}
}
