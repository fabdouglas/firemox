#!/bin/bash
PROGRAM_DIR=`dirname "$0"`
PROGRAM_DIR=`cd "$PROGRAM_DIR"; pwd`
if [ "$(echo ${PROGRAM_DIR}/*.jar)" = "${PROGRAM_DIR}/*.jar" ]; then
	echo "You seem to have set an invalid PROGRAM_DIR, unable to continue!"
	exit 1
elif [ ! -f starter.jar ]; then
	echo "Unable to locate starter.jar in $PROGRAM_DIR, aborting!"
	exit 1
fi
java -jar starter.jar -oracle2xml -f ~/Allsets-2007-03-11.txt -d ~/out