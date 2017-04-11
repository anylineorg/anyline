D:
cd D:\jar_bat
del anyline_jpush.jar

rmdir  /s/q anyline_jpush\org
mkdir anyline_jpush\org
xcopy D:\develop\git\anyline\anyline_jpush\bin\org anyline_jpush\org /s /e


cd anyline_jpush
jar -cvf ..\anyline_jpush.jar *
cd ..
