package org.anyline.data.entity;

import org.anyline.entity.data.Function;
import org.anyline.entity.data.Parameter;

import java.util.ArrayList;
import java.util.List;

public class DefaultFunction implements Function {
    private String catalog;
    private String schema;
    private String name;
    private List<Parameter> parameter = new ArrayList<>();
    private String definition;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public String getCatalog() {
        return catalog;
    }

    @Override
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }
}
