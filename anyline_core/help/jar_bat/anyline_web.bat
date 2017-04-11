D:
cd D:\jar_bat
del anyline_web.jar

rmdir  /s/q anyline_web\org
mkdir anyline_web\org
xcopy D:\develop\git\anyline\anyline_web\WebRoot\WEB-INF\classes\org anyline_web\org /s /e


rmdir  /s/q anyline_web\META-INF
mkdir anyline_web\META-INF
xcopy D:\develop\git\anyline\anyline_web\WebRoot\WEB-INF\tld anyline_web\META-INF /s /e


cd anyline_web
jar -cvf ..\anyline_web.jar *
cd ..
