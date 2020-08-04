package ru.runa.gpd.util.docx;

import java.util.List;
import java.util.Map;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ColumnExpansionOperation extends AbstractIteratorOperation {
    private String containerSelector;

    public void setContainerSelector(String containerSelector) {
        this.containerSelector = containerSelector;
    }

    @Override
    public void setContainerVariable(WfVariable containerVariable) {
        super.setContainerVariable(containerVariable);
        if (iterateBy == null) {
            if (containerVariable.getValue() instanceof Map) {
                iterateBy = IterateBy.values;
            }
            if (containerVariable.getValue() instanceof List) {
                iterateBy = IterateBy.items;
            }
        }
    }

    public String getStringValue(DocxConfig config, IVariableProvider variableProvider, Object key) {
        if (iterateBy == IterateBy.indexes) {
            // modify original algorithm
            // return String.valueOf(key);
            return DocxDialogEnhancementMode.DETECT_STRING_CONST;
        }
        if (iterateBy == IterateBy.numbers) {
            // modify original algorithm
            // return String.valueOf(((Number) key).longValue() + 1);
            return DocxDialogEnhancementMode.DETECT_STRING_CONST;
        }
        if (iterateBy == IterateBy.items) {
            // modify original algorithm
            // int index = TypeConversionUtil.convertTo(Integer.class, key);
            // List<?> list = (List<?>) containerVariable.getValue();
            // Object listItem = list.size() > index ? list.get(index) : null;
            // if (containerSelector == null) {
            // return FormatCommons.formatComponentValue(containerVariable, 0, listItem);
            // } else {
            // return String.valueOf(DocxUtils.getValue(config, variableProvider, listItem, containerSelector));
            // }
            return DocxDialogEnhancementMode.DETECT_STRING_CONST;
        }
        if (iterateBy == IterateBy.keys) {
            // modify original algorithm
            // if (containerSelector == null) {
            // return FormatCommons.formatComponentValue(containerVariable, 0, key);
            // } else {
            // return String.valueOf(DocxUtils.getValue(config, variableProvider, key, containerSelector));
            // }
            return DocxDialogEnhancementMode.DETECT_STRING_CONST;
        }
        if (iterateBy == IterateBy.values) {
            // modify original algorithm
            // Object value = ((Map<?, ?>) containerVariable.getValue()).get(key);
            // if (containerSelector == null) {
            // return FormatCommons.formatComponentValue(containerVariable, 1, value);
            // } else {
            // return String.valueOf(DocxUtils.getValue(config, variableProvider, value, containerSelector));
            // }
            return DocxDialogEnhancementMode.DETECT_STRING_CONST;
        }
        return null;
    }

}
