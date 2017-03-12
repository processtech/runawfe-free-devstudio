package ru.runa.gpd.extension.decision;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.dialog.DoubleInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;

import com.google.common.base.Objects;

public abstract class GroovyTypeSupport {
    private static final Map<String, GroovyTypeSupport> TYPES_MAP = new HashMap<String, GroovyTypeSupport>();
    static {
        TYPES_MAP.put(Object.class.getName(), new DefaultType());
        TYPES_MAP.put(String.class.getName(), new StringType());
        TYPES_MAP.put(Boolean.class.getName(), new BooleanType());
        TYPES_MAP.put(Number.class.getName(), new NumberType());
        TYPES_MAP.put(Date.class.getName(), new DateType());
        TYPES_MAP.put(BigDecimal.class.getName(), new BigDecimalType());
    }

    public static GroovyTypeSupport get(String className) {
        if (className == null) {
            className = Object.class.getName();
        }
        GroovyTypeSupport typeSupport = TYPES_MAP.get(className);
        while (typeSupport == null) {
            try {
                Class<?> superClass = Class.forName(className).getSuperclass();
                if (superClass == null) {
                    superClass = Object.class;
                }
                className = superClass.getName();
                typeSupport = TYPES_MAP.get(className);
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("Not found type support for type: " + className + ", using default (" + e + ")");
                typeSupport = TYPES_MAP.get(Object.class.getName());
            }
        }
        return typeSupport;
    }

    public boolean hasUserInputEditor() {
        return true;
    }

    public List<String> getPredefinedValues(Operation operation) {
        List<String> v = new ArrayList<String>();
        if (Objects.equal(Operation.EQ.getOperator(), operation.getOperator())
                || Objects.equal(Operation.NOT_EQ.getOperator(), operation.getOperator())) {
            v.add("null");
        }
        return v;
    }

    public UserInputDialog createUserInputDialog() {
        return new UserInputDialog();
    }

    abstract String wrap(Object value);

    public String unwrapValue(String value) {
        return value;
    }

    abstract List<Operation> getTypedOperations();

    static class DefaultType extends GroovyTypeSupport {
        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getScriptingName();
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public boolean hasUserInputEditor() {
            return false;
        }

        @Override
        List<Operation> getTypedOperations() {
            return null;
        }
    }

