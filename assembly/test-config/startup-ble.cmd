title IDMM2-BLE

set BUSINESS_NAME=ble
set CLASSPATH=ble;..\..\assembly\target\classes;..\..\idmm3-ble\target\classes;..\..\idmm3-blestore-jdbc\target\classes;lib\*;



set JVM_OPTS=-D%BUSINESS_NAME%
set JVM_OPTS=%JVM_OPTS% -Duser.language=Zh -Duser.region=CN
set JVM_OPTS=%JVM_OPTS% -Ddefault.client.encoding=GBK
set JVM_OPTS=%JVM_OPTS% -Dfile.encoding=GBK

set BOOTUP=com.sitech.crmpd.idmm2.ble.BLEServ


mkdir log

java %JVM_OPTS% %BOOTUP%

