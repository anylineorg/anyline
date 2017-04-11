D:
cd D:\jar_bat
del anyline_alipay.jar

rmdir  /s/q anyline_alipay\org
mkdir anyline_alipay\org
xcopy D:\develop\git\anyline\anyline_alipay\WebRoot\WEB-INF\classes\org anyline_alipay\org /s /e

cd anyline_alipay
jar -cvf ..\anyline_alipay.jar *
cd ..
