package ru.runa.gpd.htmleditor.editors;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

import ru.runa.gpd.htmleditor.HTMLPlugin;

public class SoftTabVerifyListener implements VerifyListener {
	
	private boolean useSoftTab;
	private String softTab;
	
	public void verifyText(VerifyEvent evt) {
		if(useSoftTab){
			if(evt.text.equals("\t")){
				evt.text = softTab;
			}
		}
	}
	
	public void preferenceChanged(PropertyChangeEvent event){
		String key = event.getProperty();
		if(key.equals(HTMLPlugin.PREF_USE_SOFTTAB)){
			setUseSoftTab(((Boolean)event.getNewValue()).booleanValue());
		}
		if(key.equals(HTMLPlugin.PREF_SOFTTAB_WIDTH)){
			int width = ((Integer)event.getNewValue()).intValue();
			setSoftTabWidth(width);
		}
	}
	
	public void setUseSoftTab(boolean useSoftTab){
		this.useSoftTab = useSoftTab;
	}
	
	public void setSoftTabWidth(int softTabWidth){
		softTab = "";
		for(int i=0;i<softTabWidth;i++){
			softTab = softTab + " ";
		}
	}
	
}
