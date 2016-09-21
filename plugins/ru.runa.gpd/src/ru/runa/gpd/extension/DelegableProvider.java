package ru.runa.gpd.extension;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.window.Window;
import org.osgi.framework.Bundle;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class DelegableProvider {
    protected Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    protected DelegableConfigurationDialog createConfigurationDialog(IDelegable iDelegable) {
        return new DelegableConfigurationDialog(iDelegable.getDelegationConfiguration());
    }

    public String showConfigurationDialog(IDelegable iDelegable) {
        DelegableConfigurationDialog dialog = createConfigurationDialog(iDelegable);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    /**
     * Validates configuration. Implementors can return <code>false</code> to raise default invalid configuration message. Or can invoke
     * delegable.addError.
     * 
     * @return <code>false</code> for raising default invalid configuration message
     */
    public boolean validateValue(IDelegable iDelegable, List<ValidationError> errors) throws Exception {
        return true;
    }

    /**
     * Callback is invoked when delegable is deleted from process definition.
     * 
     * @param iDelegable
     */
    public void onDelete(IDelegable iDelegable) {
    }

    /**
     * Callback is invoked when delegable is renamed in process definition.
     * 
     * @param iDelegable
     * @param oldName
     * @param newName
     */
    public void onRename(IDelegable iDelegable, String oldName, String newName) {
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
    public void onCopy(IFolder sourceFolder, IDelegable source, String sourceName, IFolder targetFolder, IDelegable target, String targetName) {
        target.setDelegationClassName(source.getDelegationClassName());
        target.setDelegationConfiguration(source.getDelegationConfiguration());
    }

    public List<String> getUsedVariableNames(IDelegable iDelegable) throws Exception {
        String configuration = iDelegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        for (String variableName : iDelegable.getVariableNames(true)) {
            Matcher matcher = Pattern.compile(variableName + "[^\\.]").matcher(configuration);
            if (matcher.find()) {
                result.add(variableName);
            }
        }
        return result;
    }

    public String getConfigurationOnVariableRename(IDelegable iDelegable, Variable currentVariable, Variable previewVariable) {
        return iDelegable.getDelegationConfiguration().replaceAll(Pattern.quote(currentVariable.getName()),
                Matcher.quoteReplacement(previewVariable.getName()));
    }
}
