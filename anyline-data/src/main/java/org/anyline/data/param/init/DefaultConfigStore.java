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



package org.anyline.data.param.init;

import org.anyline.adapter.KeyAdapter;
import org.anyline.data.handler.DataHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigChain;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.Group;
import org.anyline.data.prepare.GroupStore;
import org.anyline.data.prepare.init.AbstractGroup;
import org.anyline.data.prepare.init.DefaultGroupStore;
import org.anyline.data.run.Run;
import org.anyline.entity.*;
import org.anyline.entity.Compare.EMPTY_VALUE_SWITCH;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.encrypt.DESUtil;

import java.util.*;

public class DefaultConfigStore implements ConfigStore {
	protected Class clazz										    ;
	protected DataHandler handler								    ; // 流式读取时handler
	protected ConfigChain chain										; // 条件集合
	protected PageNavi navi											; // 分页参数
	protected OrderStore orders										; // 排序依据
	protected GroupStore groups;
	protected String having;
	protected List<String> queryColumns     = new ArrayList<>()		; // 查询的列
	protected List<String> excludeColumns 	= new ArrayList<>()		; // 不查询的列
	protected List<Object> values									; // 保存values后续parse用到
	protected boolean cascade				= false					; // 是否开启级联操作
	protected boolean supportKeyHolder		= true					; // 是否支持返回自增主键值
	protected List<String> keyHolders		= new ArrayList<>()		; // 自增主键值key

	protected Boolean override              = null                  ; //如果数据库中存在相同数据(根据overrideBy)是否覆盖 true或false会检测数据库null不检测
	protected List<String> overrideByColumns		= null			; //中存在相同数据(根据overrideBy)是否覆盖 true或false会检测数据库null不检测
	protected Constraint overrideByConstraint		= null			; //中存在相同数据(根据Constraint)是否覆盖 true或false会检测数据库null不检测
	protected List<String> primaryKeys    	= new ArrayList<>()     ; // 主键
	protected boolean integrality 			= true					; // 是否作为一个整体，不可分割，与其他条件合并时以()包围
	protected List<Run> runs				= new ArrayList<>()		; // 执行过的命令 包括ddl dml
	protected KeyAdapter.KEY_CASE kc 		= null					; //
	protected boolean execute				= true  				;
	protected String datasource				= null					; // 查询或操作的数据源
	protected String dest					= null					; // 查询或操作的目标(表,存储过程, sql等)
	protected Catalog catalog				= null					;
	protected Schema schema					= null					;
	protected Table table					= null					;

	@Override
	public Table table() {
		return table;
	}

	@Override
	public Schema schema() {
		return schema;
	}

	@Override
	public Catalog catalog() {
		return catalog;
	}
	@Override
	public String tableName() {
		if(null != table){
			return table.getName();
		}
		return null;
	}

	@Override
	public String schemaName() {
		if(null != schema){
			return schema.getName();
		}
		return null;
	}

	@Override
	public String catalogName() {
		if(null != catalog){
			return catalog.getName();
		}
		return null;
	}

	@Override
	public ConfigStore table(Table table) {
		this.table = table;
		return this;
	}

	@Override
	public ConfigStore schema(Schema schema) {
		this.schema = schema;
		return this;
	}

	@Override
	public ConfigStore catalog(Catalog catalog) {
		this.catalog = catalog;
		return this;
	}

	@Override
	public ConfigStore table(String table) {
		if(BasicUtil.isNotEmpty(table)) {
			this.table = new Table(table);
		}
		return this;
	}

	@Override
	public ConfigStore schema(String schema) {
		if(BasicUtil.isNotEmpty(schema)) {
			this.schema = new Schema(schema);
		}
		return this;
	}

	@Override
	public ConfigStore catalog(String catalog) {
		if(BasicUtil.isNotEmpty(catalog)) {
			this.catalog = new Catalog(catalog);
		}
		return this;
	}

	/**
	 * 设置查询或操作的数据源
	 * @param datasource 查询或操作的数据源
	 * @return ConfigStore
	 */
	@Override
	public ConfigStore datasource(String datasource) {
		this.datasource = datasource;
		return this;
	}

	/**
	 * 查询或操作的数据源
	 * @return String
	 */
	@Override
	public String datasource() {
		return datasource;
	}

	/**
	 * 设置查询或操作的目标(表, 存储过程, sql等)
	 * @param dest 查询或操作的目标
	 * @return ConfigStore
	 */
	@Override
	public ConfigStore dest(String dest) {
		this.dest = dest;
		if(null != dest && !dest.contains(" ") && !dest.contains(":")){
			table(dest);
		}
		return this;
	}

	/**
	 * 查询或操作的目标(表,存储过程, sql等)
	 * @return String
	 */
	@Override
	public String dest() {
		return dest;
	}

