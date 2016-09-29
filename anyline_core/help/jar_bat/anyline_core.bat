D:
cd D:\jar_bat
del anyline_core.jar

rmdir  /s/q anyline_core\org
mkdir anyline_core\org
xcopy D:\develop\web\anyline_core\WEB-INF\classes\org anyline_core\org /s /e


rmdir  /s/q anyline_core\META-INF
mkdir anyline_core\META-INF
xcopy D:\develop\web\anyline_core\WEB-INF\tld anyline_core\META-INF /s /e

cd anyline_core
jar -cvf ..\anyline_core.jar *
cd ..
