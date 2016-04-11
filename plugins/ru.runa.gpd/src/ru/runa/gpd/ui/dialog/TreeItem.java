package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;


public class TreeItem {
    protected String label;
    protected Object tag;
    protected List<TreeItem> children = new ArrayList<TreeItem>();
    protected TreeItem parent;
    protected Color color;
    protected boolean allowSelection = false;
    
    public TreeItem(String label) {
        this.label = label;
    }
    
    public void addChild(TreeItem child) {
        child.parent = this;
        children.add(child);
    }

	public String getLabel() {
		return label;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public void setAllowSelection(boolean allowSelection) {
		this.allowSelection = allowSelection;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

}
