package ru.runa.gpd.extension;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import ru.runa.gpd.Localization;

public class SearchTextDialog extends Dialog {
    private StyledText styledText;
    private StyledText keywordText;
    private int currentOccurrenceIndex = -1;

    public SearchTextDialog(Shell parentShell, StyledText styledText) {
        super(parentShell);
        this.styledText = styledText;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("SearchTextDialog.title"));
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
        setBlockOnOpen(false);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        keywordText = new StyledText(area, SWT.SINGLE | SWT.BORDER);
        keywordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Localization.getString("SearchTextDialog.button.find"), true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            handleFind(styledText);
        } else if (buttonId == IDialogConstants.CANCEL_ID) {
            setReturnCode(CANCEL);
            close();
        }
    }

    private void handleFind(StyledText editor) {
        String keyword = keywordText.getText();
        currentOccurrenceIndex = findNextOccurrence(editor.getText(), keyword, currentOccurrenceIndex);
        if (currentOccurrenceIndex != -1) {
            int keywordLength = keyword.length();
            editor.setSelection(currentOccurrenceIndex, currentOccurrenceIndex + keywordLength);
            editor.setSelectionBackground(editor.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
            editor.setSelectionForeground(editor.getDisplay().getSystemColor(SWT.COLOR_BLACK));
            currentOccurrenceIndex = currentOccurrenceIndex + keywordLength;
            editor.showSelection();
            editor.redraw();
        }
    }

    private int findNextOccurrence(String text, String keyword, int currentIndex) {
        int index = text.toLowerCase().indexOf(keyword.toLowerCase(), currentIndex + 1);
        if (index != -1) {
            return index;
        }
        // Search from begin when text end reached
        return text.indexOf(keyword);
    }

}
