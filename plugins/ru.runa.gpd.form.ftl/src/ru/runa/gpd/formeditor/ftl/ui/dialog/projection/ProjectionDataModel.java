package ru.runa.gpd.formeditor.ftl.ui.dialog.projection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.util.XmlUtil;

public class ProjectionDataModel extends Observable {
    private List<Projection> projections;

    private ProjectionDataModel(List<Projection> projections) {
        this.projections = projections;
    }

    @SuppressWarnings("unchecked")
    public static Optional<ProjectionDataModel> fromXml(String xml) {
        if (StringUtils.isBlank(xml)) {
            return Optional.empty();
        }

        final Document document = XmlUtil.parseWithoutValidation(xml);
        final List<Projection> projections = (List<Projection>) document.getRootElement().elements("projection").stream()
                .map(element -> Projection.deserialize((Element) element)).collect(Collectors.toList());

        return Optional.of(new ProjectionDataModel(projections));
    }

    public static ProjectionDataModel by(VariableUserType userType) {
        final List<Projection> projections = userType.getAttributes().stream().map(variable -> new Projection(variable.getName()))
                .collect(Collectors.toList());
        final ProjectionDataModel model = new ProjectionDataModel(projections);
        return model;
    }

    public ProjectionDataModel merge(ProjectionDataModel with) {
        if (projections.equals(with.getProjections())) {
            return this;
        }

        final Map<String, Projection> nameToProjection = with.getProjections().stream()
                .collect(Collectors.toMap(Projection::getName, Function.identity()));
        for (Projection projection : projections) {
            final Projection candidate = nameToProjection.get(projection.getName());
            if (candidate != null) {
                projection.clone(candidate);
            }
        }

        final List<Projection> resultProjections = new ArrayList<>(nameToProjection.values());
        resultProjections.sort((a, b) -> a.getName().compareTo(b.getName()));
        projections = resultProjections;

        return this;
    }

    @Override
    public String toString() {
        final Document document = XmlUtil.createDocument("projections");
        final Element root = document.getRootElement();
        for (Projection model : projections) {
            model.serialize(root.addElement("projection"));
        }
        return XmlUtil.toString(document);
    }

    public List<Projection> getProjections() {
        return projections;
    }
}
