package ru.runa.gpd.extension.decision;

import com.google.common.base.Strings;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
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
import ru.runa.gpd.ui.control.ExpressionLine;
import ru.runa.gpd.ui.dialog.GlobalValidatorExpressionConstructorDialog;
import ru.runa.gpd.util.VariableUtils;

/**
 * Tests can be found in separate project in rm429.
 * 
 * Tests are separated from code because of infrastructure complications
 * https://rcpquickstart.wordpress.com/2007/06/20/unit-testing-plug-ins-with-fragments/
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
                    ReturnStatement returnStatement = (ReturnStatement) ((BlockStatement) ((IfStatement) statement).getIfBlock()).getStatements()
                            .get(0);
                    String transitionName = (String) ((ConstantExpression) returnStatement.getExpression()).getValue();
                    Variable variable1 = VariableUtils.getVariableByScriptingName(variables, parsedData.leftText);
                    assertNotNull(variable1, parsedData.leftText);
                    Variable variable2 = VariableUtils.getVariableByScriptingName(variables, parsedData.rightText);
                    model.addIfExpression(
                            new GroovyDecisionModel.IfExpression(transitionName, variable1, variable2 != null ? variable2 : parsedData.rightText,
                                    Operation.getByOperator(parsedData.operationText, GroovyTypeSupport.get(variable1.getJavaClassName()))));
                }
                if (statement instanceof ReturnStatement) {
                    String transitionName = (String) ((ConstantExpression) ((ReturnStatement) statement).getExpression()).getValue();
                    model.addIfExpression(new GroovyDecisionModel.IfExpression(transitionName));
                }
            }
            return Optional.of(model);
        } catch (Exception e) {
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
        return code.contains(DEPRECATED_EQUALS_VALUE) || code.contains(DEPRECATED_BOOLEAN_VALUE) || code.contains(DEPRECATED_DOUBLE_VALUE)
                || code.contains(DEPRECATED_DATE_VALUE) || code.contains(DEPRECATED_BIG_DECIMAL_VALUE);
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

    /**
     * @author andrey belozerov
     */
    public static Optional<GlobalValidatorExpressionConstructorDialog.ExpressionModel> parseValidationModel(String code, List<Variable> variables) {
        try {
            if (Strings.isNullOrEmpty(code)) {
                return Optional.empty();
            }
            AstBuilder astBuilder = new AstBuilder();
            List<ASTNode> astNodes = astBuilder.buildFromString(CompilePhase.CONVERSION, true, code); // возвращает один узел типа BlockStatement
            BlockStatement blockStatement = (BlockStatement) astNodes.get(0); // состоит из одного ExpressionStatement
            ExpressionStatement expressionStatement = (ExpressionStatement) blockStatement.getStatements().get(0); // состоит из одного Expression
            Expression expression = expressionStatement.getExpression(); // выражение - двоичное дерево, каждый узел которого -
            // BinaryExpression, форма зависит от расстановки скобок
            // нужно разложить это дерево в список простых выражений, каждое из которых соответствует ExpressionLine в GUI и либо не BinaryExpression,
            // либо BinaryExpression, но не имеет логическую операцию && или ||
            List<Expression> expressionList = new LinkedList<>();
            expressionList.add(expression); // положим корень в список, и будем многократно заменять каждый элемент списка его потомками,
            // которые встают на место исходного элемента.
            // Проходить по листу итератором и одновременно модифицировать нельзя. Варианты: по индексу - get долго, использовать ArrayList -
            // долгая вставка, поэтому используется другой вариант - каждый раз создается новый список
            // В этой же процедуре для каждого узла определяется, какая логическая операция стоит справа от него в выражении, такая информация
            // нужна для заполнения Combo логической операции в ExpressionLine
            // Чтобы хранить эту информацию, она привязывается к узлам в их метаданных.
            // в этой же процедуре определяется количество скобок рядом с каждым выражением -
            // открывающие скобки слева от выражения и закрывающие справа от него.
            // Открывающая скобка родителя идет в левый потомок, закрывающая скобка - в правый потомок.
            String openBracketsNumber = "openBracketsNumber"; // количество открывающих скобок слева от выражения
            String closeBracketsNumber = "closeBracketsNumber"; // количество закрывающих скобок справа от выражения
            String operationOnTheRight = "operationOnTheRight";
            String operationInParent = "operationInParent";
            expression.putNodeMetaData(openBracketsNumber, 0);
            expression.putNodeMetaData(closeBracketsNumber, 0);
            expression.putNodeMetaData(operationInParent, ""); // операция в родителе узла,
            // пустой строкой обозначим отсутствие операции (для алгоритма ниже удобней
            // обозначить это таким значением, а не null, иначе придется делать проверку на null)
            while (true) {
                boolean listChangedInCycle = false;
                List<Expression> newExpressionList = new LinkedList<>();
                for (Expression i : expressionList) { // ищем элемент, который можно заменить
                    if (i instanceof BinaryExpression) {
                        BinaryExpression binaryExpression = (BinaryExpression) i;
                        String operation = binaryExpression.getOperation().getText();
                        if (operation.equals("&&") || operation.equals("||")) {
                            Expression leftExpression = binaryExpression.getLeftExpression();
                            Expression rightExpression = binaryExpression.getRightExpression();
                            newExpressionList.add(leftExpression);
                            newExpressionList.add(rightExpression);
                            listChangedInCycle = true;
                            // далее помещение информации о скобках
                            // помещение скобок вокруг этого BinaryExpression не нужно, когда
                            // операция - и, так как из всех используемых операций она имеет
                            // наибольший приоритет. Если операция или, то:
                            // если в родительском BinaryExpression операция - или, то скобки
                            // не нужны, потому что a or (b or c) = a or b or c
                            // если в родительском BinaryExpression операция - и, то скобки
                            // нужны, потому что родительское подвыражение тогда имеет вид a and (b or c),
                            // для такого выражения скобки необходимы.
                            leftExpression.putNodeMetaData(closeBracketsNumber, 0);
                            rightExpression.putNodeMetaData(openBracketsNumber, 0);
                            boolean addBrackets = operation.equals("||") && binaryExpression.getNodeMetaData(operationInParent).equals("&&");
                            int newBracketsNumber = addBrackets ? 1 : 0;
                            leftExpression.putNodeMetaData(openBracketsNumber,
                                    newBracketsNumber + (Integer) binaryExpression.getNodeMetaData(openBracketsNumber));
                            rightExpression.putNodeMetaData(closeBracketsNumber,
                                    newBracketsNumber + (Integer) binaryExpression.getNodeMetaData(closeBracketsNumber));
                            leftExpression.putNodeMetaData(operationInParent, operation);
                            rightExpression.putNodeMetaData(operationInParent, operation);
                            // далее помещение информации о логической операции
                            leftExpression.putNodeMetaData(operationOnTheRight, operation);
                            rightExpression.putNodeMetaData(operationOnTheRight, binaryExpression.getNodeMetaData(operationOnTheRight));
                        } else {
                            newExpressionList.add(i);
                        }
                    } else {
                        newExpressionList.add(i);
                    }
                }
                if (!listChangedInCycle) {
                    break;
                }
                expressionList = newExpressionList;
            }
            GlobalValidatorExpressionConstructorDialog.ExpressionModel model = new GlobalValidatorExpressionConstructorDialog.ExpressionModel();
            ListIterator<Expression> iterator = expressionList.listIterator(); // нужен, чтобы в LinkedList смотреть на следующий элемент за текущим
            while (iterator.hasNext()) {
                Expression currentExpression = iterator.next();
                ExpressionLine.ExpressionLineModel expressionLineModel = new ExpressionLine.ExpressionLineModel();
                Object logicOperation = currentExpression.getNodeMetaData(operationOnTheRight);
                if (iterator.hasNext()) {
                    expressionLineModel.setLogicOperationGroovy((String) logicOperation);
                } else {
                    // задание логической операции по умолчанию в последнем ExpressionLine, там это значение
                    // не видно и не используется, пока ExpressionLine остается последней.
                    expressionLineModel.setLogicOperationGroovy("&&");
                }
                int openBracketsAfterExpression;
                if (iterator.hasNext()) {
                    Expression nextExpression = iterator.next(); // продвинулись вперед на 1
                    openBracketsAfterExpression = (Integer) nextExpression.getNodeMetaData(openBracketsNumber);
                    iterator.previous(); // возвращаемся обратно
                } else {
                    openBracketsAfterExpression = 0;
                }
                if (openBracketsAfterExpression == 0) {
                    expressionLineModel.setOpenBracketExist(false);
                } else if (openBracketsAfterExpression == 1) {
                    expressionLineModel.setOpenBracketExist(true);
                } else { // такое невозможно, исходное выражение неправильное
                    throw new Exception(openBracketsAfterExpression + " open brackets after expression " + iterator.previous() + " , must be 0 or 1");
                }
                int closeBracketsAfterExpression = (Integer) currentExpression.getNodeMetaData(closeBracketsNumber);
                if (closeBracketsAfterExpression == 0 || iterator.nextIndex() == expressionList.size()) {
                    // на последней ExpressionLine не используются кнопки-скобки
                    expressionLineModel.setCloseBracketExist(false);
                } else if (closeBracketsAfterExpression == 1) {
                    expressionLineModel.setCloseBracketExist(true);
                } else if (iterator.nextIndex() != expressionList.size()) {
                    throw new Exception(closeBracketsAfterExpression + " close brackets after expression " + iterator.previous()
                            + " , must be 0 or 1, these brackets are not in the end of expression");
                    // такое невозможно, исходное выражение неправильное, если это не конец выражения.
                    // в конце может быть любое количество закрывающих скобок
                    // и для конца не надо вызывать setCloseBracketExist, так как в GUI последняя
                    // ExpressionLine не имеет виджета с кнопками-скобками
                }
                parseSimpleExpression(currentExpression, expressionLineModel);
                model.addExpressionLineModel(expressionLineModel);
            }
            return Optional.of(model);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("parseValidationModel " + code, e);
            return Optional.empty();
        }
    }

    /**
     * @author andrey belozerov
     */
    private static void parseSimpleExpression(Expression expression, ExpressionLine.ExpressionLineModel model) throws Exception {
        Expression leftExpression = null;
        Expression rightExpression = null;
        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            leftExpression = binaryExpression.getLeftExpression();
            model.setOperation(binaryExpression.getOperation().getText());
            rightExpression = binaryExpression.getRightExpression();
        } else if (expression instanceof MethodCallExpression) {
            MethodCallExpression methodCallExpression = (MethodCallExpression) expression;
            leftExpression = methodCallExpression.getObjectExpression();
            model.setOperation(methodCallExpression.getMethodAsString());
            ArgumentListExpression methodArguments = (ArgumentListExpression) methodCallExpression.getArguments();
            if (methodArguments.getExpressions().size() == 1) {
                rightExpression = methodArguments.getExpression(0);
            } else {
                throw new Exception(methodArguments.getExpressions().size() + " arguments in method call, must be 1");
            }
        } else {
            throw new Exception("simple expression is not binary and not method call, expression:\n" + expression);
        }
        if (leftExpression instanceof VariableExpression || hasUserTypeSyntax(leftExpression)) {
            model.setFirstOperand(leftExpression.getText());
        } else {
            throw new Exception("left part of sub-expression " + expression + "\n is not variable, this part is:\n" + leftExpression);
        }
        model.setSecondOperandIsStringValue(false);
        if (rightExpression instanceof VariableExpression || hasUserTypeSyntax(rightExpression)) {
            model.setSecondOperandTypeVariable();
        } else if (rightExpression instanceof ConstantExpression) {
            ConstantExpression constantExpression = (ConstantExpression) rightExpression;
            ClassNode rightExpressionType = constantExpression.getType();
            Class<?> rightExpressionClass = rightExpressionType.getTypeClass();
            if (rightExpressionClass.equals(String.class)) {
                // путаница между типами значения value и predefinedValue возникает только для строк, этот случай рассматривается
                model.setSecondOperandTypeValue(); // предопределенное значение только null, оно не String
                model.setSecondOperandIsStringValue(true); // ASTNode.getText, который используется, возвращает одинаковое значение
                // для, например, "77" и 77 - строка с текстом 77. Чтобы различать это для модели, решено ввести в модель дополнительный параметр.
                // Другой вариант - вариант 2 - помещать в параметр значения второго операнда кавычки, когда это строка -
                // этот вариант не выбран, так как другой вариант проще.
            }
            model.setSecondOperandTypeValueOrPredefinedValue();
        } else {
            throw new Exception(
                    "right part of sub-expression " + expression + "\n is not variable and not constant, this part is:\n" + rightExpression);
        }
        model.setSecondOperand(rightExpression.getText());
    }

    /**
     * @author andrey belozerov
     */
    private static boolean hasUserTypeSyntax(Expression expression) {
        // в названии метода "syntax", так как тут только проверяется синтаксис,
        // а наличие такого типа и переменной проверяется не в GroovyCodeParser
        if (!(expression instanceof PropertyExpression)) {
            return false;
        }
        PropertyExpression propertyExpression = (PropertyExpression) expression;
        Expression objectExpression = propertyExpression.getObjectExpression();
        Expression property = propertyExpression.getProperty();
        // синтаксис: name1.name2.name3.... , где name1 есть VariableExpression, а остальные - ConstantExpression
        boolean correctObjectExpression = (objectExpression instanceof VariableExpression) || hasUserTypeSyntax(objectExpression);
        boolean correctProperty = isDotSeparatedConstantExpressions(property);
        return correctObjectExpression && correctProperty;
    }

    /**
     * @author andrey belozerov
     */
    private static boolean isDotSeparatedConstantExpressions(Expression expression) {
        if (expression instanceof ConstantExpression) {
            return true;
        } else if (expression instanceof PropertyExpression) {
            PropertyExpression propertyExpression = (PropertyExpression) expression;
            Expression objectExpression = propertyExpression.getObjectExpression();
            Expression property = propertyExpression.getProperty();
            return isDotSeparatedConstantExpressions(objectExpression) && isDotSeparatedConstantExpressions(property);
        } else {
            return false;
        }
    }
}