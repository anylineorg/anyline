D:
cd D:\jar_bat
del anyline_sms.jar

rmdir  /s/q anyline_sms\org
mkdir anyline_sms\org
xcopy D:\develop\git\anyline\anyline_sms\bin\org anyline_sms\org /s /e


cd anyline_sms
jar -cvf ..\anyline_sms.jar *
cd ..
