package ru.runa.gpd.algorithms;

import java.util.ArrayList;
import java.util.List;

public class ListUnprocessedStates {
	private Vector firstObj;
	private List<Vector> list = new ArrayList<Vector>();
	
	public void init(Vector obj) {
		list = new ArrayList<Vector>();
		this.firstObj = obj;
		list.add(obj);			
	}
	
	public Vector getFirstObj() {
		return firstObj;
	}
	
	public boolean isFirstObjExist() {
		return firstObj != null;
	}
	
	public List<Vector> getList() {
		return list;
	}
	
	public void addInList(List<Vector> addedCollection) {
		list.addAll(addedCollection);
		if(firstObj == null && list.size() > 0) {
			firstObj = list.get(0);
		}
	}
	
	public void removeFirst() {
		list.remove(firstObj);
		firstObj = null;
		if(list.size() > 0) {
			firstObj = list.get(0);
		}
	}
}
