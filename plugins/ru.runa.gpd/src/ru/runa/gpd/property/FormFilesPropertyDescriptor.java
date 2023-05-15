package ru.runa.gpd.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;

public class FormFilesPropertyDescriptor extends PropertyDescriptor {
    private final FormNode formNode;

    public FormFilesPropertyDescriptor(Object id, String label, FormNode formNode) {
        super(id, label);
        this.formNode = formNode;
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new FormFilesCellEditor(parent);
    }

    public class FormFilesCellEditor extends CellEditor {

        public FormFilesCellEditor(Composite parent) {
            super(parent, SWT.NONE);
        }

        @Override
        protected Object doGetValue() {
            return formNode;
        }
        
        @Override
        protected void doSetFocus() {            
        }
        
        @Override
        protected void doSetValue(Object value) {
        }

        @Override
        protected Control createControl(Composite parent) {
            Font font = parent.getFont();
            Color bg = parent.getBackground();
            Composite editor = new Composite(parent, getStyle());
            editor.setFont(font);
            editor.setBackground(bg);
            GridLayout layout = new GridLayout(6, false);
            layout.marginHeight = 0;
            layout.verticalSpacing = 0;
            editor.setLayout(layout);
            createCheckbox(editor, formNode.hasForm());
            if (formNode.hasForm()) {
                String formTypeLabel = FormTypeProvider.getFormType(formNode.getFormType()).getName();
                createLabel(editor, formTypeLabel);
            } else {
                createLabel(editor, Localization.getString("FormNode.property.formFile"));
            }
            createCheckbox(editor, formNode.hasFormValidation());
            createLabel(editor, Localization.getString("FormNode.property.formValidation"));
            createCheckbox(editor, formNode.hasFormScript());
            createLabel(editor, Localization.getString("FormNode.property.formScript"));
            return editor;
        }

        private void createCheckbox(Composite parent, boolean selected) {
            Label label = new Label(parent, SWT.NONE);
            label.setFont(parent.getFont());
            label.setBackground(parent.getBackground());
            label.setImage(SharedImages.getImage(selected ? "icons/checked.gif" : "icons/unchecked.gif"));
        }

        private void createLabel(Composite parent, String text) {
            Label label = new Label(parent, SWT.NONE);
            label.setFont(parent.getFont());
            label.setBackground(parent.getBackground());
            label.setText(text);
        }

    }
}
