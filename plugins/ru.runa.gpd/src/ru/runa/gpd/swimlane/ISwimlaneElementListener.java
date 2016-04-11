package ru.runa.gpd.swimlane;


public interface ISwimlaneElementListener {

	public void opened(String path, boolean createNewInitializer);
	
	public void completed(String path, SwimlaneInitializer swimlaneInitializer);
	
}
