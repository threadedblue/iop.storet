#!/usr/bin/env bash

DIR=`pwd`
cd ../iox.utils
gradle clean install
cd $DIR
gradle clean bundle