# Machine 1 writes then Machine 2 & 3 request to write while Machine 1 writes
bash scripts/baseTest.sh 25000\
    test3/m1\
    test3/m2\
    test3/m3
