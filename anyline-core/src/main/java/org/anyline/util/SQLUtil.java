/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.anyline.util;

import org.anyline.metadata.Metadata;
import org.anyline.util.regular.RegularUtil;

import java.util.HashSet;
import java.util.List;

public class SQLUtil {

	public static HashSet<String> keys = new HashSet<>();
	static {
		String[] ks = new String[]{"abort", "abs", "absent", "absolute", "access", "accessed", "according", "account", "action", "activate", "active", "ada", "add", "admin", "administer", "administrator", "advise", "advisor", "after", "aggregate", "algorithm", "alias", "all", "allocate", "allow", "also", "alter", "always", "analyse", "analyze", "ancillary", "and", "and_equal", "antijoin", "any", "append", "apply", "archive", "archivelog", "are", "array", "as", "asc", "ascending", "asensitive", "assertion", "assignment", "associate", "asymmetric", "at", "atomic", "attach", "attribute", "attributes", "audit", "authenticated", "authentication", "authid", "authorization", "auto", "auto-increment", "autoallocate", "autoextend", "autoinc", "automatic", "availability", "avg", "backup", "backward", "base64", "become", "before", "begin", "behalf", "bernoulli", "between", "bfile", "bigfile", "bigint", "binary", "binding", "bit", "bitmap", "bits", "blob", "block", "blocked", "blocks", "blocksize", "body", "bom", "boolean", "both", "bound", "breadth", "break", "broadcast", "browse", "buffer", "build", "bulk", "by", "byte", "bytes", "cache", "call", "called", "cancel", "cardinality", "cascade", "cascaded", "case", "cast", "catalog", "category", "ceil", "ceiling", "certificate", "cfile", "chain", "chained", "change", "char", "character", "characteristics", "characters", "check", "checkpoint", "child", "choose", "chunk", "class", "clear", "clob", "clone", "close", "cluster", "clustered", "coalesce", "coarse", "cobol", "collate", "collation", "collect", "column", "columns", "comment", "comments", "commit", "committed", "compact", "compatibility", "compile", "complete", "compress", "compute", "computed", "concurrently", "condition", "conditional", "configuration", "confirm", "conflict", "conforming", "connect", "connection", "consider", "consistent", "constraint", "constraints", "constructor", "container", "containing", "contains", "containstable", "content", "contents", "context", "continue", "control", "controlfile", "controlrow", "conversion", "convert", "copy", "corr", "corresponding", "corruption", "cost", "count", "create", "cross", "cstring", "csv", "cube", "current", "current_path", "current_role", "current_row", "current_schema", "current_time", "current_timestamp", "current_user", "cursor", "cycle", "dangling", "data", "database", "databases", "datafile", "datafiles", "datalink", "dataobjno", "date", "datetime", "day", "deallocate", "dec", "decimal", "declare", "decrement", "default", "defaults", "deferrable", "deferred", "defined", "definer", "degree", "delay", "delete", "delimiter", "delimiters", "demand", "deny", "depends", "depth", "deref", "derived", "desc", "descending", "describe", "descriptor", "detach", "detached", "determines", "deterministic", "diagnostics", "dictionary", "dimension", "directory", "disable", "disassociate", "discard", "disconnect", "disk", "diskgroup", "disks", "dismount", "dispatch", "distinct", "distinguished", "distributed", "div", "dlnewcopy", "dlpreviouscopy", "dlurlcomplete", "dlurlcompleteonly", "dlurlcompletewrite", "dlurlpath", "dlurlpathonly", "dlurlpathwrite", "dlurlscheme", "dlurlserver", "dlvalue", "dml", "do", "document", "domain", "double", "downgrade", "drop", "dummy", "dump", "dynamic", "each", "element", "else", "elseif", "empty", "enable", "enclosed", "encoding", "encrypted", "encryption", "end", "endexec", "enforce", "enforced", "entry", "enum", "equals", "errlvl", "error", "errorexit", "escape", "escaped", "estimate", "event", "events", "every", "except", "exception", "exceptions", "exchange", "exclude", "excluding", "exclusive", "exec", "execute", "exempt", "exists", "exit", "exp", "expire", "explain", "explosion", "export", "expression", "extend", "extends", "extension", "extent", "extents", "external", "externally", "extract", "fact", "failed", "failgroup", "false", "family", "fast", "fbtscan", "fetch", "field", "fields", "file", "fillfactor", "filter", "final", "fine", "finish", "first", "flag", "flagger", "flashback", "float", "flob", "floor", "floppy", "flush", "following", "for", "force", "foreign", "fortran", "forward", "found", "free", "freelist", "freelists", "freepools", "freetext", "freetexttable", "freeze", "fresh", "from", "fs", "full", "function", "functions", "fusion", "general", "generated", "generator", "get", "global", "globally", "go", "goto", "grant", "granted", "greatest", "group", "grouping", "groups", "guarantee", "guaranteed", "guard", "handler", "hash", "hashkeys", "having", "header", "heap", "hex", "hierarchy", "high", "hold", "holdlock", "hour", "identified", "identifier", "identity", "idgenerators", "if", "ifnull", "ignore", "ilike", "immediate", "immediately", "immutable", "implementation", "implicit", "import", "in", "inactive", "including", "increment", "incremental", "indent", "index", "indexed", "indexes", "indextype", "indextypes", "indicator", "infile", "infinite", "informational", "inherit", "inherits", "initial", "initialized", "initially", "initrans", "inline", "inner", "inout", "input", "insensitive", "insert", "instance", "instances", "instantiable", "instantly", "instead", "int", "integer", "integrity", "intermediate", "interpreted", "intersect", "intersection", "interval", "into", "invalidate", "invoker", "is", "isnull", "isolation", "iterate", "java", "job", "join", "keep", "kerberos", "key", "keyfile", "keys", "keysize", "kill", "label", "lag", "language", "large", "last", "lateral", "layer", "lead", "leading", "leakproof", "least", "left", "length", "less", "level", "levels", "library", "like", "limit", "lineno", "lines", "link", "list", "listen", "ln", "load", "lob", "local", "localtime", "localtimestamp", "location", "locator", "lock", "locked", "log", "logfile", "logged", "logging", "logical", "logoff", "logon", "long", "longint", "lower", "ltrim", "main", "manage", "managed", "management", "manual", "map", "mapping", "master", "match", "matched", "materialize", "materialized", "max", "maxarchlogs", "maxdatafiles", "maxextents", "maximize", "maxinstances", "maxlogfiles", "maxloghistory", "maxlogmembers", "maxsize", "maxtrans", "maxvalue", "measures", "member", "memory", "merge", "message", "method", "migrate", "min", "minextents", "minimize", "minimum", "minus", "minute", "minvalue", "mirror", "mirrorexit", "mlslabel", "mod", "mode", "model", "modifies", "modify", "module", "money", "monitoring", "month", "more", "mount", "move", "movement", "multiset", "mumps", "named", "namespace", "nan", "national", "native", "natural", "nav", "nchar", "nclob", "needed", "nested", "nesting", "network", "never", "new", "next", "nfc", "nfd", "nfkc", "nfkd", "nil", "noappend", "noarchivelog", "noaudit", "nocache", "nocheck", "nocompress", "nocycle", "nodelay", "noforce", "noguarantee", "nologging", "nomapping", "nomaxvalue", "nominimize", "nominvalue", "nomonitoring", "nonclustered", "none", "noorder", "nooverride", "noparallel", "norely", "norepair", "noresetlogs", "noreverse", "norewrite", "normal", "normalize", "normalized", "norowdependencies", "nosegment", "nosort", "nostrict", "noswitch", "not", "nothing", "notify", "notnull", "novalidate", "nowait", "ntile", "null", "nullable", "nullif", "nulls", "number", "numeric", "nvarchar", "nvarchar2", "object", "objno", "octets", "of", "off", "offline", "offset", "offsets", "oid", "oidindex", "oids", "old", "on", "once", "online", "only", "opaque", "opcode", "open", "operator", "optimal", "option", "options", "or", "order", "ordered", "ordering", "ordinality", "organization", "others", "out", "outer", "outline", "output", "over", "overflow", "overlaps", "overlay", "overriding", "own", "owned", "owner", "p", "package", "packages", "pad", "page", "pages", "parallel", "parameter", "parameters", "parent", "parity", "parser", "partial", "partially", "partition", "partitions", "pascal", "passing", "passthrough", "password", "path", "pctfree", "pctincrease", "pctthreshold", "pctused", "pctversion", "percent", "performance", "period", "perm", "permanent", "permission", "pfile", "physical", "pipe", "placing", "plan", "plans", "pli", "policy", "portion", "position", "power", "prebuilt", "precedes", "preceding", "precision", "prepare", "prepared", "present", "preserve", "primary", "print", "prior", "private", "privilege", "privileges", "proc", "procedural", "procedure", "processexit", "profile", "program", "project", "protected", "protection", "public", "publication", "purge", "query", "queue", "quiesce", "quota", "quote", "raiserror", "random", "range", "rank", "rapidly", "raw", "rba", "read", "reads", "readtext", "real", "reassign", "rebalance", "rebuild", "recheck", "recover", "recoverable", "recovery", "recursive", "recycle", "recyclebin", "reduced", "redundancy", "ref", "reference", "referenced", "references", "referencing", "refresh", "regexp", "register", "reindex", "reject", "rekey", "relational", "relative", "release", "rely", "rename", "repair", "repeat", "repeatable", "replace", "replica", "replication", "require", "required", "requiring", "reserv", "reserving", "reset", "resetlogs", "resize", "resolve", "resolver", "resource", "respect", "restart", "restore", "restrict", "restricted", "result", "resumable", "resume", "retain", "retention", "return", "returning", "returns", "reuse", "reverse", "revoke", "rewrite", "right", "role", "roles", "rollback", "rollup", "routine", "row", "rowcount", "rowdependencies", "rowid", "rownum", "rows", "rtrim", "rule", "rules", "sample", "save", "savepoint", "sb4", "scale", "scan", "scheduler", "schema", "schemas", "scn", "scope", "scroll", "search", "second", "section", "security", "seed", "segment", "select", "selective", "selectivity", "self", "semijoin", "sensitive", "separator", "sequence", "sequenced", "sequences", "sequential", "serializable", "server", "servererror", "session", "sessiontimezone", "sessiontzname", "set", "setof", "sets", "settings", "setuser", "severe", "shadow", "share", "shared", "show", "shrink", "shutdown", "siblings", "sid", "similar", "simple", "single", "singletask", "singular", "size", "skip", "smallfile", "smallint", "snapshot", "some", "sort", "source", "space", "specific", "specification", "specifictype", "spfile", "split", "spreadsheet", "sql", "sqlcode", "sqlerror", "sqlexception", "sqlldr", "sqlstate", "sqlwarning", "sqrt", "stability", "stable", "standalone", "standby", "star", "start", "starting", "starts", "startup", "state", "statement", "static", "statistics", "stdin", "stdout", "stop", "storage", "store", "streams", "strict", "strip", "structure", "style", "submultiset", "subpartition", "subpartitions", "subscription", "substitutable", "substr", "substring", "succeeds", "successful", "sum", "summary", "supplemental", "suspend", "switch", "switchover", "symmetric", "synonym", "sysaux", "sysdate", "sysdba", "sysid", "sysoper", "system", "system_time", "system_user", "systimestamp", "t", "table", "tables", "tablesample", "tablespace", "tabno", "tape", "temp", "tempfile", "template", "temporary", "test", "text", "textsize", "than", "the", "then", "thread", "through", "ties", "time", "time_zone", "timeout", "timestamp", "to", "token", "top", "toplevel", "trace", "tracing", "tracking", "trailing", "tran", "transaction", "transform", "transforms", "transitional", "translate", "translation", "treat", "trigger", "triggers", "trim", "true", "truncate", "trusted", "tuning", "tx", "type", "types", "ub2", "uba", "uescape", "uid", "unarchived", "unbound", "unbounded", "uncommitted", "under", "undo", "undrop", "unencrypted", "uniform", "union", "unique", "unknown", "unlimited", "unlink", "unlisten", "unlock", "unlogged", "unnamed", "unnest", "unpacked", "unprotected", "unquiesce", "unrecoverable", "until", "untyped", "unusable", "unused", "updatable", "update", "updated", "updatetext", "upgrade", "upper", "upsert", "uri", "urowid", "usage", "use", "user", "using", "vacuum", "valid", "validate", "validation", "validator", "value", "values", "varbinary", "varchar", "varchar2", "variable", "variadic", "varray", "varying", "verbose", "version", "versioning", "versions", "view", "views", "volatile", "volume", "wait", "waitfor", "wellformed", "when", "whenever", "where", "while", "whitespace", "window", "with", "within", "without", "work", "wrapper", "write", "writetext", "xid", "xml", "xmlagg", "xmlattributes", "xmlbinary", "xmlcast", "xmlcolattval", "xmlcomment", "xmlconcat", "xmldeclaration", "xmldocument", "xmlelement", "xmlexists", "xmlforest", "xmliterate", "xmlnamespaces", "xmlparse", "xmlpi", "xmlquery", "xmlroot", "xmlschema", "xmlserialize", "xmltable", "xmltext", "xmltype", "xmlvalidate", "xor", "year", "yes", "zone"};
		for(String k:ks) {
			keys.add(k);
		}
	}

