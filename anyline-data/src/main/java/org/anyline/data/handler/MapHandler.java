package org.anyline.data.handler;

import java.util.Map;

public interface MapHandler extends StreamHandler{

    boolean read(Map<String,Object> map);
}
