#!/usr/bin/env bash

DIR=`pwd`
NAME="xml2xpath"
cp -r $NAME ~/
cd ~/$NAME
java -jar $NAME-0.0.1.jar -i /ccd/ccd1/ccd-0 -t ccd -ow -fs hdfs://haz00:9000 -l acccumulo -zk haz00:2181 
cd $DIR