	/**
	 * 是否需要界定符
	 * @param key String
	 * @return boolean
	 */
	public static boolean delimiter(String key) {
		if(null == key) {
			return false;
		}
		key = key.trim();
		if(key.contains(" ") || key.contains("+") || key.contains("/") || key.contains(">") || key.contains("*") || key.contains("<")) {
			return false;
		}
		if(ConfigTable.IS_SQL_DELIMITER_OPEN || key.contains("-") || (ConfigTable.IS_AUTO_CHECK_KEYWORD && keys.contains(key.toLowerCase()))) {
			return true;
		}
		return false;
	}
	public static StringBuilder delimiter(StringBuilder builder, String src, String delimiter) {
		if(BasicUtil.isEmpty(src)) {
			return builder;
		}
		if(delimiter == null) {
			builder.append(src);
			return builder;
		}
		if(!delimiter(src)) {
			builder.append(src);
			return builder;
		}
		String delimiterFr = "";
		String delimiterTo = "";
		delimiter = delimiter.replaceAll("\\s", "");
		if(delimiter.length() == 0) {
			return builder;
		}else if(delimiter.length() ==1) {
			delimiterFr = delimiter;
			delimiterTo = delimiter;
		}else{
			delimiterFr = delimiter.substring(0, 1);
			delimiterTo = delimiter.substring(1, 2);
		}
		return delimiter(builder, src, delimiterFr, delimiterTo);
	}
	public static StringBuilder delimiter(StringBuilder builder, Metadata src, String delimiterFr, String delimiterTo) {
		String name =  null;
		if(null != src) {
			name = src.getName();
		}
		return delimiter(builder, name, delimiterFr, delimiterTo);
	}
	public static StringBuilder delimiter(StringBuilder builder, String src, String delimiterFr, String delimiterTo) {
		if("null".equalsIgnoreCase(src)) {
			builder.append(src);
			return builder;
		}
		if(BasicUtil.isEmpty(src)) {
			return builder;
		}
		if(src.startsWith("${") && src.endsWith("}")) {
			String body = RegularUtil.cut(src, "${", "}");
			builder.append(body);
			return builder;
		}

		if(!delimiter(src)) {
			builder.append(src);
			return builder;
		}
		if(BasicUtil.isNumber(src)) {
			builder.append(src);
			return builder;
		}
		if(src.contains("'") || src.contains("\"")) {
			builder.append(src);
			return builder;
		}
		src = src.trim();
		if(src.startsWith(delimiterFr) || src.endsWith(delimiterTo)) {
			builder.append(src);
			return builder ;
		}
		String[] holder = placeholder();
		if(null != holder) {
			if(src.startsWith(holder[0]) || src.endsWith(holder[1])) {
				builder.append(src);
				return builder ;
			}
		}
		if(src.contains(".")) {
			String[] cols = src.split("\\.");
			int size = cols.length;
			for(int i=0; i<size; i++) {
				String col = cols[i];
				builder.append(delimiterFr).append(col).append(delimiterTo);
				if(i < size-1) {
					builder.append(".");
				}
			}
		}else if(src.contains(" ") || src.contains("(")) {
			builder.append(src);
		}else {
			builder.append(delimiterFr).append(src).append(delimiterTo);
		}

		return builder ;
	}
	public static String delimiter(String src, String delimiterFr, String delimiterTo) {
		if(BasicUtil.isEmpty(src)) {
			return "";
		}
		if(!delimiter(src)) {
			return src;
		}
		if(src.startsWith(delimiterFr) || src.endsWith(delimiterTo)) {
			return src ;
		}
		String result = SQLUtil.delimiter(new StringBuilder(), src, delimiterFr, delimiterTo).toString();
		return result;
	}

