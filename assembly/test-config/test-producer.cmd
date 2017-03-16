title producer

set CLASSPATH=..\..\idmm2-test\target\classes;..\..\idmm2-client-api\target\classes;lib\*;



set JVM_OPTS=
set JVM_OPTS=%JVM_OPTS% -Duser.language=Zh -Duser.region=CN
set JVM_OPTS=%JVM_OPTS% -Ddefault.client.encoding=GBK
set JVM_OPTS=%JVM_OPTS% -Dfile.encoding=GBK

set BOOTUP=t.Producer


mkdir log

java %JVM_OPTS% %BOOTUP%

