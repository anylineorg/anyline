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



package org.anyline.environment.boot.navi;

import org.anyline.entity.PageNaviConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration("anyline.environment.boot.page")
@ConfigurationProperties(prefix = "anyline.page")
public class PageNaviProperty {
    public String keyPageRows			    = "_anyline_page_rows"			; // 设置每页显示多少条的key
    public String keyPageNo			        = "_anyline_page"				; // 设置当前第几页的key
    public String keyTotalPage			    = "_anyline_total_page"			; // 显示一共多少页的key
    public String keyTotalRow			    = "_anyline_total_row"			; // 显示一共多少条的key
    public String keyShowStat			    = "_anyline_navi_show_stat"		; // 设置是否显示统计数据的key
    public String keyShowJump			    = "_anyline_navi_show_jump"		; // 设置是否显示页数跳转key
    public String keyShowVol			    = "_anyline_navi_show_vol"		; // 设置是否显示每页条数设置key
    public String keyGuide				    = "_anyline_navi_guide"			; // 设置分页样式的key
    public String keyIdFlag 			    = "_anyline_navi_conf_"			; // 生成配置文件标识
    public int varPageDefaultVol            = 10					        ; // 每页多少条
    public int varPageMaxVol				= 100					        ; // 每页最多多少条(只针对从http传过来的vol, 后台设置的不影响)
    public boolean varClientSetVolEnable	= false					        ; // 前端是否可设置每页多少条

    public String getKeyPageRows() {
        return keyPageRows;
    }

    public void setKeyPageRows(String keyPageRows) {
        this.keyPageRows = keyPageRows;
        PageNaviConfig.DEFAULT_KEY_PAGE_ROWS = keyPageRows;
    }

    public String getKeyPageNo() {
        return keyPageNo;
    }

    public void setKeyPageNo(String keyPageNo) {
        this.keyPageNo = keyPageNo;
        PageNaviConfig.DEFAULT_KEY_PAGE_NO = keyPageNo;
    }

    public String getKeyTotalPage() {
        return keyTotalPage;
    }

    public void setKeyTotalPage(String keyTotalPage) {
        this.keyTotalPage = keyTotalPage;
        PageNaviConfig.DEFAULT_KEY_TOTAL_PAGE = keyTotalPage;
    }

    public String getKeyTotalRow() {
        return keyTotalRow;
    }

    public void setKeyTotalRow(String keyTotalRow) {
        this.keyTotalRow = keyTotalRow;
        PageNaviConfig.DEFAULT_KEY_TOTAL_ROW = keyTotalRow;
    }

    public String getKeyShowStat() {
        return keyShowStat;
    }

    public void setKeyShowStat(String keyShowStat) {
        this.keyShowStat = keyShowStat;
        PageNaviConfig.DEFAULT_KEY_SHOW_STAT = keyShowStat;
    }

    public String getKeyShowJump() {
        return keyShowJump;
    }

    public void setKeyShowJump(String keyShowJump) {
        this.keyShowJump = keyShowJump;
        PageNaviConfig.DEFAULT_KEY_SHOW_JUMP = keyShowJump;
    }

    public String getKeyShowVol() {
        return keyShowVol;
    }

    public void setKeyShowVol(String keyShowVol) {
        this.keyShowVol = keyShowVol;
        PageNaviConfig.DEFAULT_KEY_SHOW_VOL = keyShowVol;
    }

    public String getKeyGuide() {
        return keyGuide;
    }

    public void setKeyGuide(String keyGuide) {
        this.keyGuide = keyGuide;
        PageNaviConfig.DEFAULT_KEY_GUIDE = keyGuide;
    }

    public String getKeyIdFlag() {
        return keyIdFlag;
    }

    public void setKeyIdFlag(String keyIdFlag) {
        this.keyIdFlag = keyIdFlag;
        PageNaviConfig.DEFAULT_KEY_ID_FLAG = keyIdFlag;
    }

    public int getVarPageDefaultVol() {
        return varPageDefaultVol;
    }

    public void setVarPageDefaultVol(int varPageDefaultVol) {
        this.varPageDefaultVol = varPageDefaultVol;
        PageNaviConfig.DEFAULT_VAR_PAGE_DEFAULT_VOL = varPageDefaultVol;
    }

    public int getVarPageMaxVol() {
        return varPageMaxVol;
    }

    public void setVarPageMaxVol(int varPageMaxVol) {
        this.varPageMaxVol = varPageMaxVol;
        PageNaviConfig.DEFAULT_VAR_PAGE_MAX_VOL = varPageMaxVol;
    }

    public boolean isVarClientSetVolEnable() {
        return varClientSetVolEnable;
    }

    public void setVarClientSetVolEnable(boolean varClientSetVolEnable) {
        this.varClientSetVolEnable = varClientSetVolEnable;
        PageNaviConfig.DEFAULT_VAR_CLIENT_SET_VOL_ENABLE = varClientSetVolEnable;
    }
}
