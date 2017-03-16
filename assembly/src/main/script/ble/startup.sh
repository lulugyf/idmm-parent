BUSINESS_NAME=ble

## 工作目录
BIN_PATH=`dirname $0`
cd ${BIN_PATH}/../..
RUN_HOME=`pwd`

PS_ALIVE="ps -ef"
if [ `uname` == "SunOS" ];then
	PS_ALIVE="/usr/ucb/ps -auxww"
fi

if [ -f "${RUN_HOME}/log/${BUSINESS_NAME}.pid" ]; then
	LASTPID=`cat "${RUN_HOME}/log/${BUSINESS_NAME}.pid"`
	PROCNUM=`${PS_ALIVE}|grep ${LASTPID} |grep java| grep ${BUSINESS_NAME} |grep -v grep |wc -l`  
	if [ $PROCNUM -gt 0 ]; then
		echo "Already running!"
		exit 99
	fi
fi

## 加载环境变量
if [ -r "~/.profile" ];then
	. ~/.profile> /dev/null 2>&1
fi
if [ -r "~/.bash_profile" ];then
	. ~/.bash_profile> /dev/null 2>&1
	source ~/.bash_profile> /dev/null 2>&1
fi

CLASSPATH=${RUN_HOME}/config/ble
for JARFILE in ${RUN_HOME}/lib/*.jar
do
	CLASSPATH=${CLASSPATH}:${JARFILE}
done

## JVM启动项参数
## 语言环境，客户端字符集，文件字符集
JVM_OPTS="${JVM_OPTS} -D${BUSINESS_NAME}"
JVM_OPTS="${JVM_OPTS} -Duser.language=Zh -Duser.region=CN"
JVM_OPTS="${JVM_OPTS} -Ddefault.client.encoding=UTF-8"
JVM_OPTS="${JVM_OPTS} -Dfile.encoding=UTF-8"
#JVM_OPTS="${JVM_OPTS} -Xms8G -Xmx8G -Xss512k"
#JVM_OPTS="${JVM_OPTS} -XX:+UseParNewGC -XX:+UseConcMarkSweepGC"
#JVM_OPTS="${JVM_OPTS} -XX:+CMSClassUnloadingEnabled -XX:+CMSScavengeBeforeRemark"
JVM_OPTS="${JVM_OPTS} -XX:-OmitStackTraceInFastThrow"

## 启动类
BOOTUP=com.sitech.crmpd.idmm2.ble.BLEServ

# (set JAVACMD to "auto" for automatic detection)
JAVACMD="auto"

if [ -n "$JAVA_HOME"  ] ; then
	if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
		# IBM's JDK on AIX uses strange locations for the executables
		JAVACMD="$JAVA_HOME/jre/sh/java"
	else
		JAVACMD="$JAVA_HOME/bin/java"
	fi
fi

# Hm, we still do not know the location of the java binary
if [ ! -x "${JAVACMD}" ] ; then
		JAVACMD=`which java 2> /dev/null `
		if [ -z "${JAVACMD}" ] ; then
				JAVACMD=java
		fi
fi

# Stop here if no java installation is defined/found
if [ ! -x "${JAVACMD}" ] ; then
	echo "ERROR: Configuration variable JAVA_HOME or JAVACMD is not defined correctly."
	echo "       (JAVA_HOME='$JAVAHOME', JAVACMD='${JAVACMD}')"
	exit 4
fi


## 建立标准输出的目录，用于重定向输出
mkdir -p ${RUN_HOME}/log
## 判断下业务专属环境配置文件是否存在，不在的话，使用默认的环境配置文件
nohup ${JAVACMD} -classpath ${CLASSPATH} ${JVM_OPTS} ${JVM_PARAM} ${BOOTUP}>${RUN_HOME}/log/${BUSINESS_NAME}.console 2>&1 &

echo $! |tee ${RUN_HOME}/log/${BUSINESS_NAME}.pid
${PS_ALIVE} |grep `cat ${RUN_HOME}/log/${BUSINESS_NAME}.pid` |grep -v grep

