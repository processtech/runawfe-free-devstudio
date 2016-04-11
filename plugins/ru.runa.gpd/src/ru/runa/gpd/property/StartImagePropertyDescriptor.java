package ru.runa.gpd.property;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class StartImagePropertyDescriptor extends PropertyDescriptor {
    public StartImagePropertyDescriptor(Object id, String label) {
        super(id, label);
    }

    @Override
    public CellEditor createPropertyEditor(Composite parent) {
        return new ImageCellEditor(parent);
    }

    private IFile getImageFile() {
        return IOUtils.getFile(ParContentProvider.PROCESS_INSTANCE_START_IMAGE_FILE_NAME);
    }

    class ImageCellEditor extends DialogCellEditor {
        private Image image;
        private Label colorLabel;

        public ImageCellEditor(Composite parent) {
            super(parent);
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            FileDialog dialog = new FileDialog(cellEditorWindow.getShell(), SWT.OPEN);
            dialog.setFilterExtensions(new String[] { "*.png" });
            String path = dialog.open();
            if (path == null) {
                return null;
            }
            IOUtils.copyFile(path, getImageFile());
            return getImageFile();
        }

        @Override
        protected Control createContents(Composite cell) {
            Color bg = cell.getBackground();
            colorLabel = new Label(cell, SWT.LEFT);
            colorLabel.setBackground(bg);
            return colorLabel;
        }

        @Override
        protected void updateContents(Object value) {
            try {
                IFile imageFile = getImageFile();
                if (imageFile == null || !imageFile.exists()) {
                    return;
                }
                ImageData data = new ImageData(imageFile.getContents()).scaledTo(16, 16);
                image = new Image(colorLabel.getDisplay(), data, data.getTransparencyMask());
                colorLabel.setImage(image);
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("start image", e);
            }
        }

        @Override
        public void dispose() {
            if (image != null) {
                image.dispose();
                image = null;
            }
            super.dispose();
        }
    }
}