	@Override
	public ConfigStore copyProperty(ConfigStore configs) {
		if(null != configs){
			this.table = configs.table();
			this.handler = configs.handler();
			this.catalog = configs.catalog();
			this.schema = configs.schema();
			this.table = configs.table();
			this.datasource = configs.datasource();
			this.execute = configs.execute();
			this.clazz = configs.getClass();
			this.integrality = configs.integrality();
		}
		return this;
	}

	/**
	 * 设置虚拟主键，主要是用作为更新条件
	 * @param keys keys
	 * @return this
	 */
	@Override
	public ConfigStore keys(String ... keys) {
		if(null == this.primaryKeys){
			this.primaryKeys = new ArrayList<>();
		}else {
			this.primaryKeys.clear();
		}
		for(String key:keys){
			this.primaryKeys.add(key);
		}
		return this;
	}

	/**
	 * 虚拟主键，主要是用作为更新条件
	 * @return List
	 */
	@Override
	public List<String> keys() {
		return primaryKeys;
	}

	public DefaultConfigStore init(){
		return new DefaultConfigStore();
	}

	@Override
	public String toString(){
		String str = "";
		if(null != chain){
			str += chain.toString();
		}
		if(null != navi){
			str += "." + navi.getFirstRow() + "." + navi.getLastRow() + "." + navi.getCurPage();
		}
		if(null != orders){
			str += "." + orders.getRunText("");
		}
		if(null != groups){
			str += "." + groups.getRunText("");
		}
		return str;
	}

	@Override
	public boolean execute() {
		return execute;
	}

	@Override
	public ConfigStore execute(boolean execute) {
		this.execute = execute;
		return this;
	}

	/**
	 * 解析查询配置参数 
	 * @param config	  configs
	 * 			"COMPANY_CD:company","NM:nmEn% | NM:nmCn%","STATUS_VALUE:[status]"
	 * "NM:nmEn|NM:nmCn" 生成 NM={nmEn} OR NM = {nmCn}
	 * "NM:nmEn|nmCn" 生成 NM={nmEn} OR NM = {nmCn} nmEn为空时当前条件不生效
	 * "NM:nmEn|{1}" 
	 * "NM:nmEn:nmCn" 根据参数值生成NM = {nmEn}或生成 NM={nmCn}   
	 * @return Config
	 */
	@Override 
	public Config parseConfig(String config){
		if(null == config){
			return null; 
		} 
		DefaultConfig conf = null;
		if(config.contains("|")){
			conf = new DefaultConfigChain(config);
		}else{
			conf = new DefaultConfig(config);
		} 
		return conf; 
	}
	@Override
	public ConfigStore setPageNavi(PageNavi navi){
		this.navi = navi;
		return this;
	}
	@Override
	public ConfigStore copyPageNavi(PageNavi navi){
		if(null == this.navi){
			this.navi = navi;
		}else{
			this.navi.setBaseLink(navi.getBaseLink());
			this.navi.setCalType(navi.getCalType());
			this.navi.setCurPage(navi.getCurPage());
			this.navi.setDisplayPageFirst(navi.getDisplayPageFirst());
			this.navi.setDisplayPageLast(navi.getDisplayPageLast());
			this.navi.setFirstRow(navi.getFirstRow());
			this.navi.setLastRow(navi.getLastRow());
			this.navi.setPageRows(navi.getPageRows());
			this.navi.setTotalPage(navi.getTotalPage());
			this.navi.setTotalRow(navi.getTotalRow());
		}
		return this;
	}

	public DefaultConfigStore(String ... configs){
		configs = BasicUtil.compress(configs);
		chain = new DefaultConfigChain();
		for(String config:configs){
			chain.addConfig(parseConfig(config));
		}
	}

	/**
	 * 按起止行数查询
	 * @param first 起
	 * @param last 止
	 */
	public DefaultConfigStore(long first, long last){
		chain = new DefaultConfigChain();
		scope(first, last);
	}
	public DefaultConfigStore(List<String> configs){
		configs = BasicUtil.compress(configs);
		chain = new DefaultConfigChain();
		for(String config:configs){
			chain.addConfig(parseConfig(config));
		}
	}
	@Override
	public Boolean override(){
		return override;
	}
	@Override
	public List<String> overrideByColumns(){
		return overrideByColumns;
	}
	@Override
	public Constraint overrideByConstraint(){
		return overrideByConstraint;
	}
	@Override
	public ConfigStore override(Boolean override, String ... columns){
		this.override = override;
		if(null != columns){
			if(null == overrideByColumns){
				overrideByColumns = new ArrayList<>();
			}
			for(String column:columns){
				overrideByColumns.add(column);
			}
		}
		return this;
	}
	@Override
	public ConfigStore override(Boolean override, Constraint constraint){
		this.override = override;
		overrideByConstraint = constraint;
		return this;
	}

	@Override
	public DataHandler handler() {
		return handler;
	}

