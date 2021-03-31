/*
package org.anyline.tencent.cos.tag;


import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.web.tag.BaseBodyTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.util.Map;

public class COSConfig extends BaseBodyTag {
    private static final long serialVersionUID = 1L;
    private boolean debug = false;
    private int expire = 0;
    private String dir = "";
    private String key = "default";
    private String var = "al.config.oss.aliyun";
    private String allow = "*";

    public int doEndTag() throws JspException {
        try{
            COSUtil util = COSUtil.getInstance(key);
            if(BasicUtil.isEmpty(dir)){
                dir = util.getConfig().DIR;
            }
            if(BasicUtil.isNotEmpty(dir)){
                String yyyy = DateUtil.format("yyyy");
                String yy = DateUtil.format("yy");
                String mm = DateUtil.format("MM");
                String dd = DateUtil.format("dd");
                dir = dir.replace("{yyyy}", yyyy);
                dir = dir.replace("{yy}", yy);
                dir = dir.replace("{MM}", mm);
                dir = dir.replace("{mm}", mm);
                dir = dir.replace("{dd}", dd);
                dir = dir.replace("{y}", yyyy);
                dir = dir.replace("{m}", mm);
                dir = dir.replace("{d}", dd);
            }
            Map<String,String> map = util.signature(dir, expire);
            if(BasicUtil.isEmpty(var)){
                var = "al.config.oss.aliyun";
            }
            StringBuffer script = new StringBuffer();
            script.append("<script>\n");
            script.append(var).append(" = ").append(BeanUtil.map2json(map)).append(";\n");
            script.append("</script>\n");
            JspWriter out = pageContext.getOut();
            out.println(script);
        } catch (Exception e) {
            e.printStackTrace();
            if(ConfigTable.isDebug() && log.isWarnEnabled()){
                e.printStackTrace();
            }
        } finally {
            release();
        }
        return EVAL_PAGE;
    }
    public boolean isDebug() {
        return debug;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public String getKey() {
        return key;
    }

    public int getExpire() {
        return expire;
    }
    public void setExpire(int expire) {
        this.expire = expire;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getDir() {
        return dir;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    public String getVar() {
        return var;
    }
    public void setVar(String var) {
        this.var = var;
    }

}
*/
