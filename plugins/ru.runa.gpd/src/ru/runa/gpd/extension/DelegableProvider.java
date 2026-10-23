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
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
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

    public Object getConfigurationValue(Delegable delegable, String valueId) throws Exception {
        throw new RuntimeException("The function is not implemented yet!");
    }

    public String getExtendedTooltip(Delegable delegable) {
        return delegable.getDelegationConfiguration();
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
     * @param targetFolder
     * @param target
     */
    public void onCopy(IFolder sourceFolder, Delegable source, IFolder targetFolder, Delegable target) {
        target.setDelegationClassName(source.getDelegationClassName());
        target.setDelegationConfiguration(source.getDelegationConfiguration());
        if (source instanceof ScriptTask) {
            ((ScriptTask) target).setUseExternalStorageIn(((ScriptTask) source).isUseExternalStorageIn());
            ((ScriptTask) target).setUseExternalStorageOut(((ScriptTask) source).isUseExternalStorageOut());
        }
    }

    public List<String> getUsedVariableNames(Delegable delegable) throws Exception {
        return findUsedNames(delegable.getDelegationConfiguration(), delegable.getVariableNames(true));
    }

    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) throws Exception {
        return delegable.getDelegationConfiguration().replaceAll(Pattern.quote(currentVariable.getName()),
                Matcher.quoteReplacement(previewVariable.getName()));
    }

    public String getConfigurationOnVariableRename(
            Delegable delegable,
            Variable currentAttribute,
            Variable previewAttribute,
            VariableUserType userType) throws Exception {
        return getConfigurationOnVariableRename(delegable, currentAttribute, previewAttribute);
    }

    public List<String> getUsedUserTypeNames(Delegable delegable) throws Exception {
        return Lists.newArrayList();
    }

    public String getConfigurationOnUserTypeRename(
            Delegable delegable,
            VariableUserType currentUserType,
            VariableUserType previewUserType) throws Exception {
        return delegable.getDelegationConfiguration();
    }

    protected List<String> findUsedNames(String configuration, List<String> names) {
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        for (String name : names) {
            Matcher matcher = Pattern.compile(Pattern.quote(name) + "[^\\.]").matcher(configuration);
            if (matcher.find()) {
                result.add(name);
            }
        }
        return result;
    }
}
