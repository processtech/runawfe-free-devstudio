package ru.runa.gpd.algorithms;

public class Vector {
	private int[] elements;
	
	public Vector(int size) {
		elements = new int[size];
		setInitValue();
	}
	
	public Vector(int[] arrays) {
		elements = new int[arrays.length];
		for(int i = 0; i < elements.length; i++) {
			elements[i] = arrays[i];
		}
	}
	
	public void setInitValue() {
		for(int i = 0; i < elements.length; i++) {
			elements[i] = 0;
		}
	}
	
	public void setElementValue(int index, int value) {
		elements[index] = value;
	}
	
	public Vector getVectorsSum(Vector second) {
		Vector returnVector = new Vector(elements.length);
		for(int i = 0; i < elements.length; i++) {
			returnVector.setElementValue(i, elements[i] + second.getElements()[i]);
		}
		return returnVector;
	}
	
	public int[] getElements() {
		return elements;
	}
	
	public boolean isNegativeNumberExist() {
		for(int i = 0; i < elements.length; i++) {
			if(elements[i] < 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isNullValueVector() {
		int sum = 0;
		for(int i = 0; i < elements.length; i++) {
			sum += elements[i];
		}
		return sum == 0;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < elements.length; i++) {
			str.append(elements[i]);
		}
		return str.toString();
	}
}
