package ru.runa.gpd.office.store.externalstorage.predicate;

import java.util.Optional;
import java.util.StringTokenizer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.office.store.externalstorage.VariableProvider;
import ru.runa.wfe.var.VariableDoesNotExistException;

public class PredicateParser {
    public static final char UNICODE_CHARACTER_OVERLINE = '\u203E';

    private final String predicateString;
    private final VariableUserType variableUserType;
    private final VariableProvider variableProvider;

    public PredicateParser(String predicateString, VariableUserType variableUserType, VariableProvider variableProvider) {
        this.variableUserType = variableUserType;
        this.variableProvider = variableProvider;
        this.predicateString = hideSpacesInAttributeNames(predicateString.trim());
    }

    public ConstraintsPredicate<?, ?> parse() {
        final StringTokenizer st = new StringTokenizer(predicateString);
        ConstraintsPredicate<?, ?> result = null;

        int[] brackets = new int[2];
        boolean endExpressionLine = false;

        while (st.hasMoreTokens()) {            
            String token = st.nextToken();
            token = trimBrackets(token, brackets);
            if (endExpressionLine) {
                result.setBrackets(brackets);
                endExpressionLine = false;
                brackets = new int[2];
            }

            if (token.startsWith("[") && token.endsWith("]")) {
                token = token.replace(UNICODE_CHARACTER_OVERLINE, ' ');

                String[] parts = token.split("\\.");
                if (parts.length == 2) {
                    final String userTypeTableFieldName = parts[0].substring(1, parts[0].length() - 1)
                            + "."
                            + parts[1].substring(1, parts[1].length() - 1);

                    final Variable comparingWithVariable = variableProvider.variableByName(userTypeTableFieldName)
                            .orElseThrow(() -> new VariableDoesNotExistException(userTypeTableFieldName));

                    if (result instanceof VariablePredicate) {
                        ((VariablePredicate) result).setRight(comparingWithVariable);
                    } else if (result instanceof ExpressionPredicate) {
                        ((ExpressionPredicate<?>) result).getRight().setRight(comparingWithVariable);
                    }

                    endExpressionLine = true;
                } else {
                    final String userTypeFieldName = token.substring(1, token.length() - 1);
                    final Variable userTypeField = getUserTypeFieldByName(userTypeFieldName) // поле пользователького типа
                            .orElseThrow(() -> new VariableDoesNotExistException(userTypeFieldName));

                    if (result != null && result instanceof VariablePredicate) {
                        ((VariablePredicate) result).setLeft(userTypeField);
                    } else if (result != null && result instanceof ExpressionPredicate) {
                        VariablePredicate right = ((ExpressionPredicate<?>) result).getRight();
                        if (right != null) {
                            right.setLeft(userTypeField);
                        } else {
                            right = new VariablePredicate(userTypeField, null, null);
                            ((ExpressionPredicate<?>) result).setRight(right);
                            right.setParent(result);
                            right.setBrackets(brackets);
                        }
                    } else {
                        result = new VariablePredicate(userTypeField, null, null); // узел дерева - операция, в которой участвует это поле
                        // пользователького типа. Ее левый потомок - это userTypeField - поле пользователького типа
                    }
                }
            } else if (PredicateOperationType.codes().contains(token.toLowerCase())) { // если токен обозначает операцию
                PredicateOperationType type = PredicateOperationType.byCode(token.toLowerCase()).get();
                if (PredicateOperationType.AND.equals(type) || PredicateOperationType.OR.equals(type)) { // если логическая операция
                    if (result instanceof VariablePredicate) { // создаем узел дерева для операции; то, что было слева, становится левым потомком
                        result = new ExpressionPredicate<VariablePredicate>((VariablePredicate) result, type, null);
                    } else { // то же самое, но если слева было сложное выражение
                        result = new ExpressionPredicate<ExpressionPredicate<?>>((ExpressionPredicate<?>) result, type, null);
                    }
                    if (result.getLeft() != null) {
                        ((ConstraintsPredicate<?, ?>) result.getLeft()).setParent(result);
                    }
                    brackets = new int[2];
                } else if (result != null && result instanceof ExpressionPredicate) {
                    final VariablePredicate right = ((ExpressionPredicate<?>) result).getRight();
                    right.setType(type);
                } else if (result != null && result instanceof VariablePredicate) { // для операции уже был сделан узел с заданным левым потомком
                    ((VariablePredicate) result).setType(type); // теперь устанавливается тип операции
                }
            } else if (token.startsWith("@")) { // если токен - переменная
                final String variableName = token.substring(1);
                final Variable comparingWithVariable = variableProvider.variableByScriptingName(variableName)
                        .orElseThrow(() -> new VariableDoesNotExistException(variableName));
                if (result != null && result instanceof VariablePredicate) {// result - узел операции, в которой участвует эта переменная
                    ((VariablePredicate) result).setRight(comparingWithVariable);
                } else if (result != null && result instanceof ExpressionPredicate) {
                    final VariablePredicate right = ((ExpressionPredicate<?>) result).getRight();
                    right.setRight(comparingWithVariable);
                }
                endExpressionLine = true;
            }
        }
        return result;
    }

    public String trimBrackets(String str, int[] brackets) {
        while (str.charAt(0) == '(') {
            brackets[0]++;
            str = str.substring(1);
            if (str.length() == 0) {
                break;
            }
        }
        if (str.length() != 0) {
            while (str.charAt(str.length() - 1) == ')') {
                brackets[1]++;
                str = str.substring(0, str.length() - 1);
                if (str.length() == 0) {
                    break;
                }
            }
        }
        return str;
    }

    private Optional<Variable> getUserTypeFieldByName(String name) {
        return variableUserType.getAttributes().stream().filter(variable -> variable.getName().equals(name)).findAny();
    }

    private String hideSpacesInAttributeNames(String condition) {
        char[] conditionChars = condition.toCharArray();
        boolean attributeName = false;
        for (int i = 0; i < conditionChars.length; i++) {
            char c = conditionChars[i];
            if (c == '[') {
                attributeName = true;
            } else if (c == ']') {
                attributeName = false;
            } else if (c == ' ') {
                if (attributeName) {
                    conditionChars[i] = UNICODE_CHARACTER_OVERLINE;
                }
            }
        }
        return new String(conditionChars);
    }
}
