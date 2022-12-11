source scripts/setupLog.sh
rm -r $logDir
mkdir $logDir
logFile=$logDir/central_server_$fDate.log
touch $logFile
[ -z "$(command -v code)" ] || code $logFile
mvn exec:java -Dexec.mainClass=server.CentralServerMain -e 3<&0 2> $logFile

