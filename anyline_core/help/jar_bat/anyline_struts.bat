E:
cd E:\jar_bat
del anyline_struts.jar

rmdir  /s/q anyline_struts\org
mkdir anyline_struts\org
xcopy E:\develop\git\osc\anyline\anyline_struts\bin\org anyline_struts\org /s /e
      
cd anyline_struts
jar -cvf ..\anyline_struts.jar *
cd ..
