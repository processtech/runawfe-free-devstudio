package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.wfe.ConnectorCallback;
import ru.runa.gpd.wfe.WFEServerProcessDefinitionImporter;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class ImportParWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedParsLabel;
    private Button selectParsButton;
    private Button importFromServerButton;
    private TreeViewer serverDefinitionViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    public ImportParWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Localization.getString("ImportParWizardPage.page.title"));
        setDescription(Localization.getString("ImportParWizardPage.page.description"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        createProjectsGroup(sashForm);
        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        importFromFileButton = new Button(importGroup, SWT.RADIO);
        importFromFileButton.setText(Localization.getString("ImportParWizardPage.page.importFromFileButton"));
        importFromFileButton.setSelection(true);
        importFromFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setImportMode();
            }
        });
        fileSelectionArea = new Composite(importGroup, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionData.heightHint = 30;
        fileSelectionArea.setLayoutData(fileSelectionData);
        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 2;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);
        selectedParsLabel = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedParsLabel.setLayoutData(gridData);
        selectParsButton = new Button(fileSelectionArea, SWT.PUSH);
        selectParsButton.setText(Localization.getString("button.choose"));
        selectParsButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*.par" });
                if (dialog.open() != null) {
                    selectedDirFileName = dialog.getFilterPath();
                    selectedFileNames = dialog.getFileNames();
                    String text = "";
                    for (String fileName : selectedFileNames) {
                        text += fileName + "\n";
                    }
                    selectedParsLabel.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("ImportParWizardPage.page.importFromServerButton"));
        SyncUIHelper.createHeader(importGroup, WFEServerProcessDefinitionImporter.getInstance(), WFEConnectionPreferencePage.class,
                new ConnectorCallback() {

                    @Override
                    public void onSynchronizationFailed(Exception e) {
                        Dialogs.error(Localization.getString("error.Synchronize"), e);
                    }

                    @Override
                    public void onSynchronizationCompleted() {
                    	setupServerDefinitionViewer();
                    }
                });
        createServerDefinitionsGroup(importGroup);
        setControl(pageControl);
    }

    private void setImportMode() {
        boolean fromFile = importFromFileButton.getSelection();
        selectParsButton.setEnabled(fromFile);
        if (fromFile) {
            serverDefinitionViewer.setInput(new Object());
        } else {
            if (WFEServerProcessDefinitionImporter.getInstance().isConfigured()) {
                if (!WFEServerProcessDefinitionImporter.getInstance().hasCachedData()) {
                    long start = System.currentTimeMillis();
                    WFEServerProcessDefinitionImporter.getInstance().synchronize();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo("def sync [sec]: " + ((end - start) / 1000));
                }
                setupServerDefinitionViewer();
            }
        }
    }

    private void setupServerDefinitionViewer(){
    	Map<WfDefinition, List<WfDefinition>> definitions = WFEServerProcessDefinitionImporter.getInstance().loadCachedData();
    	DefinitionNode treeDefinitions = createTree(new TreeMap<>(definitions));
        serverDefinitionViewer.setInput(treeDefinitions);
        serverDefinitionViewer.refresh(true);
    }

    private void createServerDefinitionsGroup(Composite parent) {
        serverDefinitionViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        serverDefinitionViewer.getControl().setLayoutData(gridData);
        serverDefinitionViewer.setContentProvider(new ViewContentProvider());
        serverDefinitionViewer.setLabelProvider(new ViewLabelProvider());
        serverDefinitionViewer.setInput(new Object());
    }

	private void getChildNodes(DefinitionNode node,
			List<WfDefinition> defSelections) {

		if (!node.getChildren().isEmpty()) {
			List<DefinitionNode> nodeList = node.getChildren();
			for (DefinitionNode currentNode : nodeList) {
				if(currentNode != null)
				getChildNodes(currentNode, defSelections);
			}
		} else {
			defSelections.add(node);
		}
	}
    
    public boolean performFinish() {
        InputStream[] parInputStreams = null;
        Process[] processes = null;
        try {
            IContainer container = getSelectedContainer();
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidPar"));
                }
                processes = new Process[selectedFileNames.length];
                parInputStreams = new InputStream[selectedFileNames.length];
                for (int i = 0; i < selectedFileNames.length; i++) {                    
                	processes[i] = new Process(selectedFileNames[i].substring(0, selectedFileNames[i].length() - 4), "", null); //workaround "" processes in root tree have empty path
                    String fileName = selectedDirFileName + File.separator + selectedFileNames[i];
                    parInputStreams[i] = new FileInputStream(fileName);
                }
            } else {
            	TreeItem[] selections = serverDefinitionViewer.getTree().getSelection();
            	List<WfDefinition> defSelections = Lists.newArrayList();
            	            	
            	for(int i = 0; i < selections.length; i++){
            		Object selected = selections[i].getData();
            		//if group is selected
            		DefinitionNode selectedDefinitionNode = (DefinitionNode) selected;            		
            		if (selectedDefinitionNode.isGroup()){ 
            			getChildNodes(selectedDefinitionNode, defSelections);     
               	   }else{
               		    //in case we don't want group name to be imported
               		    selectedDefinitionNode.incrementalPath = "";               		    
               		    defSelections.add((WfDefinition) selectedDefinitionNode);
               	   }
            	}

                 if (defSelections.isEmpty()) {
                     throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidDefinition"));
                 } 

                processes = new Process[defSelections.size()];
                parInputStreams = new InputStream[defSelections.size()];
                for (int i = 0; i < processes.length; i++) {
                	DefinitionNode definitionNode = (DefinitionNode)defSelections.get(i);
                	//adjust path if not entire group is selected
                	String path = !definitionNode.incrementalPath.isEmpty() ? removeNameFromPath(definitionNode.incrementalPath) : definitionNode.incrementalPath;
                    Process process = new Process(definitionNode.getName(), path, definitionNode.getVersion());
                    processes[i] = process;
                    WfDefinition stub = defSelections.get(i);
                    byte[] par = WFEServerProcessDefinitionImporter.getInstance().loadPar(stub);
                    parInputStreams[i] = new ByteArrayInputStream(par);
                }
            }

            for (int i = 0; i < processes.length; i++) {
                String processName = processes[i].getName();
                String processPath = processes[i].getPath();

                processName = !processPath.trim().isEmpty() ? processPath + File.separator + processName : processName;
                //if it is history folder
                if(processName.contains(Localization.getString("ImportParWizardPage.page.oldDefinitionVersions")))
                	processName = processName + "-v"+ processes[i].getVersion();

                IFolder processFolder = IOUtils.getProcessFolder(container, processName);

                if (processFolder.exists()) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.processWithSameNameExists"));
                }

                IOUtils.createFolder(processFolder);

                IOUtils.extractArchiveToFolder(parInputStreams[i], processFolder);
                
                //TODO: check, conflicting with previous logic 
                /*
                IFile definitionFile = IOUtils.getProcessDefinitionFile(processFolder);                
                ProcessDefinition definition = ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                if (definition != null && !Objects.equal(definition.getName(), processFolder.getName())) {
                    // if par name differs from definition name
                    IPath destination = IOUtils.getProcessFolder(container, definition.getName()).getFullPath();
                    processFolder.move(destination, true, false, null);
                    processFolder = IOUtils.getProcessFolder(container, definition.getName());
                    IFile movedDefinitionFile = IOUtils.getProcessDefinitionFile(processFolder);
                    ProcessCache.newProcessDefinitionWasCreated(movedDefinitionFile);
                    ProcessCache.invalidateProcessDefinition(definitionFile);
                }*/
            }
        } catch (Exception exception) {
            PluginLogger.logErrorWithoutDialog("import par", exception);
            setErrorMessage(Throwables.getRootCause(exception).getMessage());
            return false;
        } finally {
            if (parInputStreams != null) {
                for (InputStream inputStream : parInputStreams) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return true;
    }

    private void removeExistedDefinitionFromHistory(WfDefinition definition, List<WfDefinition> historyDefinitions){
    	Long definitionVersion = definition.getVersion();
    	for (WfDefinition historyDefinition : historyDefinitions) {
    		if(definitionVersion == historyDefinition.getVersion())
    			historyDefinitions.remove(historyDefinition);
    		break;
    	}
    }

    class ViewLabelProvider extends LabelProvider {
        @Override
        public String getText(Object obj) {
        	DefinitionNode definitionNode = (DefinitionNode)obj;
            return definitionNode.isHistory() ? definitionNode.getVersion().toString() : definitionNode.getName();
        }
        @Override
        public Image getImage(Object obj) {
        	DefinitionNode definitionNode = (DefinitionNode)obj;
        	return definitionNode.isGroup ? SharedImages.getImage("icons/project.gif") : SharedImages.getImage("icons/process.gif");        	            
        }
    }

    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object parent) {
            return getChildren(parent);
        }

        @Override
        public Object getParent(Object child) {
            if (child instanceof DefinitionNode) {
                return ((DefinitionNode) child).getChildren();
            }
            return null;
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof DefinitionNode) {
                return ((DefinitionNode) parent).getChildren().toArray();
            }
            return new Object[0];
        }

        @Override
        public boolean hasChildren(Object parent) {
            if (parent instanceof DefinitionNode) {
                return !((DefinitionNode) parent).getChildren().isEmpty();
            }
            return false;
        }
    }

    class DefinitionNode extends WfDefinition{    	

		private static final long serialVersionUID = 1L;
		
		private List<DefinitionNode> childs;
        private List<DefinitionNode> leafs;
        private String name;
        private String incrementalPath;
        private Long id = null;    
        private Long version = null;
        private boolean isGroup;
        private boolean isHistory;

        public DefinitionNode( String name, String incrementalPath, Long id, Long version, boolean isGroup, boolean isHistory ) {
            this.childs = new ArrayList<DefinitionNode>();
            this.leafs = new ArrayList<DefinitionNode>();
            this.name = name;
            this.incrementalPath = incrementalPath;            
            this.id = id;
            this.version = version;
            this.isGroup = isGroup;
            this.isHistory = isHistory;
        }        

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getVersion() {
			return version;
		}

		public void setVersion(Long version) {
			this.version = version;
		}

		public boolean isHistory() {
			return isHistory;
		}

		public void setHistory(boolean isHistory) {
			this.isHistory = isHistory;
		}

		public boolean isGroup() {
			return isGroup;
		}

		public void setGroup(boolean isGroup) {
			this.isGroup = isGroup;
		}

		public boolean isLeaf() {
            return childs.isEmpty() && leafs.isEmpty();
        }
	
        public void addElement(String currentPath, String[] list, WfDefinition wfDefinition, List<WfDefinition> historyDefinitions) {
        	String firstInList = list[0];
        	String currentPathAndfirstInList = currentPath+"/"+list[0];
        	//if it is group        	
        	if ( list.length == 1 ) {
        		DefinitionNode currentChild = new DefinitionNode(firstInList, currentPathAndfirstInList, wfDefinition.getId(), wfDefinition.getVersion(), false, false);
        		if(historyDefinitions != null && !historyDefinitions.isEmpty()){
        			removeExistedDefinitionFromHistory(wfDefinition, historyDefinitions);
        			currentChild.getChildren().add(getHistoryGroup(historyDefinitions, currentPath+"/"+list[0]));
        			}
            	childs.add( currentChild );
                return;
            } else {            	
            	DefinitionNode currentChild = new DefinitionNode(firstInList, currentPathAndfirstInList, null, null, true, false);
                int index = childs.indexOf( currentChild );
                if ( index == -1 ) {
                	//child in new group
                    childs.add( currentChild );
                    currentChild.addElement(currentChild.incrementalPath, Arrays.copyOfRange(list, 1, list.length), wfDefinition, historyDefinitions);
                } else {
                	//child in existed group
                	DefinitionNode nextChild = childs.get(index);
                    nextChild.addElement(currentChild.incrementalPath, Arrays.copyOfRange(list, 1, list.length), wfDefinition, historyDefinitions);
                }
            }
        }

        @Override
        public boolean equals(Object obj) {
        	DefinitionNode cmpObj = (DefinitionNode)obj;
            return incrementalPath.equals( cmpObj.incrementalPath ) && name.equals( cmpObj.name );
        }

        public void printNode( int increment ) {
            for (int i = 0; i < increment; i++) {
                System.out.print(" ");
            }
            System.out.println(incrementalPath + (isLeaf() ? " -> " + name : "")  );
            for( DefinitionNode n: childs)
                n.printNode(increment+2);
            for( DefinitionNode n: leafs)
                n.printNode(increment+2);
        }

        @Override
        public String toString() {
            return name;
        }
        
    	public List<DefinitionNode> getChildren() {
    		return childs;
    	}
    }
    
    class DefinitionTree {

    	DefinitionNode root;
    	DefinitionNode commonRoot;

        public DefinitionTree( DefinitionNode root ) {
            this.root = root;
            commonRoot = null;
        }

        public void addElement(WfDefinition wfDefinition, List<WfDefinition> historyDefinitions) {
        	List<String> arrayList = new ArrayList<String>();
        	for(String category : wfDefinition.getCategories()){
        		arrayList.add(category);
        	}
        	
        	arrayList.add(wfDefinition.getName());        	
        	String[] list = arrayList.toArray(new String[0]);
        	
        	root.addElement(root.incrementalPath, list, wfDefinition, historyDefinitions);
        }
      
        public void printTree() {
            getCommonRoot();
            commonRoot.printNode(0);
        }

        public DefinitionNode getCommonRoot() {
            if ( commonRoot != null)
                return commonRoot;
            else {
            	DefinitionNode current = root;
                while ( current.leafs.size() <= 0 ) {
                    current = current.childs.get(0);
                }
                commonRoot = current;
                return commonRoot;
            }
        }
    }
    
    private DefinitionNode createTree(Map<WfDefinition, List<WfDefinition>> definitions) {
		DefinitionTree tree = new DefinitionTree(new DefinitionNode("", "", null, null, false, false));
    	
    	for (Map.Entry<WfDefinition, List<WfDefinition>> entry : definitions.entrySet()){
    		
            WfDefinition wfDefinition = entry.getKey(); 
            List<WfDefinition> historyDefinitions = entry.getValue();           

            tree.addElement(wfDefinition, historyDefinitions);
            }
    	
    	return tree.root;
	}	
        
    public DefinitionNode getHistoryGroup(List<WfDefinition> historyDefinitions, String currentPath){
        DefinitionNode historyGroup = null;        
        String oldDefinitionVersions = Localization.getString("ImportParWizardPage.page.oldDefinitionVersions");
        
        if(historyDefinitions != null && !historyDefinitions.isEmpty()){
         
       	 historyGroup = new DefinitionNode(oldDefinitionVersions, currentPath+"/"+oldDefinitionVersions, null, null, true, false);
       	 
       	 for(WfDefinition historyDefinition : historyDefinitions){
       		 String name = historyDefinition.getName();
       		 Long id = historyDefinition.getId();
       		 Long version = historyDefinition.getVersion();
       		 DefinitionNode historyDefinitionNode = new DefinitionNode(name, currentPath+"/"+oldDefinitionVersions+"/"+name, id, version, false, true);
       		 historyGroup.getChildren().add(historyDefinitionNode);
       	 }
        	}
        return 	historyGroup;	
    }

	class Process{
		private String name;
		private String path;
		private Long version;
		public Process(String name, String path, Long version) {
			super();
			this.name = name;
			this.path = path;
			this.version = version;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public Long getVersion() {
			return version;
		}
		public void setVersion(Long version) {
			this.version = version;
		}
		
	}	

    private String removeNameFromPath(String fullPath){
    	return fullPath.substring(0, fullPath.lastIndexOf("/"));
    }    
    
    private String getNameFromPath(String fullPath){
    	return fullPath.substring(fullPath.lastIndexOf("/"), fullPath.length());
    } 
}
