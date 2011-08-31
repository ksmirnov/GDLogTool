#!/bin/bash

#default values
port=8181

while getopts ":p:" optname
    do
        case "$optname" in
            "p")
               port="$OPTARG"
               ;;
            "?")
               ;;
            ":")
               ;;
            *)
               ;;
        esac
    done

mvn clean install

if [ "$?" -ne "0" ]; then
  exit 1
fi

mvn dependency:copy-dependencies -DoutputDirectory=target/lib

test_classpath="./target/classes"

if [ "$?" -ne "0" ]; then
  exit 1
fi

path='./'$(echo `find target/lib/`  | sed 's/ /:.\//g')":$test_classpath"

#echo $path

export properties_location="$test_classpath"

args=' -Dtestclasspath='$path

#echo "java -Xdebug -Xmx128m -Xms128m -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n  -cp $path $args fitnesseMain.FitNesseMain -p $port -e 9 -d "$test_classpath" -l target/fitnesse"

java -Xdebug -Xmx128m -Xms128m -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n  -cp $path $args fitnesseMain.FitNesseMain -p $port -e 9 -d "$test_classpath" -l target/fitnesse

echo 'Finished'