	@Override
	public ConfigStore handler(DataHandler handler) {
		this.handler = handler;
		return this;
	}

	@Override
	public ConfigStore entityClass(Class clazz) {
		this.clazz = clazz;
		return this;
	}

	@Override
	public Class entityClass() {
		return clazz;
	}

	/**
	 * 起止行 下标从0开始
	 * @param first 起
	 * @param last 止
	 * @return ConfigStore
	 */
	public ConfigStore scope(long first, long last){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.scope(first, last);
		navi.setTotalRow(last-first+1);
		this.setPageNavi(navi);
		return this;
	}

	/**
	 * 起止行 下标从0开始
	 * @param offset 指定第一个返回记录行的偏移量（即从哪一行开始返回） 初始行的偏移量为0
	 * @param rows 返回具体行数
	 * @return ConfigStore
	 */
	public ConfigStore limit(long offset, int rows){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.setFirstRow(offset);
		navi.setLastRow(offset+rows);
		navi.setCalType(1);
		navi.setTotalRow(rows);
		this.setPageNavi(navi);
		return this;
	}

	/**
	 * 设置是否需要是查询总行数<br/>
	 * maps国为性能考虑默认不查总行数，通过这个配置强制开启总行数查询，执行完成后会在page navi中存放总行数结果
	 * @param required 是否
	 * @return this
	 */
	public ConfigStore total(boolean required){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.total(required);
		return this;
	}

	public Boolean requiredTotal(){
		if(null != navi){
			return navi.requiredTotal();
		}
		return null;
	}
	@Override
	public boolean integrality() {
		return integrality;
	}

	@Override
	public ConfigStore integrality(boolean integrality) {
		this.integrality = integrality;
		if(null != chain) {
			chain.integrality(integrality);
		}
		return this;
	}

	@Override
	public List<Run> runs() {
		return runs;
	}

	@Override
	public ConfigStore runs(List<Run> runs) {
		this.runs = runs;
		return this;
	}

	@Override
	public ConfigStore add(Run run) {
		if(null == runs){
			runs = new ArrayList<>();
		}
		runs.add(run);
		return this;
	}

	/**
	 * 设置分页
	 * @param page 第page页 下标从1开始
	 * @param rows 每页rows行
	 * @return ConfigStore
	 */
	public ConfigStore page(long page, int rows){
		if(null == navi) {
			navi = new DefaultPageNavi();
		}
		navi.setCurPage(page);
		navi.setPageRows(rows);
		navi.setCalType(0);
		this.setPageNavi(navi);
		return this;
	}

	public ConfigStore addPrimaryKey(Collection<String> pks) {
		if (BasicUtil.isEmpty(pks)) {
			return this;
		}

		/*没有处于容器中时,设置自身主键*/
		if (null == primaryKeys) {
			primaryKeys = new ArrayList<>();
		}
		for (String item : pks) {
			if (BasicUtil.isEmpty(item)) {
				continue;
			}
			if (!primaryKeys.contains(item)) {
				primaryKeys.add(item);
			}
		}
		return this;
	}

	/**
	 * 设置主键
	 * @param pks keys
	 * @return DataRow
	 */
	public ConfigStore setPrimaryKey(Collection<String> pks) {
		if (BasicUtil.isEmpty(pks)) {
			return this;
		}
		if (null == this.primaryKeys) {
			this.primaryKeys = new ArrayList<>();
		} else {
			this.primaryKeys.clear();
		}
		return addPrimaryKey(pks);
	}

	public LinkedHashMap<String, Column> getPrimaryColumns() {
		LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
		List<String> pks = getPrimaryKeys();
		if(null != pks){
			for(String pk:pks){
				Column column = new Column(pk);
				columns.put(pk.toUpperCase(), column);
			}
		}
		return columns;
	}

	/**
	 * 读取主键
	 * 主键为空时且容器有主键时,读取容器主键,否则返回默认主键
	 * @return List
	 */
	public List<String> getPrimaryKeys() {
		/*有主键直接返回*/
		if (hasSelfPrimaryKeys()) {
			return primaryKeys;
		}
		return null;
	}

	public String getPrimaryKey() {
		List<String> keys = getPrimaryKeys();
		if (null != keys && !keys.isEmpty()) {
			return keys.get(0);
		}
		return null;
	}

