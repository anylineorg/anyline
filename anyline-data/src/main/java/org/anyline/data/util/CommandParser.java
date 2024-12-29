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

package org.anyline.data.util;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.SyntaxHelper;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.VariableBlock;
import org.anyline.data.prepare.init.DefaultVariable;
import org.anyline.data.prepare.init.DefaultVariableBlock;
import org.anyline.data.run.TextRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.Compare;
import org.anyline.metadata.Column;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    private Log log = LogProxy.get(CommandParser.class);

    /**
     * 解析文本中的占位符
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    public static void parseText(DataRuntime runtime, TextRun run) {
        /*run.supportSqlVarPlaceholderRegexExt(supportSqlVarPlaceholderRegexExt(runtime));
        CommandParser.parseText(runtime, run);*/
        RunPrepare prepare = run.getPrepare();
        if(null == prepare) {
            return;
        }
        String text = prepare.getText();
        if(null == text) {
            return;
        }
        parseText(runtime, run, text);

    }

    /**
     * 解析文本
     * @param runtime runtime
     * @param run run
     * @param text text
     */
    public static void parseText(DataRuntime runtime, TextRun run, String text) {
        boolean supportSqlVarPlaceholderRegexExt = ConfigStore.IS_ENABLE_PLACEHOLDER_REGEX_EXT(run.getConfigs()) && runtime.getAdapter().supportSqlVarPlaceholderRegexExt(runtime);
        try{
            //${ AND ID = ::ID}  ${AND CODE=:CODE }
            List<List<String>> boxes = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_BOX_REGEX, Regular.MATCH_MODE.CONTAIN);
            if(!boxes.isEmpty()) {
                String box = boxes.get(0).get(0);
                String prev = RegularUtil.cut(text, RegularUtil.TAG_BEGIN, box);
                List<Variable> vars = parseTextVariable(supportSqlVarPlaceholderRegexExt,  prev, Compare.EMPTY_VALUE_SWITCH.NULL);
                run.addVariable(vars);
                VariableBlock block = parseTextVarBox(runtime, run.getConfigs(), text, box);
                if(null != block) {
                    run.addVariableBlock(block);
                    run.addVariable(block.variables());
                }
                String next = RegularUtil.cut(text, box, RegularUtil.TAG_END);
                parseText(runtime, run, next);
            }else{
                List<Variable> vars = parseTextVariable(supportSqlVarPlaceholderRegexExt, text, Compare.EMPTY_VALUE_SWITCH.NULL);
                run.addVariable(vars);
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static List<Variable> parseTextVariable(boolean supportSqlVarPlaceholderRegexExt, String text, Compare.EMPTY_VALUE_SWITCH emptyValueSwitch) {
        List<Variable> vars = new ArrayList<>();
        if(null == text) {
            return vars;
        }
        try{
            //${ID = :ID}
            int type = 0;
            // AND CD = {CD} || CD LIKE '%{CD}%' || CD IN ({CD}) || CD = ${CD} || CD = #{CD}
            //{CD} 用来兼容旧版本，新版本中不要用，避免与josn格式冲突
            List<List<String>> keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX, Regular.MATCH_MODE.CONTAIN);
            type = Variable.KEY_TYPE_SIGN_V2 ;

            //::KEY 格式的占位符解析,在PG环境中会与 ::INT8 格式冲突 需要禁用
            if(keys.isEmpty() && supportSqlVarPlaceholderRegexExt) {
                // AND CD = :CD || CD LIKE ':CD' || CD IN (:CD) || CD = ::CD
                keys = RegularUtil.fetchs(text, RunPrepare.SQL_VAR_PLACEHOLDER_REGEX_EXT, Regular.MATCH_MODE.CONTAIN);
                type = Variable.KEY_TYPE_SIGN_V1 ;
            }
            if(BasicUtil.isNotEmpty(true, keys)) {
                // AND CD = :CD
                for(int i=0; i<keys.size();i++) {
                    List<String> keyItem = keys.get(i);
                    Variable var = SyntaxHelper.buildVariable(type, keyItem.get(0), keyItem.get(1), keyItem.get(2), keyItem.get(3));
                    if(null == var) {
                        continue;
                    }
                    var.setSwt(emptyValueSwitch);
                    vars.add(var);
                }// end for
            }else{
                // AND CD = ?
                int qty = SQLUtil.countPlaceholder(text);
                if(qty > 0) {
                    for(int i=0; i<qty; i++) {
                        Variable var = new DefaultVariable();
                        var.setType(Variable.VAR_TYPE_INDEX);
                        var.setSwt(emptyValueSwitch);
                        vars.add(var);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return vars;
    }
    public static VariableBlock parseTextVarBox(DataRuntime runtime, ConfigStore configs, String text, String box) {
        // ${ AND ID = ::ID}
        // ${AND CODE=:CODE }
        if(null != box) {
            box = box.trim();
            String body = box.substring(2, box.length()-1);
            boolean supportSqlVarPlaceholderRegexExt = ConfigStore.IS_ENABLE_PLACEHOLDER_REGEX_EXT(configs) && runtime.getAdapter().supportSqlVarPlaceholderRegexExt(runtime);
            List<Variable> vars = parseTextVariable(supportSqlVarPlaceholderRegexExt, body, Compare.EMPTY_VALUE_SWITCH.IGNORE);
            //run.addVariable(vars);
            VariableBlock block = new DefaultVariableBlock(box, body);
            block.variables(vars);
            //run.addVariableBlock(block);
            return block;
        }
        return null;
    }

    /**
     * query [命令合成]<br/>
     * 替换占位符
     * 先执行 ${AND ID = :ID}
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    public static String replaceVariable(DataRuntime runtime, TextRun run, List<VariableBlock> blocks, List<Variable> variables, String text) {
        //StringBuilder builder = run.getBuilder();
        //List<Variable> variables = run.getVariables();
        //List<VariableBlock> blocks = run.getVariableBlocks();
        if(null != blocks) {
            for (VariableBlock block : blocks) {
                String box = block.box();
                String body = block.body();
                boolean active = block.active();
                if (!active) {
                    text = text.replace(box, "");
                    variables.removeAll(block.variables());
                } else {
                    text = text.replace(box, body);
                }
            }
        }
        text = replaceVariable(runtime, run, variables, text);
        return text;
    }
    private static String replaceVariable(DataRuntime runtime, TextRun run, List<Variable> variables, String text) {
        DriverAdapter adapter = runtime.getAdapter();
        boolean supportPlaceholder = adapter.supportPlaceholder();

        if(null != text && supportPlaceholder && null != variables) {
            for(Variable var:variables) {
                if(null == var) {
                    continue;
                }
                if(var.getType() == Variable.VAR_TYPE_REPLACE) {
                    // CD = ::CD
                    List<Object> values = var.getValues();
                    String value = null;
                    if(BasicUtil.isNotEmpty(true, values)) {
                        if(var.getCompare() == Compare.IN) {
                            value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
                        }else {
                            value = values.get(0).toString();
                        }
                    }
                    if(null != value) {
                        text = text.replace(var.getFullKey(), value);
                    }else{
                        text = text.replace(var.getFullKey(), "NULL");
                    }
                }
            }
            for(Variable var:variables) {
                if(null == var) {
                    continue;
                }
                if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE) {
                    // CD = ':CD'
                    List<Object> values = var.getValues();
                    String value = null;
                    if(BasicUtil.isNotEmpty(true, values)) {
                        if(var.getCompare() == Compare.IN) {
                            value = BeanUtil.concat(BeanUtil.wrap(values, "'"));
                        }else {
                            value = values.get(0).toString();
                        }
                    }
                    if(null != value) {
                        text = text.replace(var.getFullKey(), value);
                    }else{
                        text = text.replace(var.getFullKey(), "");
                    }
                }
            }
            for(Variable var:variables) {
                if(null == var) {
                    continue;
                }
                if(var.getType() == Variable.VAR_TYPE_KEY) {
                    // CD = :CD
                    List<Object> varValues = var.getValues();
                    if(run.getBatch() >1) {//批量执行时在下一步提供值
                        text = text.replace(var.getFullKey(), "?");
                    }else if(BasicUtil.isNotEmpty(true, varValues)) {
                        if(var.getCompare() == Compare.IN) {
                            // 多个值IN
                            String replaceDst = "";
                            for(Object tmp:varValues) {
                                replaceDst += " ?";
                            }
                            adapter.addRunValue(runtime, run, Compare.IN, new Column(var.getKey()), varValues);
                            replaceDst = replaceDst.trim().replace(" ",",");
                            text = text.replace(var.getFullKey(), replaceDst);
                        }else{
                            // 单个值
                            text = text.replace(var.getFullKey(), "?");
                            adapter.addRunValue(runtime, run, Compare.EQUAL, new Column(var.getKey()), varValues.get(0));
                        }
                    }else{
                        //没有提供参数值
                        text = text.replace(var.getFullKey(), "NULL");
                    }
                }
            }
            // 添加其他变量值
            for(Variable var:variables) {
                if(null == var) {
                    continue;
                }
                // CD = ?
                if(var.getType() == Variable.VAR_TYPE_INDEX) {
                    List<Object> varValues = var.getValues();
                    Object value = null;
                    if(BasicUtil.isNotEmpty(true, varValues)) {
                        value = varValues.get(0);
                    }
                    adapter.addRunValue(runtime, run, Compare.EQUAL, new Column(var.getKey()), value);
                }
            }
        }
        return text;
    }
}
