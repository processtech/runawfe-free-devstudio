package ru.runa.gpd.editors;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

public class DiffEditorPropertiesReconciler extends PresentationReconciler {

    private ColorManager colorManager;

    public DiffEditorPropertiesReconciler(ColorManager colorManager) {
        this.colorManager = colorManager;
        RuleBasedScanner scanner = new RuleBasedScanner();
        List<IRule> rules = new ArrayList<>();
        rules.add(createBoldTextRule("---", DiffEditorContentColorConstants.DEFAULT_TEXT));
        rules.add(createBoldTextRule("+++", DiffEditorContentColorConstants.DEFAULT_TEXT));
        rules.add(createRule("@", DiffEditorContentColorConstants.DEFAULT_TEXT));
        rules.add(createRuleWithColumnConstrain("+", DiffEditorContentColorConstants.darkcyan));
        rules.add(createRuleWithColumnConstrain("-", DiffEditorContentColorConstants.brown));
        rules.add(createRuleWithColumnConstrain("\\\\", DiffEditorContentColorConstants.lightgray));
        scanner.setRules(rules.toArray(new IRule[0]));

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
        this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
    }

    private EndOfLineRule createRuleWithColumnConstrain(String startSequence, RGB color) {
        EndOfLineRule rule4 = new EndOfLineRule(startSequence, new Token(new TextAttribute(colorManager.getColor(color))));
        rule4.setColumnConstraint(0);
        return rule4;
    }

    private EndOfLineRule createRule(String startSequence, RGB color) {
        return new EndOfLineRule(startSequence, new Token(new TextAttribute(colorManager.getColor(color))));
    }
    
    private EndOfLineRule createBoldTextRule(String startSequence, RGB color) {
        return new EndOfLineRule(startSequence, new Token(new TextAttribute(colorManager.getColor(color), colorManager.getColor(DiffEditorContentColorConstants.DEFAULT_BACKGROUND), SWT.BOLD)));
    }
}
