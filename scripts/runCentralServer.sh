source scripts/setupLog.sh
rm -r $logDir
mkdir $logDir
logFile=$logDir/central_server_$fDate.log
touch $logFile
[ -z "$(command -v code)" ] || code $logFile
mvn exec:java -Dexec.mainClass=server.CentralServerMain -e < /dev/stdin 2> $logFile

