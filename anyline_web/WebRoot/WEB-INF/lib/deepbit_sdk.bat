D:
cd D:\jar_bat
del deepbit_sdk.jar

rmdir  /s/q deepbit_sdk\cn
mkdir deepbit_sdk\cn
xcopy D:\develop\git\deepbit\deepbit_sdk\WebRoot\WEB-INF\classes\cn deepbit_sdk\cn /s /e


rmdir  /s/q deepbit_sdk\META-INF
mkdir deepbit_sdk\META-INF
xcopy D:\develop\git\deepbit\deepbit_sdk\WebRoot\WEB-INF\tld deepbit_sdk\META-INF /s /e


cd deepbit_sdk
jar -cvf ..\deepbit_sdk.jar *
cd ..
rmdir  /s/q deepbit_sdk