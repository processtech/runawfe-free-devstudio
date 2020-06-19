package ru.runa.gpd.extension;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;

public class DelegableProvider {
    protected Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new DelegableConfigurationDialog(delegable.getDelegationConfiguration());
    }

    public String showConfigurationDialog(Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        DelegableConfigurationDialog dialog = createConfigurationDialog(delegable);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    public Object showEmbeddedConfigurationDialog(final Composite mainComposite, Delegable delegable, DialogEnhancementMode dialogEnhancementMode) {
        throw new RuntimeException("Embedded dialog is not implemented yet!");
    }

    /**
     * Validates configuration. Implementors can return <code>false</code> to raise default invalid configuration message. Or can invoke
     * delegable.addError.
     * 
     * @return <code>false</code> for raising default invalid configuration message
     */
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) throws Exception {
        return true;
    }

    /**
     * Callback is invoked when delegable is deleted from process definition.
     * 
     * @param delegable
     */
    public void onDelete(Delegable delegable) {
    }

    /**
     * Callback is invoked when delegable is renamed in process definition.
     * 
     * @param delegable
     * @param oldName
     * @param newName
     */
    public void onRename(Delegable delegable, String oldName, String newName) {
    }

    /**
     * Callback is invoked when delegable is copied in process definition.
     * 
     * @param sourceFolder
     * @param source
     * @param sourceName
     * @param target
     * @param targetFolder
     * @param targetName
     */
    public void onCopy(IFolder sourceFolder, Delegable source, String sourceName, IFolder targetFolder, Delegable target, String targetName) {
        target.setDelegationClassName(source.getDelegationClassName());
        target.setDelegationConfiguration(source.getDelegationConfiguration());
    }

    public List<String> getUsedVariableNames(Delegable delegable) throws Exception {
        String configuration = delegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        for (String variableName : delegable.getVariableNames(true)) {
            Matcher matcher = Pattern.compile(variableName + "[^\\.]").matcher(configuration);
            if (matcher.find()) {
                result.add(variableName);
            }
        }
        return result;
    }

    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        return delegable.getDelegationConfiguration().replaceAll(Pattern.quote(currentVariable.getName()),
                Matcher.quoteReplacement(previewVariable.getName()));
    }
}
