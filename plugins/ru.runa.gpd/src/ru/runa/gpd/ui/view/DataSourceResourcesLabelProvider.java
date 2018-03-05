package ru.runa.gpd.ui.view;

import java.io.IOException;
import java.io.InputStream;

import org.dom4j.Document;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceType;

public class DataSourceResourcesLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof IResource) {
            String name = ((IResource) element).getName(); 
            return name.substring(0, name.lastIndexOf('.'));
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IFile) {
            IFile file = (IFile) element;
            try (InputStream is = file.getContents()) {
                Document document = XmlUtil.parseWithoutValidation(is);
                switch (DataSourceType.valueOf(document.getRootElement().attribute("type").getValue())) {
                case Excel:
                    return SharedImages.getImage("icons/MS-Excel-2013-icon.png");
                case JDBC:
                    return SharedImages.getImage("icons/jdbc_16.gif");
                case JNDI:
                    return SharedImages.getImage("icons/wildfly_icon_16.png");
                default:
                    return SharedImages.getImage("icons/column_error.gif");
                }
            } catch (IOException | CoreException e) {
                throw new InternalApplicationException(e);
            }
        }
        return SharedImages.getImage("icons/column_error.gif");
    }

}
