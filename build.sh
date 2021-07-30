#!/bin/bash

export CLASSPATH=./src
if [ ! -d classes ];then mkdir classes;fi
javac -encoding UTF8 -d classes/ src/burp/burpython/core/protocol/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/core/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/UI/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/*.java
javac -encoding UTF8 -d classes/ src/burp/*.java
cp -r -f src/burp/burpython/resource classes/burp/burpython/
cd classes && jar cvf ../burpython.jar burp
