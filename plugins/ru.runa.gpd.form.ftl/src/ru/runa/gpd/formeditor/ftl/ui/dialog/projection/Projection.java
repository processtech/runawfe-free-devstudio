package ru.runa.gpd.formeditor.ftl.ui.dialog.projection;

import org.dom4j.Element;

public class Projection {
    private String name;
    private Visibility visibility = Visibility.VISIBLE;
    private Sort sort = Sort.NONE;

    public Projection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public static Projection deserialize(Element element) {
        final Projection projection = new Projection(element.attributeValue("name"));
        projection.setVisibility(Visibility.valueOf(element.attributeValue("visibility")));
        projection.setSort(Sort.valueOf(element.attributeValue("sort")));
        return projection;
    }

    public void serialize(Element projection) {
        projection.addAttribute("name", name);
        projection.addAttribute("visibility", visibility.name());
        projection.addAttribute("sort", sort.name());
    }

    public Projection clone(Projection to) {
        to.setName(name);
        to.setVisibility(visibility);
        to.setSort(sort);
        return to;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((sort == null) ? 0 : sort.hashCode());
        result = prime * result + ((visibility == null) ? 0 : visibility.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Projection other = (Projection) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (sort != other.sort)
            return false;
        if (visibility != other.visibility)
            return false;
        return true;
    }

}
