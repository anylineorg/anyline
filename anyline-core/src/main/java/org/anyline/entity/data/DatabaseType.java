package org.anyline.entity.data;

import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.KeyAdapter.KEY_CASE;

public enum DatabaseType {
        Cassandra			("CASSANDRA"	, KEY_CASE.SRC,""),
        ClickHouse			( "CLICKHOUSE"	, KEY_CASE.SRC, "ru.yandex.clickhouse.ClickHouseDriver"),
        CockroachDB			("CockroachDB"	, KEY_CASE.SRC,""),
        DB2					("db2"			, KEY_CASE.SRC,"com.ibm.db2.jcc.DB2Driver"),
        Derby  				("Derby"		, KEY_CASE.SRC,"org.apache.derby.jdbc.EmbeddedDriver"),
        DM		 			("达梦"			, KEY_CASE.SRC,"dm.jdbc.driver.DmDriver"),
        GBase  				("南大通用"		, KEY_CASE.SRC,"com.gbase.jdbc.Driver"),
        H2  				("H2"			, KEY_CASE.SRC,"org.h2.Driver"),
        HighGo				("瀚高"			, KEY_CASE.SRC,"com.highgo.jdbc.Driver"),
        HSQLDB  			("HSQLDB"		, KEY_CASE.SRC,"org.hsqldb.jdbcDriver"),
        InfluxDB			("InfluxDB"	, KEY_CASE.SRC,""),
        KingBase			("人大金仓"		, KEY_CASE.UPPER,"com.kingbase8.Driver"),
        MariaDB				("MariaDB"		, KEY_CASE.SRC,"org.mariadb.jdbc.Driver"),
        MongoDB				("MongoDB"		, KEY_CASE.SRC,""),
        MSSQL				("SQL Server"	, KEY_CASE.SRC,"com.microsoft.sqlserver.jdbc.SQLServerDriver"),
        MYSQL				("MySQL"		, KEY_CASE.SRC,"com.mysql.cj.jdbc.Driver"),
        Neo4j  				("Neo4j"		, KEY_CASE.SRC,"org.neo4j.jdbc.Driver"),
        OceanBase 			("OceanBase"	, KEY_CASE.SRC,"com.oceanbase.jdbc.Driver"),
        Opengauss 			("高斯"			, KEY_CASE.SRC,"org.opengauss.Driver"),
        ORACLE				("Oracle"		, KEY_CASE.SRC,"oracle.jdbc.OracleDriver"),
        oscar				("神舟通用"		, KEY_CASE.SRC,"com.oscar.Driver"),
        PolarDB  			("PolarDB"		, KEY_CASE.SRC,"com.aliyun.polardb.Driver"),
        PostgreSQL 			("PostgreSQL"	, KEY_CASE.SRC,"org.postgresql.Driver"),
        QuestDB 			("QuestDB"		, KEY_CASE.SRC,"org.postgresql.Driver"),
        RethinkDB  			("RethinkDB"	, KEY_CASE.SRC,""),
        SQLite  			("SQLite"		, KEY_CASE.SRC,"org.sqlite.JDBC"),
        TDengine  			("TDengine"	, KEY_CASE.SRC,"com.taosdata.jdbc.TSDBDriver"),
        Timescale			("Timescale"	, KEY_CASE.SRC,"org.postgresql.Driver");
        private DatabaseType(String title, KeyAdapter.KEY_CASE objectNameCase, String driver){
                this.title = title;
                this.driver = driver;
                this.objectNameCase = objectNameCase;
        }
        //时查询系统表时对象名(表名、列表)是否大写
        private final KEY_CASE objectNameCase;
        private final String title;
        private final String driver;
        public String driver(){
                return driver;
        }
        public String getTitle(){
                return title;
        }
        public KEY_CASE nameCase(){
                return objectNameCase;
        }

}
