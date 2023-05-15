package org.anyline.entity.data;

public enum DatabaseType {
        Cassandra			("CASSANDRA"	, false,""),
        ClickHouse			( "CLICKHOUSE"	, false, "ru.yandex.clickhouse.ClickHouseDriver"),
        CockroachDB			("CockroachDB"	, false,""),
        DB2					("db2"			, false,"com.ibm.db2.jcc.DB2Driver"),
        Derby  				("Derby"		, false,"org.apache.derby.jdbc.EmbeddedDriver"),
        DM		 			("达梦"			, false,"dm.jdbc.driver.DmDriver"),
        GBase  				("南大通用"		, false,"com.gbase.jdbc.Driver"),
        H2  				("H2"			, false,"org.h2.Driver"),
        HighGo				("瀚高"			, false,"com.highgo.jdbc.Driver"),
        HSQLDB  			("HSQLDB"		, false,"org.hsqldb.jdbcDriver"),
        InfluxDB			("InfluxDB"	, false,""),
        KingBase			("人大金仓"		, true,"com.kingbase8.Driver"),
        MariaDB				("MariaDB"		, false,"org.mariadb.jdbc.Driver"),
        MongoDB				("MongoDB"		, false,""),
        MSSQL				("SQL Server"	, false,"com.microsoft.sqlserver.jdbc.SQLServerDriver"),
        MYSQL				("MySQL"		, false,"com.mysql.cj.jdbc.Driver"),
        Neo4j  				("Neo4j"		, false,"org.neo4j.jdbc.Driver"),
        OceanBase 			("OceanBase"	, false,"com.oceanbase.jdbc.Driver"),
        Opengauss 			("高斯"			, false,"org.opengauss.Driver"),
        ORACLE				("Oracle"		, false,"oracle.jdbc.OracleDriver"),
        oscar				("神舟通用"		, false,"com.oscar.Driver"),
        PolarDB  			("PolarDB"		, false,"com.aliyun.polardb.Driver"),
        PostgreSQL 			("PostgreSQL"	, false,"org.postgresql.Driver"),
        QuestDB 			("QuestDB"		, false,"org.postgresql.Driver"),
        RethinkDB  			("RethinkDB"	, false,""),
        SQLite  			("SQLite"		, false,"org.sqlite.JDBC"),
        TDengine  			("TDengine"	, false,"com.taosdata.jdbc.TSDBDriver"),
        Timescale			("Timescale"	, false,"org.postgresql.Driver");
        private DatabaseType(String title, boolean upperObjectName, String driver){
                this.title = title;
                this.driver = driver;
                this.upperObjectName = upperObjectName;
        }
        //时查询系统表时对象名(表名、列表)是否大写
        private final boolean upperObjectName;
        private final String title;
        private final String driver;
        public String driver(){
                return driver;
        }
        public String getTitle(){
                return title;
        }

}
