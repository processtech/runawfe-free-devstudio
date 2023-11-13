package ru.runa.gpd.ui.control;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

// класс является аналогом класса LogicComposite, использующимся в бизнес-правилах. В этот раз
// в нем не делается вся работа со скобками, она делается в родительских виджетах 
// (родитель, его родитель и т.д.). Тут, как в FilterBox, для обьектов класса можно установить
// слушателей, которые будут слушать нажатия на кнопки в этом виджете

//виджет, в котором кнопки-скобки и лог.операция
public class CloseBracketButtonAndLogicOperationComboAndOpenBracketButton extends Composite {

    public static final String AND_LOGIC_EXPRESSION = "and"; // логические операции для выбора в combo box
    public static final String OR_LOGIC_EXPRESSION = "or";

    private Button closeButton; // кнопка с закрывающей скобкой
    private Button openButton; // кнопка с открывающей скобкой
    private Combo logicBox; // Combo для выбора логической операции

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public CloseBracketButtonAndLogicOperationComboAndOpenBracketButton(Composite parent, int style) {
        super(parent, style);

        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        this.setLayout(layout);

        closeButton = new Button(this, SWT.TOGGLE); // это не просто кнопка, а переключатель, поэтому SWT.TOGGLE
        closeButton.setText("]");

        logicBox = new Combo(this, SWT.NONE | SWT.READ_ONLY);
        logicBox.setItems(AND_LOGIC_EXPRESSION, OR_LOGIC_EXPRESSION);
        logicBox.select(0);

        openButton = new Button(this, SWT.TOGGLE);
        openButton.setText("[");
    }

    public void addCloseButtonSelectionListener(SelectionListener listener) {
        closeButton.addSelectionListener(listener);
    }

    public void addOpenButtonSelectionListener(SelectionListener listener) {
        openButton.addSelectionListener(listener);
    }

    public void addComboBoxSelectionListener(SelectionListener listener) {
        logicBox.addSelectionListener(listener);
    }

    public boolean isOpenButtonSelected() {
        return openButton.getSelection();
    }

    public boolean isCloseButtonSelected() {
        return closeButton.getSelection();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public void setOpenButtonSelection(boolean selected) {
        openButton.setSelection(selected);
    }

    public void setCloseButtonSelection(boolean selected) {
        closeButton.setSelection(selected);
    }

    public Button getOpenButton() {
        return this.openButton;
    }

    public Combo getCombo() {
        return this.logicBox;
    }

    public void replaceLastPart(Combo logicBox, Button openButton) { // вызывается над вынесенным виджетом
        this.logicBox.dispose();
        this.openButton.dispose();
        this.logicBox = logicBox;
        this.openButton = openButton;
        logicBox.setParent(this);
        openButton.setParent(this);
    }

    public void returnLastPart() {
        this.logicBox.setParent(this);
        this.openButton.setParent(this);
    }

    private Button openButtonForSpace;
    private Combo logicBoxForSpace;

    public void addLastPartSpace() {
        logicBoxForSpace = new Combo(this, SWT.NONE | SWT.READ_ONLY);
        logicBoxForSpace.setItems(AND_LOGIC_EXPRESSION, OR_LOGIC_EXPRESSION);
        logicBoxForSpace.select(0);
        logicBoxForSpace.setVisible(false);
        openButtonForSpace = new Button(this, SWT.TOGGLE);
        openButtonForSpace.setText("[");
        openButtonForSpace.setVisible(false);
    }

    public void removeLastPartSpace() {
        this.logicBoxForSpace.dispose();
        this.openButtonForSpace.dispose();
    }

    public void setCloseButtonVisible(boolean visible) {
        this.closeButton.setVisible(visible);
    }
}
