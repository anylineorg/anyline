package org.anyline.spider;

import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class SpiderClient {
    private Logger log = LoggerFactory.getLogger(SpiderClient.class);
    private static final String JS_ENGINE_NAME= "JavaScript";
    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final ScriptEngine engine = manager.getEngineByName(JS_ENGINE_NAME);
    private File home;
    /**
     * 导入js脚本
     * @param charset 编码
     * @param files js脚本
     */
    public void script(Charset charset, List<File> files){
        for(File file:files){
            if(file.isDirectory()){
                List<File> items = FileUtil.getAllChildrenFile(file,".js");
                script(charset, items);
            }else {
                try {
                    log.warn("[script][file:{}]", file.getAbsolutePath());
                    Reader reader = Files.newBufferedReader(file.toPath(), charset);
                    engine.eval(reader);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void script(Charset charset, File ... files){
        script(charset, BeanUtil.array2list(files));
    }
    public void script(Charset charset, String ... files){
        for(String file:files){
            File f = null;
            if(null == home){
                f = new File(file);
            }else{
                f = new File(home, file);
            }
            script(charset, f);
        }
    }
    public void script(File ... files){
        script(Charset.defaultCharset(), files);
    }
    public void script(String ... files){
        script(Charset.defaultCharset(), files);
    }

    public void script(String charset, List<File> files){
        Charset c = null;
        if(null == charset){
            c = Charset.defaultCharset();
        }else{
            c = Charset.forName(charset);
        }
        script(c, files);
    }


    public void script(String charset, File ... files){
        script(charset, BeanUtil.array2list(files));
    }


    /**
     * 调用js.function
     * @param function function
     * @param params params
     * @return Object
     * @throws Exception 异常 Exception
     */
    public Object invoke(String function, Object ... params) throws Exception{
        Invocable jsInvoke = (Invocable)engine;
        Object obj = jsInvoke.invokeFunction(function, params);
        return obj;
    }

    public ScriptEngine getEngine() {
        return this.engine;
    }

    public File getHome() {
        return this.home;
    }

    public void setHome(final File home) {
        this.home = home;
    }
}
