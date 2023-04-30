package org.anyline.entity.data;

public enum DatabaseType {
 
        Cassandra			{public String getCode(){return "DatabaseType_CASSANDRA";}			public String getName(){return "Cassandra";}			public String getDriver(){return "";}},
        ClickHouse			{public String getCode(){return "DatabaseType_CLICKHOUSE";}			public String getName(){return "ClickHouse";}			public String getDriver(){return "ru.yandex.clickhouse.ClickHouseDriver";}},
        CockroachDB			{public String getCode(){return "DatabaseType_COCKROACHDB";}			public String getName(){return "CockroachDB";}			public String getDriver(){return "";}},
        DB2					{public String getCode(){return "DatabaseType_DB2";}					public String getName(){return "db2";}					public String getDriver(){return "com.ibm.db2.jcc.DB2Driver";}},
        Derby  				{public String getCode(){return "DatabaseType_DERBY";}				public String getName(){return "Derby";}				public String getDriver(){return "org.apache.derby.jdbc.EmbeddedDriver";}},
        DM		 			{public String getCode(){return "DatabaseType_DM";}					public String getName(){return "达梦";}					public String getDriver(){return "dm.jdbc.driver.DmDriver";}},
        GBase  				{public String getCode(){return "DatabaseType_GBASE";}				public String getName(){return "南大通用";}				public String getDriver(){return "com.gbase.jdbc.Driver";}},
        H2  				{public String getCode(){return "DatabaseType_H2";}					public String getName(){return "H2";}					public String getDriver(){return "org.h2.Driver";}},
        HighGo				{public String getCode(){return "DatabaseType_HIGHGO";}				public String getName(){return "瀚高";}					public String getDriver(){return "com.highgo.jdbc.Driver";}},
        HSQLDB  			{public String getCode(){return "DatabaseType_HSQLDB";}				public String getName(){return "HSQLDB";}				public String getDriver(){return "org.hsqldb.jdbcDriver";}},
        InfluxDB			{public String getCode(){return "DatabaseType_INFLUXDB";}			public String getName(){return "InfluxDB";}				public String getDriver(){return "";}},
        KingBase			{public String getCode(){return "DatabaseType_KINGBASE";}			public String getName(){return "人大金仓 Oracle";}		public String getDriver(){return "com.kingbase8.Driver";}},
        KingBase_PostgreSQL	{public String getCode(){return "DatabaseType_KINGBASE_POSTGRESQL";}	public String getName(){return "人大金仓 PostgreSQL";}	public String getDriver(){return "";}},
        MariaDB				{public String getCode(){return "DatabaseType_MARIADB";}				public String getName(){return "MariaDB";}				public String getDriver(){return "org.mariadb.jdbc.Driver";}},
        MongoDB				{public String getCode(){return "DatabaseType_MONGODB";}				public String getName(){return "MongoDB";}				public String getDriver(){return "";}},
        MSSQL				{public String getCode(){return "DatabaseType_MSSQL";}				public String getName(){return "mssql";}				public String getDriver(){return "com.microsoft.sqlserver.jdbc.SQLServerDriver";}},
        MYSQL				{public String getCode(){return "DatabaseType_MYSQL";}				public String getName(){return "mysql";}				public String getDriver(){return "com.mysql.cj.jdbc.Driver";}},
        Neo4j  				{public String getCode(){return "DatabaseType_NEO4J";}				public String getName(){return "Neo4j";}				public String getDriver(){return "org.neo4j.jdbc.Driver";}},
        OceanBase 			{public String getCode(){return "DatabaseType_OCEANBASE";}			public String getName(){return "OceanBase";}			public String getDriver(){return "com.oceanbase.jdbc.Driver";}},
        ORACLE				{public String getCode(){return "DatabaseType_ORACLE";}				public String getName(){return "oracle";}				public String getDriver(){return "oracle.jdbc.OracleDriver";}},
        oscar				{public String getCode(){return "DatabaseType_OSCAR";}				public String getName(){return "神舟通用";}				public String getDriver(){return "com.oscar.Driver";}},
        PolarDB  			{public String getCode(){return "DatabaseType_POLARDB";}				public String getName(){return "PolarDB";}				public String getDriver(){return "com.aliyun.polardb.Driver";}},
        PostgreSQL 			{public String getCode(){return "DatabaseType_POSTGRESQL";}			public String getName(){return "PostgreSQL";}			public String getDriver(){return "org.postgresql.Driver";}},
        QuestDB 			{public String getCode(){return "DatabaseType_QUESTDB";}				public String getName(){return "QuestDB";}				public String getDriver(){return "org.postgresql.Driver";}},
        RethinkDB  			{public String getCode(){return "DatabaseType_RETHINKDB";}			public String getName(){return "RethinkDB";}			public String getDriver(){return "";}},
        SQLite  			{public String getCode(){return "DatabaseType_SQLITE";}				public String getName(){return "SQLite";}				public String getDriver(){return "org.sqlite.JDBC";}},
        TDengine  			{public String getCode(){return "DatabaseType_TDENGINE";}			public String getName(){return "TDengine";}				public String getDriver(){return "com.taosdata.jdbc.TSDBDriver";}},
        Timescale			{public String getCode(){return "DatabaseType_TIMESCALE";}			public String getName(){return "Timescale";}			public String getDriver(){return "org.postgresql.Driver";}};

        public abstract String getCode();
        public abstract String getName();
        //默认的驱动,在注册数据源时可能用到,如果不准确,需要根据依赖的驱动jar修改
        public abstract String getDriver();

}
