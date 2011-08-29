#!/bin/bash

#default values
port=8181

while getopts ":p:e:h:" optname
    do
        case "$optname" in
            "p")
               port="$OPTARG"
               ;;
            "e")
               profile="$OPTARG"
               ;;
            "h")
               history_path="$OPTARG"
               ;;
            "?")
               ;;
            ":")
               ;;
            *)
               ;;
        esac
    done

if [ -z "$profile" ]
then
    echo 'Usage: ./start-server.sh -e <profile> [-p <port>] [-h <zip_with_history>]'
    exit 1
else
    echo Using profile $profile
fi

mvn clean install

if [ "$?" -ne "0" ]; then
  exit 1
fi

mvn dependency:copy-dependencies -DoutputDirectory=target/lib -Duser.profile=$profile

test_classpath="./target/test-classes"

if [ "$?" -ne "0" ]; then
  exit 1
fi

path=$(echo `find target/lib/`  | sed 's/ /:/g')":$test_classpath"

#echo $path

if [ -n "$history_path" ];
    then
      echo 'Unzipping old test history';
      unzip -o $history_path || ( echo Could not unzip test history from $history_path; )
fi

export properties_location="$test_classpath"

args=' -Dtestclasspath='$path

java -Xdebug -Xmx128m -Xms128m -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n  -cp $path $args fitnesseMain.FitNesseMain -p $port -e 9 -d "$test_classpath" -l target/fitnesse

if [ -n "$history_path" ];
    then
      echo 'Zipping test history';
      zip $history_path -r $test_classpath/FitNesseRoot/files/testResults || ( echo Could not zip test history to $history_path; )
fi

echo 'Finished'
