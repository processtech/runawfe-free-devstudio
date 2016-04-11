package ru.runa.gpd.editor.gef.part.tree;

import ru.runa.gpd.lang.model.Variable;

public class VariableTreeEditPart extends ElementTreeEditPart {

    public VariableTreeEditPart(Variable variable) {
        setModel(variable);
    }

    @Override
    public Variable getModel() {
        return (Variable) super.getModel();
    }

}
