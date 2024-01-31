package ru.runa.gpd.quick.formeditor;

import java.util.ArrayList;
import java.util.List;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.wfe.var.dto.QuickFormVariable;

public class QuickFormComponent extends QuickFormVariable {
    private String formatLabel;

    public String getFormatLabel() {
        return formatLabel;
    }

    public void setFormatLabel(String formatLabel) {
        this.formatLabel = formatLabel;
    }

    public QuickFormComponent copy() {
        QuickFormComponent result = new QuickFormComponent();
        result.setDescription(this.getDescription());
        result.setFormatLabel(this.getFormatLabel());
        result.setName(this.getName());
        result.setParams(this.copyParams());
        result.setScriptingName(this.getScriptingName());
        result.setTagName(this.getTagName());
        return result;
    }

    public List<Object> copyParams() {
        List<Object> params = this.getParams();
        List<Object> result = new ArrayList<>(params.size());
        for (Object param : params) {
            if (param instanceof String) {
                result.add(param);
            } else { // тип параметра - List<String>
                List<String> paramAsList = (List<String>) param;
                List<String> paramCopy = new ArrayList<>(paramAsList.size());
                for (String item : paramAsList) {
                    paramCopy.add(item);
                }
                result.add(paramCopy);
            }
        }
        return result;
    }

    public void fillFromVariable(Variable variable) {
        this.setName(variable.getName());
        this.setScriptingName(variable.getScriptingName());
        this.setDescription(variable.getDescription());
        this.setFormatLabel(variable.getFormatLabel());
    }
}
