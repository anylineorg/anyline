package org.anyline.web.util;


import javax.servlet.http.HttpServletRequest;

public interface EntityListener {

    public void init(HttpServletRequest request, Object entity);
}
