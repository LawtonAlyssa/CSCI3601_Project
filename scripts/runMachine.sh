source scripts/setupLog.sh
mvn exec:java -Dexec.mainClass=machine.MachineMain -e 2> $logDir/machine_$fDate.log