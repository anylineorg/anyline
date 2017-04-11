D:
cd D:\jar_bat
del anyline_easemob.jar

rmdir  /s/q anyline_easemob\org
mkdir anyline_easemob\org
xcopy D:\develop\git\anyline\anyline_easemob\bin\classes\org anyline_easemob\org /s /e


cd anyline_easemob
jar -cvf ..\anyline_easemob.jar *
cd ..
