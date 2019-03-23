package ru.runa.gpd.property;

import com.google.common.base.Strings;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.lang.model.Describable;

public class DescribablePropertyDescriptor extends PropertyDescriptor {
    private final Describable describable;
	
    public DescribablePropertyDescriptor(Object id, String displayName, Describable describable) {
		super(id, displayName);
        this.describable = describable;
	}

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new TextAreaDialogCellEditor(parent);
    }

    private class TextAreaDialogCellEditor extends DialogCellEditor {

        public TextAreaDialogCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            TextAreaDialog dialog = new TextAreaDialog(cellEditorWindow.getShell(), describable.getDescription());
            if (dialog.open() == TextAreaDialog.OK) {
                return dialog.getResult();
            }
            return null;
        }
    }

	private class TextAreaDialog extends Dialog {
		private Text text;
		private String result;
		private String initValue;
		
		public TextAreaDialog(Shell parent, String initValue){
			super(parent);
            this.initValue = Strings.nullToEmpty(initValue);
			setShellStyle(getShellStyle()|SWT.RESIZE);
		}
		
		protected Point getInitialSize() {
			return new Point(350, 300);
		}
		
		protected Control createDialogArea(Composite parent) {
			getShell().setText(getDisplayName());
			
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			text = new Text(composite, SWT.MULTI|SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
			text.setLayoutData(new GridData(GridData.FILL_BOTH));
			text.setText(this.initValue);
			
			return composite;
		}

		protected void okPressed() {
			result = this.text.getText();
			super.okPressed();
		}
		
		public String getResult(){
			return this.result;
		}
		
	}

}
