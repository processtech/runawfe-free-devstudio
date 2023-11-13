package ru.runa.gpd.extension.decision;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

/**
 * Tests can be found in separate project in rm429.
 * 
 * Tests are separated from code because of infrastructure complications (https://rcpquickstart.wordpress.com/2007/06/20/unit-testing-plug-ins-with-fragments/)
 * 
 * @author dofs
 */
public class GroovyCodeParser {
    private static final String DEPRECATED_EQUALS = "equals";
    private static final String DEPRECATED_EQUALS_VALUE = ".equals(";
    private static final String DEPRECATED_BOOLEAN_VALUE = ".booleanValue()";
    private static final String DEPRECATED_DOUBLE_VALUE = ".doubleValue()";
    private static final String DEPRECATED_DATE_VALUE = "CalendarUtil.dateToCalendar";
    private static final String DEPRECATED_BIG_DECIMAL_VALUE = "new BigDecimal(";

    public static Optional<GroovyDecisionModel> parseDecisionModel(Decision decision) {
        return parseDecisionModel(decision.getDelegationConfiguration(), decision.getVariables(true, true));
    }
    
    public static Optional<GroovyDecisionModel> parseDecisionModel(String code, List<Variable> variables) {
        try {
            if (Strings.isNullOrEmpty(code)) {
                return Optional.empty();
            }
            GroovyDecisionModel model = new GroovyDecisionModel();
            AstBuilder astBuilder = new AstBuilder();
            List<ASTNode> astNodes = astBuilder.buildFromString(CompilePhase.CONVERSION, true, code);
            BlockStatement blockStatement = (BlockStatement) astNodes.get(0);
            for (Statement statement : blockStatement.getStatements()) {
                if (statement instanceof IfStatement) {
                    Expression expression = ((IfStatement) statement).getBooleanExpression().getExpression();
                    IfStatementParsedData parsedData = parseIfStatementExpression(expression);
                    ReturnStatement returnStatement = (ReturnStatement) ((BlockStatement) ((IfStatement) statement).getIfBlock()).getStatements().get(0);
                    String transitionName = (String) ((ConstantExpression) returnStatement.getExpression()).getValue();
                    Variable variable1 = VariableUtils.getVariableByScriptingName(variables, parsedData.leftText);
                    assertNotNull(variable1, parsedData.leftText);
                    Variable variable2 = VariableUtils.getVariableByScriptingName(variables, parsedData.rightText);
                    model.addIfExpression(
                            new GroovyDecisionModel.IfExpression(
                                    transitionName,
                                    variable1,
                                    variable2 != null ? variable2 : parsedData.rightText,
                                    Operation.getByOperator(parsedData.operationText, GroovyTypeSupport.get(variable1.getJavaClassName()))));
                }
                if (statement instanceof ReturnStatement) {
                    String transitionName = (String) ((ConstantExpression) ((ReturnStatement) statement).getExpression()).getValue();
                    model.addIfExpression(new GroovyDecisionModel.IfExpression(transitionName));
                }
            }
            return Optional.of(model);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("parseDecisionModel " + code, e);
            return Optional.empty();
        }
    }

    public static Optional<GroovyValidationModel> parseValidationModel(String code, List<Variable> variables) {
        try {
            if (Strings.isNullOrEmpty(code)) {
                return Optional.empty();
            }
            AstBuilder astBuilder = new AstBuilder();
            List<ASTNode> astNodes = astBuilder.buildFromString(CompilePhase.CONVERSION, true, code);
            Expression expression = ((ExpressionStatement) ((BlockStatement) astNodes.get(0)).getStatements().get(0)).getExpression();
            IfStatementParsedData parsedData = parseIfStatementExpression(expression);
            Variable variable1 = VariableUtils.getVariableByScriptingName(variables, parsedData.leftText);
            assertNotNull(variable1, parsedData.leftText);
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
            Operation operation = Operation.getByOperator(parsedData.operationText, typeSupport);
            Variable variable2 = VariableUtils.getVariableByScriptingName(variables, parsedData.rightText);
            assertNotNull(variable2, parsedData.rightText);
            return Optional.of(new GroovyValidationModel(variable1, variable2, operation));
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("parseValidationModel " + code, e);
            return Optional.empty();
        }
    }

    private static IfStatementParsedData parseIfStatementExpression(Expression expression) {
        if (containsDeprecatedInstructions(expression.getText())) {
            return parseIfStatementExpressionWithDeprecatedInstructions(expression);
        }
        IfStatementParsedData parsedData = new IfStatementParsedData();
        if (expression instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expression;
            parsedData.leftText = be.getLeftExpression().getText();
            parsedData.operationText = be.getOperation().getText();
            parsedData.rightText = be.getRightExpression().getText();
        } else /* contains */ {
            MethodCallExpression mce = (MethodCallExpression) expression;
            parsedData.leftText = mce.getObjectExpression().getText();
            parsedData.operationText = mce.getMethodAsString();
            parsedData.rightText = ((ArgumentListExpression) mce.getArguments()).getExpression(0).getText();
        }
        return parsedData;
    }
    