    static class StringType extends GroovyTypeSupport {
        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getScriptingName();
            } else if (value instanceof String) {
                return "\"" + value + "\"";
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public String unwrapValue(String value) {
            if (value.length() > 1) {
                return value.substring(1, value.length() - 1);
            }
            return super.unwrapValue(value);
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new Operation(Localization.getString("Groovy.Operation.contains"), "contains") {
                @Override
                public String generateCode(Variable variable, Object lexem2) {
                    StringBuffer buffer = new StringBuffer("");
                    buffer.append(wrap(variable));
                    buffer.append(".contains(");
                    buffer.append(wrap(lexem2));
                    buffer.append(")");
                    return buffer.toString();
                }
            });
            return extOperations;
        }
    }

    static class BooleanType extends GroovyTypeSupport {
        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getScriptingName() + ".booleanValue()";
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public List<String> getPredefinedValues(Operation operation) {
            List<String> v = super.getPredefinedValues(operation);
            v.add("true");
            v.add("false");
            return v;
        }

        @Override
        public boolean hasUserInputEditor() {
            return false;
        }

        @Override
        List<Operation> getTypedOperations() {
            return null;
        }
    }

    static class NumberType extends GroovyTypeSupport {
        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getScriptingName() + ".doubleValue()";
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public UserInputDialog createUserInputDialog() {
            return new DoubleInputDialog();
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new Operation(Localization.getString("Groovy.Operation.more"), ">"));
            extOperations.add(new Operation(Localization.getString("Groovy.Operation.less"), "<"));
            extOperations.add(new Operation(Localization.getString("Groovy.Operation.moreeq"), ">="));
            extOperations.add(new Operation(Localization.getString("Groovy.Operation.lesseq"), "<="));
            return extOperations;
        }
    }

    static class DateType extends GroovyTypeSupport {
        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getScriptingName();
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public boolean hasUserInputEditor() {
            return false;
        }

        class DateTypeOperation extends Operation {
            public DateTypeOperation(String visibleName, String operator) {
                super(visibleName, operator);
            }

            @Override
            public String generateCode(Variable variable, Object lexem2) {
                if (NULL.equals(lexem2)) {
                    if (Objects.equal(Operation.EQ.getOperator(), getOperator())) {
                        return variable.getScriptingName() + " == " + NULL;
                    }
                    if (Objects.equal(Operation.NOT_EQ.getOperator(), getOperator())) {
                        return variable.getScriptingName() + " != " + NULL;
                    }
                }
                boolean needZeroTime = isDateFormatVariable(variable);
                boolean lexem2isVariable = lexem2 instanceof Variable;
                if (!needZeroTime && lexem2isVariable) {
                    needZeroTime = isDateFormatVariable((Variable) lexem2);
                }
                String var1 = wrap(variable);
                String var2 = wrap(lexem2);
                String code = var1 + " == null || ";
                if (lexem2isVariable) {
                    code += var2 + " == null || ";
                }
                code += toCalc(var1, needZeroTime) + ".compareTo(" + toCalc(var2, needZeroTime) + ") " + getOperator() + " 0";
                return code;
            }

            String toCalc(String varName, boolean needZeroTime) {
                String code = "ru.runa.wfe.commons.CalendarUtil.dateToCalendar(" + varName + ")";
                if (needZeroTime) {
                    code = "ru.runa.wfe.commons.CalendarUtil.getZeroTimeCalendar(" + code + ")";
                }
                return code;
            }

            boolean isDateFormatVariable(Variable var) {
                return "ru.runa.wfe.var.format.DateFormat".equals(var.getFormatClassName());
            }
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new DateTypeOperation(Localization.getString("Groovy.Operation.later"), ">"));
            extOperations.add(new DateTypeOperation(Localization.getString("Groovy.Operation.latereq"), ">="));
            extOperations.add(new DateTypeOperation(Localization.getString("Groovy.Operation.earlier"), "<"));
            extOperations.add(new DateTypeOperation(Localization.getString("Groovy.Operation.earliereq"), "<="));
            extOperations.add(new DateTypeOperation(Operation.EQ.getVisibleName(), Operation.EQ.getOperator()));
            extOperations.add(new DateTypeOperation(Operation.NOT_EQ.getVisibleName(), Operation.NOT_EQ.getOperator()));
            return extOperations;
        }
    }

    static class BigDecimalType extends GroovyTypeSupport {

        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getScriptingName();
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public UserInputDialog createUserInputDialog() {
            return new DoubleInputDialog();
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new BigDecimalTypeOperation(Localization.getString("Groovy.Operation.more"), ">"));
            extOperations.add(new BigDecimalTypeOperation(Localization.getString("Groovy.Operation.less"), "<"));
            extOperations.add(new BigDecimalTypeOperation(Localization.getString("Groovy.Operation.moreeq"), ">="));
            extOperations.add(new BigDecimalTypeOperation(Localization.getString("Groovy.Operation.lesseq"), "<="));
            return extOperations;
        }

        class BigDecimalTypeOperation extends Operation {

            public BigDecimalTypeOperation(String visibleName, String operator) {
                super(visibleName, operator);
            }

            @Override
            public String generateCode(Variable variable, Object lexem2) {
                if (NULL.equals(lexem2)) {
                    if (Objects.equal(Operation.EQ.getOperator(), getOperator())) {
                        return variable.getScriptingName() + " == " + NULL;
                    }
                    if (Objects.equal(Operation.NOT_EQ.getOperator(), getOperator())) {
                        return variable.getScriptingName() + " != " + NULL;
                    }
                }
                boolean lexem2isVariable = lexem2 instanceof Variable;
                String var1 = wrap(variable);
                String var2 = wrap(lexem2);
                String code = var1 + " == null || ";
                if (lexem2isVariable) {
                    code += var2 + " == null || ";
                }
                code += var1 + ".compareTo( new BigDecimal( " + var2 + " ) ) " + getOperator() + " 0";
                return code;
            }

        }

    }

}
