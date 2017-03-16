cp=./test-classes:./t.jar:./idmm2-broker-0.0.1-SNAPSHOT.jar
for f in ./dependency/*.jar
do
cp=$cp:$f
done
export CLASSPATH=$cp

java com.sitech.crmpd.idmm2.ble.BLEServ
