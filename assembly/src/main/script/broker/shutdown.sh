BUSINESS_NAME=broker

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
	echo "Before kill ${LASTPID} ..."
	${PS_ALIVE} |grep ${LASTPID} |grep java| grep ${BUSINESS_NAME} |grep -v grep
	PROCNUM=`${PS_ALIVE}|grep ${LASTPID} |grep java| grep ${BUSINESS_NAME} |grep -v grep |wc -l`  
	if [ $PROCNUM -gt 0 ]; then
		kill $LASTPID
	fi
	sleep 2
	echo "After kill ${LASTPID} ..."
	${PS_ALIVE} |grep ${LASTPID} |grep java| grep ${BUSINESS_NAME} |grep -v "ps -ef" |grep -v grep
fi
