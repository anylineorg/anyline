D:
cd D:\jar_bat
del anyline_mssql.jar

rmdir  /s/q anyline_mssql\org
mkdir anyline_mssql\org
xcopy D:\develop\git\anyline\anyline_mssql\bin\org anyline_mssql\org /s /e

cd anyline_mssql
jar -cvf ..\anyline_mssql.jar *
cd ..
