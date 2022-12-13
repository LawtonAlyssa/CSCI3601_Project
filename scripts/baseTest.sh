serverRunTime=$1
shift
bash scripts/runCentralServer.sh $serverRunTime &
serverpid=$!
sleep 3
i=0
for inputFile in "$@"
do
    # echo $i
    i=$((i+1))
    bash scripts/runMachine.sh $i < input_files/$inputFile &
    pids[${i}]=$!
done

wait $serverpid
echo Server terminated

for pid in ${pids[*]}; do
    wait $pid
done

echo All machines terminated
