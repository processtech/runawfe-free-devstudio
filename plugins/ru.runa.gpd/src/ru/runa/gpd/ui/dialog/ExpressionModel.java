package ru.runa.gpd.ui.dialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ru.runa.gpd.ui.control.ExpressionLine;

public class ExpressionModel {
    // модель выражения такого типа, который задается в этом диалоге.
    // класс используется только для передачи данных о модели при загрузке из сохранения
    private Map<String, String> params = null;
    private static final String paramPrefix = "ComplexExpressionConstructorDialog";
    private static final String isSavedParam = paramPrefix + "_saved";
    private static final String expressionLineNumberParam = paramPrefix + "_ExpressionLinesNumber";

    public ExpressionModel() {
        this.params = new HashMap<>();
        this.params.put(isSavedParam, "true");
        this.params.put(expressionLineNumberParam, String.valueOf(0));
    }

    // возвращает представление модели в виде Map из параметров
    public Map<String, String> getParamsMap() {
        return params;
    }

    public int getExpressionLineNumber() {
        return Integer.parseInt(this.params.get(expressionLineNumberParam));
    }

    public void addExpressionLineModel(ExpressionLine.ExpressionLineModel model) {
        String expressionLinesNumberString = this.params.get(expressionLineNumberParam);
        int expressionLinesNumber = Integer.parseInt(expressionLinesNumberString);
        for (Entry<String, String> entry : model.getParamsMap().entrySet()) {
            this.params.put(paramPrefix + "_" + expressionLinesNumberString + "_" + entry.getKey(), entry.getValue());
        }
        this.params.put(expressionLineNumberParam, String.valueOf(expressionLinesNumber + 1));
    }

    public ExpressionLine.ExpressionLineModel getExpressionLineModel(int index) {
        ExpressionLine.ExpressionLineModel model = new ExpressionLine.ExpressionLineModel();
        for (String param : ExpressionLine.getParamsNames()) {
            String key = paramPrefix + "_" + index + "_" + param;
            model.getParamsMap().put(param, this.params.get(key));
        }
        return model;
    }
}
