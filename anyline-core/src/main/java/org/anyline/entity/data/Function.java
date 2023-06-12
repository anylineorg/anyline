package org.anyline.entity.data;

import java.util.List;

public interface Function {
    public String getName() ;

    public void setName(String name) ;

    public List<Parameter> getParameter();

    public void setParameter(List<Parameter> parameter);

    public String getDefinition();
    public void setDefinition(String definition) ;
    public void setCatalog(String schema);
    public String getCatalog();
    public void setSchema(String schema);
    public String getSchema();
}
