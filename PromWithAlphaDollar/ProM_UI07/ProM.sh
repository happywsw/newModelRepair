#!/bin/bash

CLASSPATH="$CLASSPATH:./lib/framework/ProM.jar"
CLASSPATH="$CLASSPATH:./lib/framework/att.jar"
CLASSPATH="$CLASSPATH:./lib/framework/MXMLib.jar"
CLASSPATH="$CLASSPATH:./lib/framework/java_cup.jar"
CLASSPATH="$CLASSPATH:./lib/framework/slickerbox0.5.jar"
CLASSPATH="$CLASSPATH:./lib/framework/GantzGraf-0.9.jar"

export CLASSPATH
java -classpath "$CLASSPATH" -Xmx1500M -Djava.library.path=./lib/external org.processmining.ProM
