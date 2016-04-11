package ru.runa.gpd.formeditor.wysiwyg;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.VisibilityWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class BrowserWindowHelper implements OpenWindowListener, VisibilityWindowListener, CloseWindowListener {

	private Display display;
	
	public BrowserWindowHelper(Display display) {
		this.display = display;
	}

	public void open(WindowEvent event) {
		if (!event.required) return;

		final Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		Browser browser = new Browser(shell, SWT.NONE);
		browser.addOpenWindowListener(this);
		browser.addVisibilityWindowListener(this);
		browser.addCloseWindowListener(this);
		browser.addTitleListener(new TitleListener() {

			public void changed(TitleEvent event) {
				shell.setText(event.title);
			}
			
		});
		event.browser = browser;
	}

	public void hide(WindowEvent event) {
		Browser browser = (Browser) event.widget;
		Shell shell = browser.getShell();
		shell.setVisible(false);
	}

	public void show(WindowEvent event) {
		Browser browser = (Browser) event.widget;
		Shell shell = browser.getShell();
		if (event.location != null)
			shell.setLocation(event.location);
		if (event.size != null) {
			Point size = event.size;
			shell.setSize(shell.computeSize(size.x, size.y));
		}
		shell.open();
	}

	public void close(WindowEvent event) {
		Browser browser = (Browser) event.widget;
		Shell shell = browser.getShell();
		shell.close();
	}

}