    private static boolean containsDeprecatedInstructions(String code) {
        return code.contains(DEPRECATED_EQUALS_VALUE) ||
                code.contains(DEPRECATED_BOOLEAN_VALUE) ||
                code.contains(DEPRECATED_DOUBLE_VALUE) ||
                code.contains(DEPRECATED_DATE_VALUE) ||
                code.contains(DEPRECATED_BIG_DECIMAL_VALUE);
    }

    private static IfStatementParsedData parseIfStatementExpressionWithDeprecatedInstructions(Expression expression) {
        GroovyCodeParser.IfStatementParsedData parsedData = new IfStatementParsedData();
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            String binaryExpressionText = binaryExpression.getText();
            if (binaryExpressionText.contains(DEPRECATED_BOOLEAN_VALUE) || binaryExpressionText.contains(DEPRECATED_DOUBLE_VALUE)) {
                MethodCallExpression mce = (MethodCallExpression) binaryExpression.getLeftExpression();
                parsedData.leftText = mce.getObjectExpression().getText();
                parsedData.operationText = binaryExpression.getOperation().getText();
                if (binaryExpression.getRightExpression() instanceof MethodCallExpression) {
                    parsedData.rightText = ((MethodCallExpression) binaryExpression.getRightExpression()).getObjectExpression().getText();
                } else {
                    parsedData.rightText = binaryExpression.getRightExpression().getText();
                }
            } else if (binaryExpressionText.contains(DEPRECATED_DATE_VALUE)) {
                BinaryExpression dateBinaryExpression = ((BinaryExpression) binaryExpression.getRightExpression());
                MethodCallExpression mce = ((MethodCallExpression) dateBinaryExpression.getLeftExpression());
                MethodCallExpression mceLeft = (MethodCallExpression) mce.getObjectExpression();
                Expression innerLeft = ((ArgumentListExpression) mceLeft.getArguments()).getExpression(0);
                if (innerLeft instanceof VariableExpression) {
                    parsedData.leftText = ((VariableExpression) innerLeft).getName();
                } else {
                    MethodCallExpression mceLeft1 = (MethodCallExpression) innerLeft;
                    parsedData.leftText = ((ArgumentListExpression) mceLeft1.getArguments()).getExpression(0).getText();
                }
                parsedData.operationText = dateBinaryExpression.getOperation().getText();
                MethodCallExpression mceRight = (MethodCallExpression) ((ArgumentListExpression) mce.getArguments()).getExpression(0);
                Expression innerRight = ((ArgumentListExpression) mceRight.getArguments()).getExpression(0);
                if (innerRight instanceof VariableExpression) {
                    parsedData.rightText = ((VariableExpression) innerRight).getName();
                } else {
                    MethodCallExpression mceRight1 = (MethodCallExpression) innerRight;
                    parsedData.rightText = ((ArgumentListExpression) mceRight1.getArguments()).getExpression(0).getText();
                }
            } else if (binaryExpressionText.contains(DEPRECATED_BIG_DECIMAL_VALUE)) {
                BinaryExpression bdBinaryExpression = ((BinaryExpression) binaryExpression.getRightExpression());
                MethodCallExpression mce = (MethodCallExpression) bdBinaryExpression.getLeftExpression();
                ConstructorCallExpression cceLeft = ((ConstructorCallExpression) mce.getObjectExpression());
                parsedData.leftText = ((ArgumentListExpression) cceLeft.getArguments()).getExpression(0).getText();
                parsedData.operationText = bdBinaryExpression.getOperation().getText();
                ConstructorCallExpression cceRight = ((ConstructorCallExpression) ((ArgumentListExpression) mce.getArguments()).getExpression(0));
                parsedData.rightText = ((ArgumentListExpression) cceRight.getArguments()).getExpression(0).getText();
            } else {
                parsedData.leftText = ((VariableExpression) binaryExpression.getLeftExpression()).getName();
                parsedData.operationText = binaryExpression.getOperation().getText();
                parsedData.rightText = binaryExpression.getRightExpression().getText();
            }
        } else if (expression instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) expression;
            parsedData.leftText = methodCallExpression.getObjectExpression().getText();
            parsedData.operationText = methodCallExpression.getMethodAsString();
            if (DEPRECATED_EQUALS.equals(parsedData.operationText)) {
                parsedData.operationText = Operation.EQ.getOperator();
            }
            parsedData.rightText = ((ArgumentListExpression) methodCallExpression.getArguments()).getExpression(0).getText();
        } else if (expression instanceof NotExpression) {
            // "!equals" only
            MethodCallExpression methodCallExpression = (MethodCallExpression) ((NotExpression) expression).getExpression();
            parsedData.leftText = methodCallExpression.getObjectExpression().getText();
            if (!DEPRECATED_EQUALS.equals(methodCallExpression.getMethodAsString())) {
                throw new RuntimeException("Unexpected NotExpression " + methodCallExpression);
            }
            parsedData.operationText = Operation.NOT_EQ.getOperator();
            parsedData.rightText = ((ArgumentListExpression) methodCallExpression.getArguments()).getExpression(0).getText();
        } else {
            throw new RuntimeException("IfStatement body is " + expression);
        }
        return parsedData;
    }

    private static void assertNotNull(Object o, String message) {
        if (o == null) {
            throw new RuntimeException(message);
        }
    }

    private static final class IfStatementParsedData {
        String leftText;
        String operationText;
        String rightText;
    }

}
