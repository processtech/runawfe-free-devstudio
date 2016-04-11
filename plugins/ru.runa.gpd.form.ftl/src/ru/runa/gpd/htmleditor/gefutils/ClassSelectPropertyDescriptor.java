package ru.runa.gpd.htmleditor.gefutils;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.SelectionDialog;

import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.htmleditor.HTMLUtil;

/**
 * This is PropertyDescriptor which can input a class name directry
 * and select from JDT class selection dialog.
 * 
 * @author takezoe
 */
public class ClassSelectPropertyDescriptor extends AbstractDialogPropertyDescriptor {

	/**
	 * @param id
	 * @param displayName
	 */
	public ClassSelectPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
		
	protected Object openDialogBox(Object obj, Control cellEditorWindow) {
		try {
			// �v���W�F�N�g���擾���邽�߂ɃA�N�e�B�u�ȃG�f�B�^���擾
			IEditorPart editorPart = HTMLUtil.getActiveEditor();
			
			IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput();
			IJavaProject project = JavaCore.create(input.getFile().getProject());
			
			Shell shell = cellEditorWindow.getShell();
			SelectionDialog dialog = JavaUI.createTypeDialog(
					shell,new ProgressMonitorDialog(shell),
					SearchEngine.createJavaSearchScope(new IJavaElement[]{project}),
					IJavaElementSearchConstants.CONSIDER_CLASSES,false);
				
			if(dialog.open()==SelectionDialog.OK){
				Object[] result = dialog.getResult();
				return ((IType)result[0]).getFullyQualifiedName();
			}
		} catch(Exception ex){
			HTMLPlugin.logException(ex);
		}
		return null;
	}
}
