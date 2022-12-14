source scripts/setupLog.sh
rm -r $logDir
mkdir $logDir
logFile=$logDir/central_server_$fDate.log
touch $logFile
[ -z "$(command -v code)" ] || code $logFile
serverRunTime=$1
sed -e "s/SERVER_RUN_TIME/$serverRunTime/g" input_files/exit.txt > input_files/serverExit.txt
mvn exec:java -Dexec.mainClass=server.CentralServerMain -e < input_files/serverExit.txt 2> $logFile

