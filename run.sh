#!/usr/bin/env bash

DIR=`pwd`
NAME="ccd.bfs"
cd $NAME
time java -jar $NAME-0.0.1.jar -i /ccd -o ccdRT -fs hdfs://haz00:9000 -l accumulo -zk haz00:2181 -ow
cd $DIR