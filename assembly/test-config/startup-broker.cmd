title IDMM2-BROKER

set BUSINESS_NAME=broker
set CLASSPATH=broker;..\..\assembly\target\classes;..\..\idmm3-broker\target\classes;..\..\idmm3-jdbc-repository\target\classes;lib\*;



set JVM_OPTS=-D%BUSINESS_NAME%
set JVM_OPTS=%JVM_OPTS% -Duser.language=Zh -Duser.region=CN
set JVM_OPTS=%JVM_OPTS% -Ddefault.client.encoding=GBK
set JVM_OPTS=%JVM_OPTS% -Dfile.encoding=GBK

set BOOTUP=com.sitech.crmpd.idmm2.broker.App


mkdir log

java %JVM_OPTS% %BOOTUP%

