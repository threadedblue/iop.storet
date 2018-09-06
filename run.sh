#!/usr/bin/env bash

DIR=`pwd`
NAME="xml2xpath"
cd $NAME
time java -jar $NAME-0.0.1.jar -i /ccd/ccd1 -t ccddd -fs hdfs://haz00:9000 -l accumulo -zk haz00:2181 -ow -r
cd $DIR