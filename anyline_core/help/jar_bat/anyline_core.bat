D:
cd D:\jar_bat
del anyline_core.jar

rmdir  /s/q anyline_core\org
mkdir anyline_core\org
xcopy D:\develop\git\anyline\anyline_core\bin\org anyline_core\org /s /e


cd anyline_core
jar -cvf ..\anyline_core.jar *
cd ..
