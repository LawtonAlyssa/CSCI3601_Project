source scripts/setupLog.sh
mvn exec:java -Dexec.mainClass=server.CentralServerMain -e 2> $logDir/central_server_$fDate.log
