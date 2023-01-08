package org.anyline.web.listener;

import org.anyline.data.param.ConfigStore;

import javax.servlet.http.HttpServletRequest;

public interface ControllerListener {
    public ConfigStore after(HttpServletRequest request, ConfigStore configs);
}