	/**
	 * 界定符的占位符
	 * @param src src
	 * @param delimiterFr delimiterFr
	 * @param delimiterTo delimiterTo
	 * @return String
	 */
	public static String placeholder(String src, String delimiterFr, String delimiterTo) {
		if(null == src) {
			return src;
		}

		// 未开启占位符
		if(!ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN) {
			return src;
		}
		String[] holder = placeholder();
		if(null == holder) {
			return src;
		}

		String holderFr = holder[0];
		String holderTo = holder[1];
		if(null == holderFr || null == holderTo || null == delimiterFr || null == delimiterTo) {
			return src;
		}
		if(holderFr.equals(holderTo) && delimiterFr.equals(delimiterTo)) {
			src = src.replace(holderFr, delimiterFr);
		}else{
			try {
				String regxFr = holderFr.replace("(", "\\(").replace("{", "、\\{").replace("[", "\\[");
				String regxTo = holderTo.replace(")", "\\)").replace("}", "、\\}").replace("]", "\\]");
				List<List<String>> lists = RegularUtil.fetchs(src, "("+regxFr+")" + "(.+?)" + "("+regxTo+")");
				for(List<String> list: lists) {
					String full = list.get(0);
					// String fr = list.get(1);
					String key = list.get(2).trim();
					// String to = list.get(3);
					String replace = delimiterFr + key + delimiterTo;
					src = src.replace(full, replace);
				}
			}catch (Exception ignored) {
			}
		}

		return src;
	}