	/**
	 * 自身是否有主键
	 * @return boolean
	 */
	public boolean hasSelfPrimaryKeys() {
		if (null != primaryKeys && !primaryKeys.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public ConfigStore and(EMPTY_VALUE_SWITCH swt, String text){
		Config conf = new DefaultConfig();
		conf.setSwitch(swt);
		conf.setText(text);
		chain.addConfig(conf);
		return this;
	}

	@Override
	public ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {

		if(null == compare){
			compare = Compare.AUTO;
		}
		int compareCode = compare.getCode();
		if(null == prefix && null != var && var.contains(".")){
			prefix = var.substring(0, var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}
		if(null == swt || EMPTY_VALUE_SWITCH.NONE == swt) {
			if (null != var) {
				if (var.startsWith("++")) {
					swt = EMPTY_VALUE_SWITCH.BREAK;
					var = var.substring(2);
				} else if (var.startsWith("+")) {
					swt = EMPTY_VALUE_SWITCH.NULL;
					var = var.substring(1);
				}
			}
		}
		//NULL NOT NULL
		if(compare == Compare.NULL || compare == Compare.NOT_NULL){
			String column = var;
			if(BasicUtil.isNotEmpty(prefix)){
				column = prefix + "." + var;
			}
			String txt = column + compare.formula();
			return and(swt, txt);
		}

		if(null == swt || EMPTY_VALUE_SWITCH.NONE == swt) {
			swt = EMPTY_VALUE_SWITCH.IGNORE;
		}

		value = value(value);

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 and id = 2 and id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}

		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			List list = (List)value;
			if (overValue) {
				chain.removeConfig(olds);
			}
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				boolean first = true;
				for(Object item:list){
					if(first){
						and(swt, compare, prefix, var, item, false, false);
						first = false;
					}else {
						or(compare, var, item);
					}
				}
			}else if(compareCode == 62){
				//FIND_IN_AND
				for(Object item:list){
					and(swt, compare, prefix, var, item, false, false);
				}
			}
		}else{
			if(null == conf){
				conf = new DefaultConfig();
				conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
				conf.setCompare(compare);
				conf.setPrefix(prefix);
				conf.setVariable(var);
				conf.setSwitch(swt);
				conf.setValue(value);
				chain.addConfig(conf);
			}else{
				conf.setOverCondition(overCondition);
				conf.setOverValue(overValue);
				if (overValue) {
					conf.setValue(value);
				} else {
					conf.addValue(value);
				}
			}
		}
		return this;
	}

	@Override
	public ConfigStore and(Config conf) {
		chain.addConfig(conf);
		return this;
	}

	/**
	 *
	 * @param configs ConfigStore
	 * @param apart 是否需要跟前面的条件保持隔离 <br/>
	 *              true:隔离,前面所有条件加到()，与configs合成一个新的list<br/>
	 *              false:不隔离,configs合并成原来的list中
	 * @return ConfigStore
	 */
	@Override
	public ConfigStore and(ConfigStore configs, boolean apart) {
		if(null == configs){
			return this;
		}
		ConfigChain list = null;
		ConfigChain chains = configs.getConfigChain();
		if(apart){
			list = new DefaultConfigChain();
			list.addConfig(chain);
		}else{
			list = chain;
		}
		//configs是否作为一个整体加入
		if(chains.integrality()) {
			list.addConfig(chains);
		}else{
			List<Config> items = chains.getConfigs();
			for(Config item:items){
				list.addConfig(item);
			}
		}
		chain = list;
		return this;
	}

	@Override
	public ConfigStore ands(Config config) {
		ConfigChain list = new DefaultConfigChain();
		list.addConfig(chain);
		list.addConfig(config);
		chain = list;
		return this;
	}

	@Override
	public ConfigStore ands(EMPTY_VALUE_SWITCH swt, String text){
		Config conf = new DefaultConfig();
		conf.setText(text);
		ConfigChain list = new DefaultConfigChain();
		list.addConfig(chain);
 		list.addConfig(conf);
		chain = list;
		return this;
	}
	@Override
	public ConfigStore ands(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		if(null == compare){
			compare = Compare.AUTO;
		}
		int compareCode = compare.getCode();
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}
		if(null == swt || EMPTY_VALUE_SWITCH.NONE == swt) {
			if (null != var) {
				if (var.startsWith("++")) {
					swt = EMPTY_VALUE_SWITCH.BREAK;
					var = var.substring(2);
				} else if (var.startsWith("+")) {
					swt = EMPTY_VALUE_SWITCH.NULL;
					var = var.substring(1);
				}
			}
		}
		//NULL NOT NULL
		if(compare == Compare.NULL || compare == Compare.NOT_NULL){
			String column = var;
			if(BasicUtil.isNotEmpty(prefix)){
				column = prefix + "." + var;
			}
			String txt = column + compare.formula();
			return ands(swt, txt);
		}

		if(null == swt || EMPTY_VALUE_SWITCH.NONE == swt) {
			swt = EMPTY_VALUE_SWITCH.IGNORE;
		}

