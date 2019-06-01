#!/bin/sh

# This script starts the database server and runs a series of operation against server implementation.
# If the server is implemented correctly, the output (both return values and JSON block) will match the expected outcome.
# Note that this script does not compare the output value, nor does it compare the JSON file with the example JSON.

# Please start this script in a clean environment; i.e. the server is not running, and the data dir is empty.

if [ ! -f "config.json" ]; then
	echo "config.json not in working directory. Trying to go to parent directory..."
	cd ../
fi
if [ ! -f "config.json" ]; then
  	echo "!! Error: config.json not found. Please run this script from the project root directory (e.g. ./example/test_run.sh)."
	exit -1
fi

echo "Testrun starting..."

./start.sh &
PID=$!
sleep 1

echo "Step 1: Initialize account"
for I in `seq 0 9`; do
	java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient PUT TEST---$I 10
done
echo "Check value: expecting value=10"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---5

echo "Check LogLength: expecting value=10"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient LOGLENGTH

echo "Step 2: Try deposit"
for I in `seq 0 9`; do
	java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient DEPOSIT TEST---$I 5
done
echo "Check value: expecting value=15"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---5

echo "Step 3: Try transfer"
for I in `seq 0 9`; do
	java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient TRANSFER TEST---$I TEST---TX 10
done
echo "Check value: expecting value=100"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---TX

echo "Step 4: Try transfer again"
for I in `seq 0 9`; do
	java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient TRANSFER TEST---TX TEST---$I 5
done
echo "Check value: expecting value=50"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---TX

echo "Step 5: Try withdraw"
for I in `seq 0 9`; do
	java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient WITHDRAW TEST---$I 5
done
echo "Check value: expecting value=5"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---2

echo "Step 5: Try overdraft"
for I in `seq 0 9`; do
	java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient WITHDRAW TEST---$I 10
done
echo "Check value: expecting value=5"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---2

echo "Check LogLength: expecting value<=50"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient LOGLENGTH

#echo "Try Killing the server and restart..."
#echo "Sleep for a while, waiting for hashmap reconstruction..."
#sleep 10;

echo "Check value again: expecting value=5"
java -cp target/blockdb-1.0-SNAPSHOT.jar iiis.systems.os.blockdb.DatabaseClient GET TEST---2


echo "Test completed. Please verify JSON block output with example_1.json ."

kill $PID
