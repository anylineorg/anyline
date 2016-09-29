D:
cd D:\jar_bat
del anyline_struts.jar

rmdir  /s/q anyline_struts\org
mkdir anyline_struts\org
xcopy D:\develop\git\anyline\anyline_struts\bin\org anyline_struts\org /s /e

del anyline_struts\struts-anyline.xml
xcopy  D:\develop\git\anyline\anyline_struts\bin\struts-anyline.xml anyline_struts /s /e

cd anyline_struts
jar -cvf ..\anyline_struts.jar *
cd ..
