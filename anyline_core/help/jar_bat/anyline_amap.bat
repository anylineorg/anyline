D:
cd D:\jar_bat
del anyline_amap.jar

rmdir  /s/q anyline_amap\org
mkdir anyline_amap\org
xcopy D:\develop\git\anyline\anyline_amap\bin\org anyline_amap\org /s /e


cd anyline_amap
jar -cvf ..\anyline_amap.jar *
cd ..
