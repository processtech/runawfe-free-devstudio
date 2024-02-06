package ru.runa.gpd.extension.businessRule;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class LogicComposite extends Composite {
    public static final int MARGIN_LEFT_STEP = 20;
    public static final int MARGIN_TOP_STEP = 10;
    public static final int MARGIN_BOTTOM_STEP = 10;

    public static final String AND_LOGIC_EXPRESSION = "and";
    public static final String OR_LOGIC_EXPRESSION = "or";
    public static final String NULL_LOGIC_EXPRESSION = "null";

    private List<LogicComposite> logicComposites;
    private Composite expressionsComposite;

    private Button closeButton;
    private Button openButton;
    private Combo logicBox;
    private int[] brackets;

    public LogicComposite(Composite parent, List<LogicComposite> logicComposites) {
        super(parent, SWT.NONE);
        brackets = new int[2];
        this.logicComposites = logicComposites;
        expressionsComposite = parent.getParent();

        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 100;
        setLayoutData(data);

        closeButton = new Button(this, SWT.TOGGLE);
        closeButton.setText("]");
        closeButton.addSelectionListener(new CloseSelectionHandler());

        logicBox = new Combo(this, SWT.READ_ONLY);
        logicBox.setItems(AND_LOGIC_EXPRESSION, OR_LOGIC_EXPRESSION);
        logicBox.select(0);

        openButton = new Button(this, SWT.TOGGLE);
        openButton.setText("[");
        openButton.addSelectionListener(new OpenSelectionHandler());
    }

    private class CloseSelectionHandler extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            int currentLineIndex = logicComposites.indexOf(((Button) e.getSource()).getParent());
            if (((Button) e.getSource()).getSelection()) {
                int targetLineIndex = 0;
                int lastLineIndex = logicComposites.size() - 1;
                logicComposites.get(currentLineIndex).getBrackets()[1]++;
                logicComposites.get(targetLineIndex).getBrackets()[0]++;

                if (findOpenTarget(lastLineIndex) == 0) {
                    undoPressCloseButton(lastLineIndex);
                }

                updateVerticalMargin(currentLineIndex);
                updateVerticalMargin(targetLineIndex);
                updateHorizontalMargin();
            } else {
                undoPressCloseButton(currentLineIndex);
            }
        }
    }

    private int findOpenTarget(int currentLineIndex) {
        int targetLineIndex = 0;
        int bracketsCount = 0;
        for (int i = currentLineIndex; i > 0; i--) {
            bracketsCount += logicComposites.get(i).getBrackets()[1] - logicComposites.get(i).getBrackets()[0];
            if (bracketsCount <= 0) {
                return i;
            }
        }
        return targetLineIndex;
    }

    public void undoPressCloseButton(int currentLineIndex) {
        int targetLineIndex = findOpenTarget(currentLineIndex);
        int lastLineIndex = logicComposites.size() - 1;

        if (targetLineIndex != 0) {
            logicComposites.get(currentLineIndex).getBrackets()[1]--;
            logicComposites.get(lastLineIndex).getBrackets()[1]++;
            updateVerticalMargin(currentLineIndex);
            updateVerticalMargin(lastLineIndex);
        } else {
            logicComposites.get(currentLineIndex).getBrackets()[1]--;
            logicComposites.get(targetLineIndex).getBrackets()[0]--;
            updateVerticalMargin(currentLineIndex);
            updateVerticalMargin(targetLineIndex);
        }
        if (findOpenTarget(lastLineIndex) == 0) {
            undoPressCloseButton(lastLineIndex);
        }
        updateHorizontalMargin();
    }

    private class OpenSelectionHandler extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            int currentLineIndex = logicComposites.indexOf(((Button) e.getSource()).getParent());
            if (((Button) e.getSource()).getSelection()) {
                int targetLineIndex = logicComposites.size() - 1;
                logicComposites.get(currentLineIndex + 1).getBrackets()[0]++;
                logicComposites.get(targetLineIndex).getBrackets()[1]++;

                if (findOpenTarget(targetLineIndex) == 0) {
                    undoPressCloseButton(targetLineIndex);
                }

                updateVerticalMargin(currentLineIndex + 1);
                updateVerticalMargin(targetLineIndex);
                updateHorizontalMargin();
            } else {
                undoPressOpenButton(currentLineIndex);
            }
        }
    }

    private int findCloseTarget(int currentLineIndex) {
        int targetLineIndex = logicComposites.size() - 1;
        int bracketsCount = 0;
        for (int i = currentLineIndex + 1; i < logicComposites.size(); i++) {
            bracketsCount += logicComposites.get(i).getBrackets()[0] - logicComposites.get(i).getBrackets()[1];
            if (bracketsCount <= 0) {
                return i;
            }
        }
        return targetLineIndex;
    }

    public void undoPressOpenButton(int currentLineIndex) {
        int targetLineIndex = findCloseTarget(currentLineIndex);
        int lastLineIndex = logicComposites.size() - 1;

        if (targetLineIndex != logicComposites.size() - 1) {
            logicComposites.get(currentLineIndex + 1).getBrackets()[0]--;
            logicComposites.get(0).getBrackets()[0]++;
            updateVerticalMargin(currentLineIndex + 1);
            updateVerticalMargin(0);
        } else {
            logicComposites.get(currentLineIndex + 1).getBrackets()[0]--;
            logicComposites.get(targetLineIndex).getBrackets()[1]--;
            updateVerticalMargin(currentLineIndex + 1);
            updateVerticalMargin(targetLineIndex);
        }
        if (findOpenTarget(lastLineIndex) == 0) {
            undoPressCloseButton(lastLineIndex);
        }
        updateHorizontalMargin();
    }

    public void updateBeforeDeletion() {
        int logicCompositeIndex = logicComposites.indexOf(this);

        if (getBrackets()[0] > 0 && getBrackets()[1] > 0) {
            getBrackets()[0]--;
            getBrackets()[1]--;
        }
        if (getBrackets()[0] != 0 && logicCompositeIndex != logicComposites.size() - 1) {
            logicComposites.get(logicCompositeIndex + 1).getBrackets()[0] += logicComposites.get(logicCompositeIndex).getBrackets()[0];
        }
        if (getBrackets()[1] != 0 && logicCompositeIndex != 0) {
            logicComposites.get(logicCompositeIndex - 1).getBrackets()[1] += logicComposites.get(logicCompositeIndex).getBrackets()[1];
        }
        if (logicCompositeIndex == logicComposites.size() - 1) {
            logicComposites.get(logicCompositeIndex - 1).setVisible(false);
            if (findOpenTarget(logicCompositeIndex - 1) == 0) {
                undoPressCloseButton(logicCompositeIndex - 1);
            }
        }
    }

    public void updateAfterDeletion() {
        for (int i = logicComposites.size() - 1; i >= 0; i--) {
            int openTarget = findOpenTarget(i);
            if (logicComposites.get(openTarget).getBrackets()[0] > 1 && logicComposites.get(i).getBrackets()[1] > 1) {
                logicComposites.get(openTarget).getBrackets()[0]--;
                logicComposites.get(i).getBrackets()[1]--;
            }
            updateVerticalMargin(i);
        }
        updateHorizontalMargin();
    }

    public void updateVerticalMargin(int lineIndex) {
        Composite expression = logicComposites.get(lineIndex).getParent();
        LogicComposite logiComposite = logicComposites.get(lineIndex);

        ((GridLayout) expression.getLayout()).marginTop = logiComposite.getBrackets()[0] * MARGIN_TOP_STEP;
        ((GridLayout) expression.getLayout()).verticalSpacing = logiComposite.getBrackets()[1] * MARGIN_BOTTOM_STEP;
        if (logiComposite.getBrackets()[1] == 0) {
            logiComposite.getCloseButton().setSelection(false);
            ((GridLayout) expression.getLayout()).marginBottom = ((GridLayout) expression.getLayout()).verticalSpacing;
            GridData logicCompositeData = (GridData) logiComposite.getLayoutData();
            logicCompositeData.horizontalSpan = 1;
            logicCompositeData.horizontalAlignment = SWT.BEGINNING;
        } else {
            logiComposite.getCloseButton().setSelection(true);
            ((GridLayout) expression.getLayout()).marginBottom = 0;
            GridData logicCompositeData = (GridData) logiComposite.getLayoutData();
            logicCompositeData.horizontalSpan = 4;
            logicCompositeData.horizontalAlignment = SWT.END;
        }
        if (logiComposite.getBrackets()[0] != 0 && logicComposites.indexOf(logiComposite) > 0) {
            logicComposites.get(logicComposites.indexOf(logiComposite) - 1).getOpenButton().setSelection(true);
        }
        if (logiComposite.getBrackets()[0] == 0 && logicComposites.indexOf(logiComposite) > 0) {
            logicComposites.get(logicComposites.indexOf(logiComposite) - 1).getOpenButton().setSelection(false);
        }
    }

    public void updateHorizontalMargin() {
        ((GridLayout) logicComposites.get(0).getParent().getLayout()).marginLeft = logicComposites.get(0).getBrackets()[0] * MARGIN_LEFT_STEP;
        logicComposites.get(0).getParent().requestLayout();
        for (int i = 1; i < logicComposites.size(); i++) {
            ((GridLayout) logicComposites.get(i).getParent()
                    .getLayout()).marginLeft = (((GridLayout) logicComposites.get(i - 1).getParent().getLayout()).marginLeft / MARGIN_LEFT_STEP
                            + logicComposites.get(i).getBrackets()[0] - logicComposites.get(i - 1).getBrackets()[1]) * MARGIN_LEFT_STEP;
            logicComposites.get(i).getParent().requestLayout();
        }
        ((ScrolledComposite) expressionsComposite.getParent()).setMinSize(expressionsComposite.computeSize(SWT.MIN, SWT.DEFAULT));
        expressionsComposite.redraw();
    }

    public Button getOpenButton() {
        return openButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public Combo getLogicBox() {
        return logicBox;
    }

    public int[] getBrackets() {
        return brackets;
    }

    public void setBrackets(int[] brackets) {
        this.brackets = brackets;
    }
}

