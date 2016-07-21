e:
cd e:\jar_bat
del inmvc.jar

rmdir  /s/q inmvc\com
mkdir inmvc\com
xcopy D:\tomcat8\web\inmvc\WEB-INF\classes\com inmvc\com /s /e


rmdir  /s/q inmvc\META-INF
mkdir inmvc\META-INF
xcopy D:\tomcat8\web\inmvc\WEB-INF\tld inmvc\META-INF /s /e

del inmvc\struts-inmvc.xml 
xcopy D:\tomcat8\web\inmvc\WEB-INF\classes\struts\struts-inmvc.xml inmvc /s /e

cd inmvc
jar -cvf ..\inmvc.jar *
cd ..
