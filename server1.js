var os = require('os-utils');
/*
os.cpuUsage(function(v){
    console.log( 'CPU Usage (%): ' + v );
});
*/

//console.log( 'totalMemory: ' + randomizedSensorVal); 

var mqtt    = require('mqtt')
  , host = 'localhost' // or localhost
  , client = mqtt.connect();

console.log('starting');
setInterval(publishSomeValues, 10000);
publishSomeValues();

function publishSomeValues(){
	var randomizedSensorVal = randomIntFromInterval(1, Math.floor((os.totalmem()/100)));
	console.log( 'totalMemory: ' + randomizedSensorVal);
	client.publish('Cloos3/TempSensor', randomizedSensorVal.toString());
}

function randomIntFromInterval(min,max)
{
    return Math.floor(Math.random()*(max-min+1)+min);
}


client.subscribe('situations/#');
client.on('message', function (topic, message) {

	var JSONParseObject = JSON.parse(message.toString());
	var JSONObject = JSON.stringify(JSONParseObject);

// DEBUG: Printing data ----------------------
//	var jsonfile = require('jsonfile')
//	var file = ''
//	var obj = JSONObject
//	jsonfile.writeFile(file, obj, function (err) {
//	})
//--------------------------------------------

   
var http = require('http')

var body = JSONObject;

var request = new http.ClientRequest({
    hostname: "localhost",
    port: 5984,
    path: "/situations",
    method: "POST",
    headers: {
        "Content-Type": "application/json",
        "Content-Length": Buffer.byteLength(body)
    }
})

request.end(body);

request.on('response', function (response) {
  console.log('STATUS: ' + response.statusCode);
  console.log('HEADERS: ' + JSON.stringify(response.headers));
  response.setEncoding('utf8');
  response.on('data', function (chunk) {
    console.log('BODY: ' + chunk);
  });
});
});
