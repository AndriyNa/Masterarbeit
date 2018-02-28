@echo off
cd "C:\Program Files (x86)\mosquitto"
start mosquitto.exe

cd "C:\"
start node-red

cd "C:\Program Files (x86)\Apache Software Foundation\CouchDB\bin"
start couchdb.bat

exit