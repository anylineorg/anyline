package org.anyline.data.entity;

import org.anyline.data.param.ConfigStore;
import org.anyline.metadata.Table;

import java.util.List;

public class Query {
    public class Column{
        private String table;
        private String origin;
        private String alias;

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }
    }
    public class Table{
        private String join;
        private String name;
        private String alias;
        private ConfigStore conditions;
    }
    protected String union;
    protected List<Column> columns;
    protected List<Table> tables;
    protected ConfigStore conditions;

    /**
     * 如果有union或union all时才会有unions
     */
    protected List<Query> unions;

    public static Query parse(String sql){
        Query query = new Query();
        //SELECT * FROM USER WHERE ID = 1 AND CODE = 2
        //别名
        //SELECT ID, NAME AS USER_NAME FROM USER
        //SELECT ID, CASE WHEN ID > 0 THEN 1 ELSE 0 END AS TYPE FROM USER
        //子查询
        //SELECT * FROM (SELECT * FROM USER WHERE ID = 1 AND CODE = 2) AS M WHERE TYPE = 11 AND NAME = 22
        //SELECT ID, NAME, (SELECT ID FROM DEPT WHERE ID = 1) AS DEPT FROM USER
        //UNION
        //SELECT ID, NAME FROM USER UNION ALL SELECT ID , NAME FROM HR_USER
        //子查询union
        //SELECT * FROM (SELECT ID, NAME FROM USER UNION ALL SELECT ID , NAME FROM HR_USER) AS M WHERE ID > 0
        //SELECT ID, CODE FROM (SELECT * FROM USER) AS M UNION ALL SELECT ID, CODE FROM (SELECT * FROM CRM_USER)
        return query;
    }
    public String toSQL(){
        StringBuilder builder = new StringBuilder();
        return builder.toString();
    }
    public String toJSON(){
        StringBuilder builder = new StringBuilder();
        return builder.toString();
    }

}
