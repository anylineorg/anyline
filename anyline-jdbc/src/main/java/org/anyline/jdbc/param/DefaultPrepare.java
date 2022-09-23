package org.anyline.jdbc.param;

public class DefaultPrepare {
    public static String[] split(String src, String split){
        if(null == src){
            return null;
        }
        if(null == split){
            if(src.contains("|")) {
                split = "|";
            }else{
                split = ",";
            }
        }

        if("|".equals(split)){
            split = "\\|";
        }
        return src.split(split);
    }
    public static String[] split(String src){
        return split(src, null);
    }
}
