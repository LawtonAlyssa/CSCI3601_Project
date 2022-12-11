source scripts/setupLog.sh
logFile=$logDir/machine_$fDate.log
touch $logFile
[ -z "$(command -v code)" ] || code $logFile
mvn exec:java -Dexec.mainClass=machine.MachineMain -e 3<&0 2> $logFile
