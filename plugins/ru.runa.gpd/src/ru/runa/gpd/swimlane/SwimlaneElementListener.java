package ru.runa.gpd.swimlane;


public interface SwimlaneElementListener {

	public void opened(String path, boolean createNewInitializer);
	
	public void completed(String path, SwimlaneInitializer swimlaneInitializer);
	
}