		value = value(value);

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 and id = 2 and id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}
		ConfigStore newStore = new DefaultConfigStore();
		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			//集合
			List list = (List)value;
			if (overValue) {
				chain.removeConfig(olds);
			}
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				boolean first = true;
				for(Object item:list){
					if(first){
						newStore.and(swt, compare, prefix, var, item, false, false);
						first = false;
					}else {
						newStore.or(compare, var, item);
					}
				}
			}else if(compareCode == 62){
				//FIND_IN_AND
				for(Object item:list){
					newStore.and(swt, compare, prefix, var, item, false, false);
				}
			}
		}else{
			if(null == conf){
				conf = new DefaultConfig();
				conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
				conf.setCompare(compare);
				conf.setPrefix(prefix);
				conf.setVariable(var);
				conf.setSwitch(swt);
				conf.setValue(value);
				newStore.and(conf);
			}else{
				conf.setOverCondition(overCondition);
				conf.setOverValue(overValue);
				if (overValue) {
					conf.setValue(value);
				} else {
					conf.addValue(value);
				}
			}
		}
		this.ands(newStore);
		return this;
	}

	/**
	 * 根据占位符下标赋值,注意不需要提供下标,按顺序提供值即可
	 * @param values values
	 * @return this
	 */
	@Override
	public ConfigStore params(Object... values) {
		if(null == this.values){
			this.values = new ArrayList<>();
		}
		if(null != values){
            this.values.addAll(Arrays.asList(values));
		}
		return this;
	}

	@Override
	public ConfigStore params(Collection values) {
		if(null == this.values){
			this.values = new ArrayList<>();
		}
		this.values.addAll(values);
		return this;
	}

	@Override
	public List<Object> values() {
		return this.values;
	}

	@Override
	public ConfigStore or(ConfigStore configs, boolean apart) {
		if(null == configs){
			return this;
		}
		ConfigChain list = null;
		if(apart){
			list = new DefaultConfigChain();
			list.addConfig(chain);
		}else{
			list = chain;
		}
		ConfigChain orChain = configs.getConfigChain();
		orChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		//orChain.apart(apart);
		list.addConfig(orChain);
		chain = list;
		return this;
	}
	@Override
	public ConfigStore or(Config config) {
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		chain.addConfig(config);
		return this;
	}

	@Override
	public ConfigStore or(EMPTY_VALUE_SWITCH swt, String text){
		Config config = new DefaultConfig();
		config.setText(text);
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		chain.addConfig(config);
		return this;
	}

	@Override
	public ConfigStore ors(EMPTY_VALUE_SWITCH swt, String text){
		ConfigChain list = new DefaultConfigChain();
		list.addConfig(chain);
		Config config = new DefaultConfig();
		config.setText(text);
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		list.addConfig(config);
		chain = list;
		return this;
	}

	@Override
	public ConfigStore ors(EMPTY_VALUE_SWITCH swt, Config config) {
		ConfigChain list = new DefaultConfigChain();
		list.addConfig(chain);
		config.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
		list.addConfig(config);
		chain = list;
		return this;
	}

	@Override
	public ConfigStore or(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		List<Config> configs = chain.getConfigs();
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 or id = 2 or id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}
		// 如果当前没有其他条件
		if(configs.size()==0){
			and(swt, compare, prefix, var, value, overCondition, overValue);
		}else{
			int compareCode = compare.getCode();
			value = value(value);
			if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
				List list = (List)value;
				if (overValue) {
					chain.removeConfig(olds);
				}
				if(compareCode == 60 || compareCode == 61){
					//FIND_IN_OR
					for(Object item:list){
						or(swt, compare, prefix, var, item);
					}
				}else if(compareCode == 62){
					//FIND_IN_AND
					ConfigChain findChain = new DefaultConfigChain();
					findChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					for(Object item:list){
						conf = new DefaultConfig();
						conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
						conf.setCompare(compare);
						conf.setPrefix(prefix);
						conf.setVariable(var);
						conf.setValue(item);
						findChain.addConfig(conf);
					}
					chain.addConfig(findChain);
				}
			}else{
				//覆盖原条件(不要新加)
				if(null != conf){
					if(overValue){
						conf.setValue(value);
					}else {
						conf.addValue(value);
					}
				}else{
					ConfigChain orChain = new DefaultConfigChain();
					Config last = configs.get(configs.size()-1);
					configs.remove(last);

					if(last instanceof ConfigChain){
						ConfigChain lastChain = (ConfigChain)last;
						List<Config> lastItems = lastChain.getConfigs();
						for(Config lastItem:lastItems){
							orChain.addConfig(lastItem);
						}
					}else{
						orChain.addConfig(last);
					}
					conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					if(compare == Compare.NULL || compare == Compare.NOT_NULL){
						String column = var;
						if(BasicUtil.isNotEmpty(prefix)){
							column = prefix + "." + var;
						}
						String txt = column + compare.formula();
						conf.setText(txt);
					}else {
						conf.setCompare(compare);
						conf.setVariable(var);
						conf.setPrefix(prefix);
						conf.setValue(value);
					}
					orChain.addConfig(conf);
					chain.addConfig(orChain);
				}
			}
		}
		return this;
	}
	@Override
	public ConfigStore ors(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
		if(null == prefix && var.contains(".")){
			prefix = var.substring(0,var.indexOf("."));
			var = var.substring(var.indexOf(".")+1);
		}
		int compareCode = compare.getCode();

		List<Config> olds = new ArrayList<>();
		Config conf = null;
		if(overCondition){
			olds = chain.getConfigs(prefix, var, compare);
			if(olds.size()>0) {
				conf = olds.get(0);
				//相同参数只留一个 如 id = 1 or id = 2 or id = 3
				//只留下id = 1 下一步有可能把值1覆盖
				olds.remove(conf);
				chain.removeConfig(olds);
			}
		}

		ConfigChain newChain = new DefaultConfigChain();
		newChain.addConfig(chain);
		value = value(value);
		if(value instanceof List && ((List)value).size()>1 && compareCode >= 60 && compareCode <= 62){
			List list = (List)value;
			if (overValue) {
				chain.removeConfig(olds);
			}
			if(compareCode == 60 || compareCode == 61){
				//FIND_IN_OR
				for(Object item:list){
					conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
					conf.setPrefix(prefix);
					conf.setCompare(compare);
					conf.setVariable(var);
					conf.setValue(item);
					newChain.addConfig(conf);
				}
			}else if(compareCode == 62){
				//FIND_IN_AND
				ConfigChain findChain = new DefaultConfigChain();
				findChain.setJoin(Condition.CONDITION_JOIN_TYPE_OR);
				for(Object item:list){
					conf = new DefaultConfig();
					conf.setJoin(Condition.CONDITION_JOIN_TYPE_AND);
					conf.setCompare(compare);
					conf.setPrefix(prefix);
					conf.setVariable(var);
					conf.setValue(item);
					findChain.addConfig(conf);
				}
				newChain.addConfig(findChain);
			}
		}else {
			if(null != conf) {
				if(overValue){
					conf.setValue(value);
				}else{
					conf.addValue(value);
				}
			}else{
				conf = new DefaultConfig();
				conf.setJoin(Condition.CONDITION_JOIN_TYPE_OR);

				if(compare == Compare.NULL || compare == Compare.NOT_NULL){
					String column = var;
					if(BasicUtil.isNotEmpty(prefix)){
						column = prefix + "." + var;
					}
					String txt = column + compare.formula();
					conf.setText(txt);
				}else {
					conf.setCompare(compare);
					conf.setVariable(var);
					conf.setPrefix(prefix);
					conf.setValue(value);
				}
				newChain.addConfig(conf);
			}

		}
		
		chain = newChain;
		return this;
	}

	private Object value(Object value){
		if(value instanceof Object[]){
			value = BeanUtil.array2list((Object[])value);
		}
		if(value instanceof List){
			List list = (List)value;
			if(list.size() == 0){
				value = null;
			}else if(list.size() ==1){
				value = list.get(0);
			}
		}
		return value;
	}
	
	/** 
	 * 把httpRequest中的参数存放到navi 
	 */ 
	protected void setNaviParam(){
		if(null == chain || null == navi){
			return; 
		} 
		 
		List<Config> configs = chain.getConfigs(); 
		for(Config config:configs){
			if(null == config){
				continue;
			} 
			String key = config.getKey(); 
			List<Object> values = new ArrayList<Object>(); 
			List<Object> srcValues = config.getValues(); 
			if(config.isKeyEncrypt()){
				key = DESUtil.encryptParamKey(key); 
			} 
			if(config.isValueEncrypt() && null != srcValues){
				for(Object value:srcValues){
					if(null != value){
						value = DESUtil.encryptParamValue(value.toString()); 
						values.add(value); 
					} 
				} 
			}else{
				values = srcValues; 
			} 
			navi.addParam(key, values); 
		} 
	}
	@Override
	public ConfigChain getConfigChain(){
		return chain;
	}

	@Override
	public boolean isEmptyCondition(){
		if(null != chain){
			return chain.isEmpty();
		}
		return true;
	}

	@Override
	public boolean isEmpty(){
		if(null != handler){
			return false;
		}
		if(null !=chain && !chain.isEmpty()){
			return false;
		}
		if(null != navi){
			return false;
		}
		if(null != orders && !orders.isEmpty()){
			return false;
		}
		if(null != groups && !groups.isEmpty()){
			return false;
		}
		if(null != having){
			return false;
		}
		if(null != queryColumns && !queryColumns.isEmpty()){
			return false;
		}
		if(null != excludeColumns && !excludeColumns.isEmpty()){
			return false;
		}
		if(null != values && !values.isEmpty()){
			return false;
		}
		return true;
	}
	/** 
	 * 添加排序 
	 * @param order  order
	 * @param override 如果已存在相同的排序列 是否覆盖
	 * @return ConfigStore
	 */
	@Override 
	public ConfigStore order(Order order, boolean override){
		if(null == orders){
			orders = new DefaultOrderStore();
		} 
		orders.order(order, override);
		if(null != navi){
			navi.order(order.getColumn(), order.getType().getCode(), override);
		}
		return this; 
	}
	@Override
	public ConfigStore order(Order order){
		return order(order, true);
	}

	@Override
	public ConfigStore order(String column, Order.TYPE type, boolean override){
		return order(new DefaultOrder(column,type), override);
	}

	@Override
	public ConfigStore order(String column, Order.TYPE type){
		return order(column, type, true);
	}

	@Override
	public ConfigStore order(String column, String type, boolean override){
		return order(new DefaultOrder(column,type), override);
	}

	@Override
	public ConfigStore order(String column, String type){
		return order(column, type, true);
	}

	@Override
	public ConfigStore order(String order, boolean override){
		return order(new DefaultOrder(order), override);
	}
	@Override 
	public ConfigStore order(String order){
		return order(order, true);
	} 
	@Override 
	public OrderStore getOrders() {
		if(null == orders || orders.getOrders().isEmpty()){
			if(null != navi){
				orders = navi.getOrders();
			}
		}
		return orders; 
	} 
	@Override 
	public ConfigStore setOrders(OrderStore orders) {
		this.orders = orders;
		return this; 
	} 
	/** 
	 * 添加分组 
	 * @param group  group
	 * @return ConfigStore
	 */
	@Override 
	public ConfigStore group(Group group){
		if(null == groups){
			groups = new DefaultGroupStore();
		} 
		groups.group(group); 
		return this; 
	} 

	@Override 
	public ConfigStore group(String column){
		return group(new AbstractGroup(column));
	} 
	public GroupStore getGroups() {
		return groups; 
	} 
	public ConfigStore setGroups(GroupStore groups) {
		this.groups = groups;
		return this; 
	}

	@Override
	public ConfigStore having(String having) {
		this.having = having;
		return this;
	}

	@Override
	public String getHaving() {
		return having;
	}

	@Override
	public PageNavi getPageNavi() {
		return navi; 
	}

	@Override
	public Config getConfig(String var){
		return chain.getConfig(null,var);
	}
	public ConfigStore removeConfig(String var){
		Config config = getConfig(var);
		return removeConfig(config);
	}
	@Override
	public ConfigStore removeConfig(Config config){
		chain.removeConfig(config);
		return this;
	}
	@Override
	public List<Object> getConfigValues(String var){
		Config config = chain.getConfig(null, var);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String var){
		Config config = chain.getConfig(null,var);
		if(null != config){
			List<Object> values = config.getValues();
			if(null != values && !values.isEmpty()){
				return values.get(0);
			}
		}
		return null;
	}

	@Override
	public Config getConfig(String var, Compare compare){
		return chain.getConfig(null,var,compare);
	}
	@Override
	public ConfigStore removeConfig(String var, Compare compare){
		Config config = getConfig(var, compare);
		return removeConfig(config);
	}
	@Override
	public List<Object> getConfigValues(String var, Compare compare){
		Config config = chain.getConfig(null, var,compare);
		if(null != config){
			return config.getValues();
		}
		return null;
	}
	@Override
	public Object getConfigValue(String var, Compare compare){
		Config config = chain.getConfig(null, var,compare);
		if(null != config){
			List<Object> values = config.getValues();
			if(null != values && !values.isEmpty()){
				return values.get(0);
			}
		}
		return null;
	}
	/**
	 * 开启记录总数懒加载 
	 * @param ms 缓存有效期(毫秒)
	 * @return ConfigStore
	 */
	public ConfigStore setTotalLazy(long ms){
		if(null != navi){
			navi.setLazy(ms);
		}
		return this;
	}

	@Override 
	public ConfigStore setValue(Map<String, Object> values) {
		//this.values = values;
		if(null == chain || null == values){
			return this; 
		} 
		List<Config> configs = chain.getConfigs(); 
		for(Config config:configs){
			if(null == config){
				continue;
			} 
			config.setValue(values); 
		} 
		setNaviParam();
		return this; 
	}

	/**
	 * 设置城要查询的列
	 * @param columns 需要查询的列
	 * @return ConfigStore
	 */
	public ConfigStore columns(String ... columns){
		if(null != columns){
			for(String column:columns){
				this.queryColumns.add(column);
			}
		}
		return this;
	}
	public ConfigStore columns(List<String> columns){
		if(null != columns){
			this.queryColumns.addAll(columns);
		}
		return this;
	}
	public List<String> columns(){
		return queryColumns;
	}
	public ConfigStore excludes(String ... columns){
		if(null != columns){
			for(String column:columns){
				this.excludeColumns.add(column);
			}
		}
		return this;
	}
	public ConfigStore excludes(List<String> columns){
		if(null != columns){
			this.excludeColumns.addAll(columns);
		}
		return this;
	}
	public List<String> excludes(){
		return excludeColumns;
	}

	/**
	 * 级联(如删除点相关的边)
	 * @param cascade  是否开启
	 * @return ConfigStore
	 */
	public ConfigStore cascade(boolean cascade){
		this.cascade = cascade;
		return this;
	}
	public boolean cascade(){
		return this.cascade;
	}

	/**
	 * 是否支持返回自增主键值
	 * @return boolean
	 */
	public boolean supportKeyHolder(){
		return this.supportKeyHolder;
	}
	public ConfigStore supportKeyHolder(boolean support){
		this.supportKeyHolder = support;
		return this;
	}

	/**
	 * 自增主键值 keys
	 * @return keys
	 */
	public List<String> keyHolders(){
		return this.keyHolders;
	}
	public ConfigStore keyHolders(String ... keys){
		if(null != keys){
			for(String key:keys){
				keyHolders.add(key);
			}
		}
		return this;
	}

	@Override
	public boolean isValid() {
		if(null != chain){
			for(Config config:chain.getConfigs()){
				if(config.getSwitch() == EMPTY_VALUE_SWITCH.BREAK && config.isEmpty()){
					return false;
				}
			}
		}
		return true;
	}

	public ConfigStore condition(String join, Compare compare, String key, Object ... values){
		if("or".equalsIgnoreCase(join)){
			or(compare, key, values);
		}else{
			and(compare, key, values);
		}
		return this;
	}
	public ConfigStore condition(String join, String compare, String key, String value){
		return condition(join, compare(compare), key, value);
	}

	public static Compare compare(int code){
		for(Compare compare:Compare.values()){
			if(compare.getCode() == code){
				return compare;
			}
		}
		return Compare.EQUAL;
	}
	List<Object> statics = new ArrayList<>();
	public ConfigStore addStaticValue(Object value){
		if(value instanceof Collection){
			statics.addAll((Collection) value);
		}else {
			statics.add(value);
		}
		return this;
	}
	public List<Object> getStaticValues(){
		return statics;
	}

	@Override
	public KeyAdapter.KEY_CASE keyCase() {
		return kc;
	}

	@Override
	public ConfigStore keyCase(KeyAdapter.KEY_CASE kc) {
		this.kc = kc;
		return this;
	}

	protected Hashtable<String, Object> configs = new Hashtable<>();
	public ConfigStore config(String key, Object value){
		configs.put(key.toUpperCase(), value);
		return this;
	}
	public Object config(String key){
		Object value = null;
		key = key.toUpperCase();
		if(configs.containsKey(key)){
			value = configs.get(key);
		}else{
			value = ConfigTable.get(key);
		}
		return value;
	}
	protected long executeTime = -1;
	protected long lastExecuteTime = -1;
	protected long packageTime = -1;
	protected long lastPackageTime = -1;

	@Override
	public void setExecuteTime(long time) {
		this.executeTime = time;
	}

	@Override
	public long getExecuteTime() {
		if(this.executeTime == -1){
			return this.lastExecuteTime;
		}
		return this.executeTime;
	}

	@Override
	public void setLastExecuteTime(long time) {
		this.lastExecuteTime = time;
	}

	@Override
	public long getLastExecuteTime() {
		return this.lastExecuteTime;
	}

	@Override
	public void setPackageTime(long time) {
		this.packageTime = time;
	}

	@Override
	public long getPackageTime() {
		if(packageTime == -1){
			return this.lastPackageTime;
		}
		return this.packageTime;
	}

	@Override
	public void setLastPackageTime(long time) {
		this.lastPackageTime = time;
	}

	@Override
	public long getLastPackageTime() {
		return this.lastPackageTime;
	}

	public ConfigStore fetch(String ... keys){
		DefaultConfigStore result = new DefaultConfigStore();
		result.setOrders(this.getOrders());
		result.setGroups(this.getGroups());
		result.setPageNavi(this.getPageNavi());
		ConfigChain chain = new DefaultConfigChain();
		List<Config> configs = getConfigChain().getConfigs();
		for(Config config:configs){
			if(null == config){
				continue;
			}
			if(BasicUtil.contains(keys, config.getPrefix())){
				chain.addConfig((Config)config.clone());
			}
		}
		result.chain = chain;
		return result;
	}

	@Override
	public ConfigStore clone() {
		DefaultConfigStore clone = null;
		try{
			clone = (DefaultConfigStore)super.clone();
		}catch (Exception ignored){
			clone = new DefaultConfigStore();
		}
		if(null != this.orders) {
			clone.setOrders(orders.clone());
		}
		GroupStore groups = this.getGroups();
		if(null != groups) {
			clone.setGroups(groups.clone());
		}
		PageNavi navi = this.getPageNavi();
		if(null != navi) {
			clone.setPageNavi(navi.clone());
		}
		clone.chain =this.chain.clone();
		return clone;
	}
}