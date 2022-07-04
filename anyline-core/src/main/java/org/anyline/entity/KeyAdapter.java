package org.anyline.entity;

public interface KeyAdapter {
    public static enum KEY_CASE{
        CONFIG				{public String getCode(){return "CONFIG";} 			public String getName(){return "按配置文件";}},
        SRC					{public String getCode(){return "SRC";} 			public String getName(){return "不转换";}},
        UPPER				{public String getCode(){return "UPPER";} 			public String getName(){return "强制大写";}},
        LOWER				{public String getCode(){return "LOWER";} 			public String getName(){return "强制小写";}},
        //以下规则取消
        //下/中划线转成驼峰
        Camel 				{public String getCode(){return "Camel";} 			public String getName(){return "大驼峰";}},
        camel 				{public String getCode(){return "camel";} 			public String getName(){return "小驼峰";}},
        //bean驼峰属性转下划线
        CAMEL_CONFIG		{public String getCode(){return "CAMEL_CONFIG";} 	public String getName(){return "转下划线后按配置文件转换大小写";}},
        CAMEL_SRC			{public String getCode(){return "CAMEL_SRC";} 		public String getName(){return "转下划线后不转换大小写";}},
        CAMEL_UPPER			{public String getCode(){return "CAMEL_UPPER";} 	public String getName(){return "转下划线后强制大写";}},
        CAMEL_LOWER			{public String getCode(){return "CAMEL_LOWER";} 	public String getName(){return "转下划线后强制小写";}},

        AUTO				{public String getCode(){return "AUTO";} 			public String getName(){return "自动识别";}};

        public abstract String getName();
        public abstract String getCode();
    }
    public String key(String key);
}
