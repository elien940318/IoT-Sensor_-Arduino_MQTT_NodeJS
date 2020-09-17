var express = require('express');
var router = express.Router();

var mongoDB=require("mongodb").MongoClient;
var url="mongodb://127.0.0.1:27017/iot";
var dbObj=null;

mongoDB.connect(url, function(err, db){
	dbObj=db;
	console.log("DB Connect.......");
});

var mqtt=require("mqtt"); 
var client=mqtt.connect("mqtt://192.168.10.5");

/* 안드로이드에서 POST로 MQTT에 LED 상태 publish 해준다. */
router.post('/led/:flag', function(req, res, next) {
   res.set('Content-Type', 'text/json');	
   if(req.params.flag=="on"){
	   // MQTT->led : 1
	   client.publish("led", '1');
	   res.send(JSON.stringify({led:'on'}));
   }else{
	   client.publish("led", '2');
	   res.send(JSON.stringify({led:'off'}));
   }
});

/* 안드로이드로 GET 방식 mongoDB의 데이터 JSON형식으로 ... */
// 안드로이드상에서 sensorLog 필요시 실행되는 부분.
router.get('/:device/:sensor', function(req, res, next){	
	var sensorLogs=null;
	if(req.params.sensor="sensorlog")
		sensorLogs=dbObj.collection('sensorlog');	
	
    sensorLogs.find({}).limit(10).sort({created_at:-1}).toArray(function(err, results){
        if(err) res.send(JSON.stringify(err));		            	 
        else res.send(JSON.stringify(results));
   });		
});
module.exports = router;