	private static String config_holder = null;
	private static String[] static_holder = null;

	/**
	 * 界定符的占位符
	 * @return String
	 */
	public static String[] placeholder() {
		if (config_holder != null && config_holder.equals(ConfigTable.SQL_DELIMITER_PLACEHOLDER)) {
			if(null != static_holder) {
				return static_holder;
			}
		}
		if(ConfigTable.IS_SQL_DELIMITER_PLACEHOLDER_OPEN) {
			config_holder = ConfigTable.SQL_DELIMITER_PLACEHOLDER;
			if(null == config_holder) {
				return null;
			}
			String holderFr = "";
			String holderTo = "";
			config_holder = config_holder.replaceAll("\\s", "");
			if(config_holder.length() == 0) {
				return null;
			}else if(config_holder.length() ==1) {
				holderFr = config_holder;
				holderTo = config_holder;
			}else{
				holderFr = config_holder.substring(0, 1);
				holderTo = config_holder.substring(1, 2);
			}
			static_holder = new String[]{holderFr, holderTo};
			return static_holder;
		}
		return null;
	}
	public static boolean isSingleColumn(String column) {
		if(null != column) {
			column = column.trim();
			if(!RegularUtil.match(column, "^[a-zA-Z0-9_]+$")) {
				return false;
			}
		}
		return true;
	}
} 
