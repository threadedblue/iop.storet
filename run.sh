#!/usr/bin/env bash

DIR=`pwd`
NAME="xml2xpath"
cp -r $NAME ~/
cd ~/$NAME
java -jar $NAME-0.0.1.jar -i /ccd/ccd1/ccd-0 -o /ccd/ccd1/xpath-0 -ow
cd $DIR