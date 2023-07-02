package org.anyline.metadata.type;

import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.KeyAdapter.KEY_CASE;

import java.io.Serializable;

public enum DatabaseType  implements Serializable {
        AntDB			    ("AntDB"	    , KEY_CASE.SRC,""),
        Cassandra			("CASSANDRA"	, KEY_CASE.SRC,""),
        ClickHouse			( "CLICKHOUSE"	, KEY_CASE.SRC, "ru.yandex.clickhouse.ClickHouseDriver"),
        ChinaMobileDB   	("磐维数据库"    , KEY_CASE.SRC,""),
        ChinaUnicomDB   	("中国联通"      , KEY_CASE.SRC,""),
        Citus   			("Citus"	    , KEY_CASE.SRC,""),
        CockroachDB			("CockroachDB"	, KEY_CASE.SRC,""),
        DB2					("db2"			, KEY_CASE.SRC,"com.ibm.db2.jcc.DB2Driver"),
        Derby  				("Derby"		, KEY_CASE.SRC,"org.apache.derby.jdbc.EmbeddedDriver"),
        DM		 			("达梦"			, KEY_CASE.SRC,"dm.jdbc.driver.DmDriver"),
        GoldenDB  			("中兴GoldenDB"	, KEY_CASE.SRC,""),
        GBase  				("南大通用"		, KEY_CASE.SRC,"com.gbase.jdbc.Driver"),
        GaiaDB  			("百度GaiaDB-X"	, KEY_CASE.SRC,""),
        GreatDB  			("万里数据库"	, KEY_CASE.SRC,""),
        H2  				("H2"			, KEY_CASE.SRC,"org.h2.Driver"),
        HashData  			("酷克数据"	    , KEY_CASE.SRC,""),
        HighGo				("瀚高"			, KEY_CASE.SRC,"com.highgo.jdbc.Driver"),
        HotDB  			    ("热璞"         , KEY_CASE.SRC,""),
        HSQLDB  			("HSQLDB"		, KEY_CASE.SRC,"org.hsqldb.jdbcDriver"),
        InfluxDB			("InfluxDB"	, KEY_CASE.SRC,""),
        Informix			("Informix"	, KEY_CASE.SRC,"com.informix.jdbc.IfxDriver"),
        KingBase			("人大金仓"		, KEY_CASE.UPPER,"com.kingbase8.Driver"),
        KunDB			    ("星环"		    , KEY_CASE.UPPER,""),
        LightDB			    ("LightDB"		, KEY_CASE.UPPER,""),
        MariaDB				("MariaDB"		, KEY_CASE.SRC,"org.mariadb.jdbc.Driver"),
        MongoDB				("MongoDB"		, KEY_CASE.SRC,""),
        MogDB				("云和恩墨"		, KEY_CASE.SRC,""),
        MSSQL				("SQL Server"	, KEY_CASE.SRC,"com.microsoft.sqlserver.jdbc.SQLServerDriver"),
        MuDB				("沐融信息科技"	, KEY_CASE.SRC,""),
        MYSQL				("MySQL"		, KEY_CASE.SRC,"com.mysql.cj.jdbc.Driver"),
        Neo4j  				("Neo4j"		, KEY_CASE.SRC,"org.neo4j.jdbc.Driver"),
        OceanBase 			("OceanBase"	, KEY_CASE.SRC,"com.oceanbase.jdbc.Driver"),
        OpenGauss           ("高斯"			, KEY_CASE.SRC,"org.opengauss.Driver"),
        ORACLE				("Oracle"		, KEY_CASE.SRC,"oracle.jdbc.OracleDriver"),
        oscar				("神舟通用"		, KEY_CASE.SRC,"com.oscar.Driver"),
        PolarDB  			("PolarDB"		, KEY_CASE.SRC,"com.aliyun.polardb.Driver"),
        PostgreSQL 			("PostgreSQL"	, KEY_CASE.SRC,"org.postgresql.Driver"),
        QuestDB 			("QuestDB"		, KEY_CASE.SRC,"org.postgresql.Driver"),
        RethinkDB  			("RethinkDB"	, KEY_CASE.SRC,""),
        SinoDB  			("星瑞格"	    , KEY_CASE.SRC,""),
        SQLite  			("SQLite"		, KEY_CASE.SRC,"org.sqlite.JDBC"),
        StarDB  			("京东StarDB"	, KEY_CASE.SRC,""),
        TDengine  			("TDengine"	, KEY_CASE.SRC,"com.taosdata.jdbc.TSDBDriver"),
        UXDB			    ("优炫数据库"	, KEY_CASE.SRC,"com.uxsino.uxdb.Driver"),
        UbiSQL    			("平安科技"	    , KEY_CASE.SRC,""),
        TiDB    			("TiDB"	    , KEY_CASE.SRC,""),
        TDSQL    			("TDSQL"	    , KEY_CASE.SRC,""),
        Timescale			("Timescale"	, KEY_CASE.SRC,"org.postgresql.Driver"),
        Vastbase			("Vastbase"	, KEY_CASE.SRC,""),
        xigemaDB			("华胜信泰"	    , KEY_CASE.SRC,""),
        YiDB			    ("天翼数智"	    , KEY_CASE.SRC,"");
        DatabaseType(String title, KeyAdapter.KEY_CASE objectNameCase, String driver){
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
