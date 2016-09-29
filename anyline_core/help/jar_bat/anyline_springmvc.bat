D:
cd D:\jar_bat
del anyline_springmvc.jar

rmdir  /s/q anyline_springmvc\org
mkdir anyline_springmvc\org
xcopy D:\develop\git\anyline\anyline_springmvc\bin\org anyline_springmvc\org /s /e

cd anyline_springmvc
jar -cvf ..\anyline_springmvc.jar *
cd ..
