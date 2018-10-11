package ru.runa.gpd.ui.dialog;

import java.util.List;
import org.eclipse.jface.viewers.LabelProvider;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MessageNode;

public class ChooseMessageNodeDialog extends ChooseItemDialog<MessageNode> {

    public ChooseMessageNodeDialog(List<MessageNode> messageNodes) {
        super(Localization.getString("ChooseMessageNode.title"), messageNodes, true, Localization.getString("ChooseMessageNode.message"), true);
        setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((MessageNode) element).getName();
            }
        });
    }

}
