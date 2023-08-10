package org.anyline.web.listener;

import org.anyline.data.param.ConfigStore;

import javax.servlet.http.HttpServletRequest;

public interface ControllerListener {
    /**
     * 封装完查询条件后调用
     * @param request rquest
     * @param configs 过滤条件及相关配置
     * @return ConfigStore
     */
    public ConfigStore after(HttpServletRequest request, ConfigStore configs);
}
