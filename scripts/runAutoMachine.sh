source scripts/setupLog.sh
logFile=$logDir/machine_$fDate#$1.log
touch $logFile
[ -z "$(command -v code)" ] || code $logFile
mvn exec:java -Dexec.mainClass=machine.MachineMain -e < /dev/stdin 2> $logFile
