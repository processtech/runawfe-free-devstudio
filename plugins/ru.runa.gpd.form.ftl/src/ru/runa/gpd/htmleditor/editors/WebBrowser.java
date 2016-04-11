package ru.runa.gpd.htmleditor.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;

import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLUtil;

/**
 * A simple web browser which works as an Eclipse editor.
 * It can be used to debug of web applications.
 * <p>
 *   This browser allows only IFileEditorInput and URLEditorInput as an editor input.
 *   In the case of IFileEditorInput, it works as following:
 * </p>
 * <ul>
 *   <li>*.url (URL shortcut) - 
 *       search an entry which starts with 'URL=', and open that URL in the web browser.</li>
 *   <li>etc - open in the web browser as HTML file.</li>
 * </ul>
 * 
 * @author Naoki Takezoe
 * @since 1.4.1
 */
public class WebBrowser extends EditorPart {
	
	private Text textUrl;
	private Browser browser;
	
	private ToolItem buttonRun;
	private ToolItem buttonForward;
	private ToolItem buttonBackword;
	private ToolItem buttonRefresh;
	
	
	public WebBrowser() {
		super();
	}

	public void doSave(IProgressMonitor monitor) {
		// nothing to do
	}

	public void doSaveAs() {
		// nothing to do
	}
	
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if(input instanceof IFileEditorInput || input instanceof URLEditorInput){
			setSite(site);
			setInput(input);
		} else {
			throw new PartInitException(
				"Web browser allows only IFileEditorInput or URLEditorInput.");
		}
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}
	
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		if(input instanceof IFileEditorInput){
			if(browser!=null){
				IFile file = ((IFileEditorInput)input).getFile();
				// .url file (URL shortcut)
				if(file.getName().endsWith(".url")){
					try {
						String source = new String(HTMLUtil.readStream(file.getContents()));
						String[] dim = source.split("(\r\n|\r|\n)");
						for(int i=0;i<dim.length;i++){
							if(dim[i].startsWith("URL=")){
								String url = dim[i].substring(4,dim[i].length()).trim();
								textUrl.setText(url);
								browser.setUrl(url);
								break;
							}
						}
					} catch(Exception ex){
						HTMLPlugin.logException(ex);
					}
				// etc (process as a HTML file)
				} else {
					String url = "file://" + file.getLocation().makeAbsolute().toFile().getAbsolutePath().replaceAll("\\\\","/");
					textUrl.setText(url);
					browser.setUrl(url);
				}
			}
		} else if(input instanceof URLEditorInput){
			if(browser!=null){
				String url = ((URLEditorInput)input).getUrl();
				if(url!=null && !url.equals("")){
					textUrl.setText(url);
					browser.setUrl(url);
				}
			}
		}
	}
	
	public Browser getBrowser(){
		return this.browser;
	}
	
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent,SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth  = 0;
		composite.setLayout(layout);
		
		Composite northPane = new Composite(composite,SWT.NULL);
		GridLayout northPaneLayout = new GridLayout(2,false);
		northPaneLayout.marginHeight = 0;
		northPaneLayout.marginWidth  = 0;
		northPane.setLayout(northPaneLayout);
		northPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		textUrl = new Text(northPane,SWT.BORDER);
		textUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textUrl.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent e){
				if(e.keyCode==SWT.CR || e.keyCode==SWT.LF){
					browser.setUrl(textUrl.getText());
				}
			}
		});
		
		ToolBar toolbar = new ToolBar(northPane,SWT.FLAT);
		
		buttonRun = new ToolItem (toolbar,SWT.PUSH);
		buttonRun.setImage(HTMLPlugin.getImage(HTMLPlugin.ICON_RUN));
		buttonRun.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent evt){
				browser.setUrl(textUrl.getText());
			}
		});
		
		buttonRefresh = new ToolItem (toolbar,SWT.PUSH);
		buttonRefresh.setImage(HTMLPlugin.getImage(HTMLPlugin.ICON_REFRESH));
		buttonRefresh.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent evt){
				browser.refresh();
			}
		});
		
		buttonBackword = new ToolItem (toolbar,SWT.PUSH);
		buttonBackword.setImage(HTMLPlugin.getImage(HTMLPlugin.ICON_BACKWARD));
		buttonBackword.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent evt){
				browser.back();
			}
		});
		
		buttonForward = new ToolItem (toolbar,SWT.PUSH);
		buttonForward.setImage(HTMLPlugin.getImage(HTMLPlugin.ICON_FORWARD));
		buttonForward.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent evt){
				browser.forward();
			}
		});
		
		browser = new Browser(composite,SWT.NULL);
		browser.setLayoutData(new GridData(GridData.FILL_BOTH));
		browser.addOpenWindowListener(new OpenWindowListener(){
			public void open(WindowEvent event){
				try {
					URLEditorInput input = new URLEditorInput(null);
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = window.getActivePage();
					IDE.openEditor(page, input, "ru.runa.gpd.htmleditor.editors.WebBrowser");
					IEditorPart editorPart = page.getActiveEditor();
					if(editorPart instanceof WebBrowser){
						event.browser = ((WebBrowser)editorPart).getBrowser();
					}
				} catch(PartInitException ex){
					HTMLPlugin.logException(ex);
				}
			}
		});
		browser.addTitleListener(new TitleListener(){
			public void changed(TitleEvent event) {
				setPartName(event.title);
				textUrl.setText(browser.getUrl());
				refreshActions();
			}
		});
		
		setInput(getEditorInput());
	}
	
	private void refreshActions(){
		buttonBackword.setEnabled(browser.isBackEnabled());
		buttonForward.setEnabled(browser.isForwardEnabled());
	}
	
	public void setFocus() {
		browser.setFocus();
	}

}
