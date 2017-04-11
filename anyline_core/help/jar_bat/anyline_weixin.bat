D:
cd D:\jar_bat
del anyline_weixin.jar

rmdir  /s/q anyline_weixin\org
mkdir anyline_weixin\org
xcopy D:\develop\git\anyline\anyline_weixin\WebRoot\WEB-INF\classes\org anyline_weixin\org /s /e


rmdir  /s/q anyline_weixin\META-INF
mkdir anyline_weixin\META-INF
xcopy D:\develop\git\anyline\anyline_weixin\WebRoot\WEB-INF\tld anyline_weixin\META-INF /s /e


cd anyline_weixin
jar -cvf ..\anyline_weixin.jar *
cd ..
