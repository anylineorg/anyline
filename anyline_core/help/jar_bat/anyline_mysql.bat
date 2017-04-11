D:
cd D:\jar_bat
del anyline_mysql.jar

rmdir  /s/q anyline_mysql\org
mkdir anyline_mysql\org
xcopy D:\develop\git\anyline\anyline_mysql\bin\org anyline_mysql\org /s /e


cd anyline_mysql
jar -cvf ..\anyline_mysql.jar *
cd ..
