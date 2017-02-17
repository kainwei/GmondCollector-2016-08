#! /bin/sh
bin=`dirname "$0"`
bin=`cd "$bin"; pwd`
base=`cd "$bin/../"; pwd`
conf="$base/conf"
export JAVA_HOME=/usr/local/jdk1.7.0_67/
export JRE_HOME=/usr/local/jdk1.7.0_67/jre 
java -version
CLASSPATH="."
for j in `ls $base/*.jar`
do
  CLASSPATH="$CLASSPATH:$j"
done
for j in `ls $base/lib/*.jar`
do
  CLASSPATH="$CLASSPATH:$j"
done
CLASSPATH="$conf:$CLASSPATH"
export CLASSPATH=$CLASSPATH
JAVA_HEAP_MAX="-Xmx1024m"
java -classpath $CLASSPATH $JAVA_HEAP_MAX com.sina.data.bigmonitor.cli.GmondCollectorManager $1
