package ru.runa.gpd.extension.handler;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.util.XmlUtil;

public class SQLTasksModel extends Observable {
    public List<SQLTaskModel> tasks = new ArrayList<SQLTaskModel>();

    private SQLTasksModel() {
    }

    public static SQLTasksModel createDefault() {
        SQLTasksModel model = new SQLTasksModel();
        model.addNewTask(new SQLTaskModel());
        return model;
    }

    public boolean hasFields() {
        for (SQLTaskModel model : tasks) {
            if (model.hasFields()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        Document document = XmlUtil.createDocument("database-tasks", XmlUtil.RUNA_NAMESPACE, "database-tasks.xsd");
        for (SQLTaskModel model : tasks) {
            model.serialize(document, document.getRootElement());
        }
        return XmlUtil.toString(document);
    }

    public static SQLTasksModel fromXml(String xml) throws Exception {
        SQLTasksModel model = new SQLTasksModel();
        Document document = XmlUtil.parseWithoutValidation(xml);
        List<Element> taskElements = document.getRootElement().elements("task");
        for (Element taskElement : taskElements) {
            SQLTaskModel taskModel = SQLTaskModel.deserialize(taskElement);
            model.addNewTask(taskModel);
        }
        return model;
    }

    public SQLTaskModel getFirstTask() {
        return tasks.get(0);
    }

    private void addNewTask(SQLTaskModel taskModel) {
        taskModel.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                setChanged();
                notifyObservers();
            }
        });
        tasks.add(taskModel);
    }

    static class SQLTaskModel extends Observable {
        public String dsName = "";
        public List<SQLQueryModel> queries = new ArrayList<SQLQueryModel>();

        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }

        public boolean hasFields() {
            for (SQLQueryModel model : queries) {
                if (model.hasFields()) {
                    return true;
                }
            }
            return false;
        }

        public void deleteQuery(int index) {
            queries.remove(index);
            notifyObservers();
        }

        public void addQuery() {
            queries.add(new SQLQueryModel());
            notifyObservers();
        }

        public void deleteQueryParameter(int index, boolean result, int paramIndex) {
            SQLQueryModel queryModel = queries.get(index);
            if (result) {
                queryModel.results.remove(paramIndex);
            } else {
                queryModel.params.remove(paramIndex);
            }
            notifyObservers();
        }

        public void addQueryParameter(int index, boolean result) {
            SQLQueryModel queryModel = queries.get(index);
            if (result) {
                queryModel.results.add(new SQLQueryParameterModel(result));
            } else {
                queryModel.params.add(new SQLQueryParameterModel(result));
            }
            notifyObservers();
        }

        public void moveUpQueryParameter(int index, boolean result, int paramIndex) {
            SQLQueryModel queryModel = queries.get(index);
            if (result) {
                Collections.swap(queryModel.results, paramIndex - 1, paramIndex);
            } else {
                Collections.swap(queryModel.params, paramIndex - 1, paramIndex);
            }
            notifyObservers();
        }

        public void serialize(Document document, Element parent) {
            Element taskElement = parent.addElement("task", XmlUtil.RUNA_NAMESPACE);
            taskElement.addAttribute("datasource", dsName);
            Element queriesElement = taskElement.addElement("queries", XmlUtil.RUNA_NAMESPACE);
            for (SQLQueryModel model : queries) {
                model.serialize(document, queriesElement);
            }
        }

        public static SQLTaskModel deserialize(Element element) {
            SQLTaskModel model = new SQLTaskModel();
            model.dsName = element.attributeValue("datasource");
            Element queriesElement = element.element("queries");
            List<Element> queryElements = queriesElement.elements("query");
            for (Element qElement : queryElements) {
                SQLQueryModel queryModel = SQLQueryModel.deserialize(qElement);
                model.queries.add(queryModel);
            }
            return model;
        }
    }

    static class SQLQueryModel {
        public String query = "";
        public List<SQLQueryParameterModel> params = new ArrayList<SQLQueryParameterModel>();
        public List<SQLQueryParameterModel> results = new ArrayList<SQLQueryParameterModel>();

        public boolean hasFields() {
            for (SQLQueryParameterModel model : params) {
                if (model.fieldName != null && !model.swimlaneVar) {
                    return true;
                }
            }
            for (SQLQueryParameterModel model : results) {
                if (model.fieldName != null && !model.swimlaneVar) {
                    return true;
                }
            }
            return false;
        }

        public void serialize(Document document, Element parent) {
            Element queryElement = parent.addElement("query", XmlUtil.RUNA_NAMESPACE);
            queryElement.addAttribute("sql", query.replaceAll("\n", "&#10;"));
            for (SQLQueryParameterModel model : params) {
                model.serialize(document, queryElement);
            }
            for (SQLQueryParameterModel model : results) {
                model.serialize(document, queryElement);
            }
        }

        public static SQLQueryModel deserialize(Element element) {
            SQLQueryModel model = new SQLQueryModel();
            model.query = element.attributeValue("sql").replaceAll("&#10;", "\n");
            List<Element> children = element.elements();
            for (Element child : children) {
                SQLQueryParameterModel parameterModel = SQLQueryParameterModel.deserialize(child);
                if (parameterModel.result) {
                    model.results.add(parameterModel);
                } else {
                    model.params.add(parameterModel);
                }
            }
            return model;
        }
    }

    static class SQLQueryParameterModel {
        public boolean result;
        public boolean swimlaneVar;
        public String varName = "";
        public String fieldName;

        public SQLQueryParameterModel() {
        }

        public SQLQueryParameterModel(boolean result) {
            this.result = result;
        }

        public void serialize(Document document, Element parent) {
            String elementName;
            if (result) {
                elementName = swimlaneVar ? "swimlane-result" : "result";
            } else {
                elementName = swimlaneVar ? "swimlane-param" : "param";
            }
            Element paramElement = parent.addElement(elementName, XmlUtil.RUNA_NAMESPACE);
            paramElement.addAttribute("var", varName);
            if (fieldName != null) {
                paramElement.addAttribute("field", fieldName);
            }
        }

        public static SQLQueryParameterModel deserialize(Element element) {
            SQLQueryParameterModel model = new SQLQueryParameterModel();
            String elementName = element.getName();
            if ("swimlane-result".equals(elementName)) {
                model.result = true;
                model.swimlaneVar = true;
            }
            if ("result".equals(elementName)) {
                model.result = true;
                model.swimlaneVar = false;
            }
            if ("swimlane-param".equals(elementName)) {
                model.result = false;
                model.swimlaneVar = true;
            }
            if ("param".equals(elementName)) {
                model.result = false;
                model.swimlaneVar = false;
            }
            model.varName = element.attributeValue("var");
            String fieldName = element.attributeValue("field");
            if (!Strings.isNullOrEmpty(fieldName)) {
                model.fieldName = fieldName;
            }
            return model;
        }
    }
}
