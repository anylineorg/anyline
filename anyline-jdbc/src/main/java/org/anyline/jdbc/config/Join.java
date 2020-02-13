package org.anyline.jdbc.config;

public class Join {
    public static enum TYPE{
        INNER               {public String getCode(){return "INNER";} 	public String getName(){return "内连接";}},
        LEFT				{public String getCode(){return "LEFT JOIN";} 	public String getName(){return "左连接";}},
        RIGHT			    {public String getCode(){return "RIGHT OJIN";} 	public String getName(){return "右连接";}},
        FULL				{public String getCode(){return "FULL";} 	public String getName(){return "全连接";}};
        public abstract String getName();
        public abstract String getCode();
    }
    private String schema;
    private String name;
    private TYPE type;
    private String condition;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if(name != null && name.contains(".")){
            String[] tmps = name.split(".");
            this.schema = tmps[0];
            this.name = tmps[1];
        }
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
