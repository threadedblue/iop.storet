#!/usr/bin/env bash

DIR=`pwd`
NAME="xml2xpath"
cd $NAME
time java -jar $NAME-0.0.1.jar -i /ccd -o /ccdxpath -r -ow
cd $DIR