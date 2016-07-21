E:
cd E:\jar_bat
del anyline_struts.jar

rmdir  /s/q anyline_struts\org
mkdir anyline_struts\org
xcopy E:\develop\git\osc\anyline\anyline_struts\bin\org anyline_struts\org /s /e
      
del anyline_struts\struts-anyline.xml 
xcopy E:\develop\web\anyline_core\WEB-INF\classes\struts-anyline.xml anyline_struts /s /e

cd anyline_struts
jar -cvf ..\anyline_struts.jar *
cd ..
