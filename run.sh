#!/usr/bin/env bash

DIR=`pwd`
NAME="xml2xpath"
STEM="ccd0"

cd $NAME
time java -jar $NAME-0.0.1.jar -i "/$STEM" -o "/ccdxpath/$STEM" -r -ow
cd $DIR

COUNT="500"
cd $NAME
#time java -jar $NAME-0.0.1.jar -i "/ccd500/$STEM/$COUNT" -o "/ccdxpath/$STEM/$COUNT" -r -ow
cd $DIR
COUNT="1000"

cd $NAME
#time java -jar $NAME-0.0.1.jar -i "/ccd500/$STEM/$COUNT" -o "/ccdxpath/$STEM/$COUNT" -r -ow
cd $DIR

COUNT="1500"
cd $NAME
#time java -jar $NAME-0.0.1.jar -i "/ccd500/$STEM/$COUNT" -o "/ccdxpath/$STEM/$COUNT" -r -ow
cd $DIR

COUNT="2000"
cd $NAME
#time java -jar $NAME-0.0.1.jar -i "/ccd500/$STEM/$COUNT" -o "/ccdxpath/$STEM/$COUNT" -r -ow
cd $DIR