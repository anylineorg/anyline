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
    private String alias;
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
    }
    private void parseName(){
        if(null != name){
            if(null != name && name.contains(".")){
                String[] tbs = name.split("\\.");
                name = tbs[1];
                schema = tbs[0];
            }
            String tag = " as ";
            String lower = name.toLowerCase();
            int tagIdx = lower.indexOf(tag)+tag.length();
            if(tagIdx > 0){
                String alias = name.substring(tagIdx).trim();
                name = name.substring(0,tagIdx).trim();
            }
            if(name.contains(" ")){
                String[] tmps = name.split(" ");
                name = tmps[0];
                alias = tmps[1];
            }
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
