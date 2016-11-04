boardingDistance=25;





var AV = require('leanengine');
var AV_Trace = AV.Object.extend('Trace');
var AV_ibeaconLog = AV.Object.extend('ibeaconLog');
var AV_faceLog = AV.Object.extend('faceLog');
var AV_people = AV.Object.extend('People');
var AV_routes = AV.Object.extend('Routes');
var AV_Device = AV.Object.extend('Device');


peopleDB={
	'测试1':'57dde3702e958a005466aca4',
	'测试2':'57d341d45bbb50005bb32670',
	'测试3':'574afdbe1532bc00606d2720',
	'测试4':'574afcd6a341310059177d75',
	'测试5':'574afda871cfe4006be935c6',
	'测试6':'574afde871cfe4005ea81eca'
}

peopleDB2={
	'57dde3702e958a005466aca4':'测试1',
	'57d341d45bbb50005bb32670':'测试2',
	'574afdbe1532bc00606d2720':'测试3',
	'574afcd6a341310059177d75':'测试4',
	'574afda871cfe4006be935c6':'测试5',
	'574afde871cfe4005ea81eca':'测试6'
}

cellphone={
	'57dde3702e958a005466aca4':'13601277159',
	'57d341d45bbb50005bb32670':'13001051585',
	'574afdbe1532bc00606d2720':'15810397548',
	'574afcd6a341310059177d75':'15210118019',
	'574afda871cfe4006be935c6':'18600024785',
	'574afde871cfe4005ea81eca':'18010126855'
}

//Override
cellphone={
	'57dde3702e958a005466aca4':'15801464739',
	'57d341d45bbb50005bb32670':'15801464739',
	'574afdbe1532bc00606d2720':'15801464739',
	'574afcd6a341310059177d75':'15801464739',
	'574afda871cfe4006be935c6':'15801464739',
	'574afde871cfe4005ea81eca':'15801464739'
}


beacon2people={
	'b5b182c7-eab1-4988-aa99-b5c1517008d9,424,1001': '57dde3702e958a005466aca4', //1
	'b5b182c7-eab1-4988-aa99-b5c1517008d9,424,1002': '57d341d45bbb50005bb32670', //2
	'b5b182c7-eab1-4988-aa99-b5c1517008d9,424,1003': '574afdbe1532bc00606d2720', //3
	'b5b182c7-eab1-4988-aa99-b5c1517008d9,424,1004': '574afcd6a341310059177d75', //4
	'b5b182c7-eab1-4988-aa99-b5c1517008d9,424,1005': '574afda871cfe4006be935c6', //5
	'b5b182c7-eab1-4988-aa99-b5c1517008d9,424,1006': '574afde871cfe4005ea81eca', //6
}

instId = {
	'78c5a28674458dc59cdba4b9789234fe':'busDoor',
	'ffa2a169e856c4d2e13176f5617b4424':'schoolGate'
}


instId_face='c2a77575c2a610c50ca9029abb318056';

routeId='5747dc45c26a38006ba39b4d';
	
AV.Cloud.define('hello', function(request, response) {
  response.success('Hello world!');
});

AV.Cloud.define('md5',function(request,response) {
	var md5sum=md5(request.params["string"]);
	
	response.success(md5sum);
});


AV.Cloud.define('15minute', function(request, response) {
	//Get Start timestamp
	var query=new AV.Query(AV_routes);
	
	query.get(routeId).then(function(route){
		current=new Date();
		console.log(current.getTime());
		console.log(route.get('originTime').getTime());
		sendtime=route.get('originTime').getTime()-current.getTime()-15*60*1000;
		console.log('15minute: '+sendtime);
		if ((sendtime+60000)>0&&sendtime<0) ReadyToBoard();
	},function(err) {
		console.log(err);
	});
	
	response.success('aha');
});


AV.Cloud.define('TracePoints', function(request,response) {
	var routeStartString="2016-05-30 7:20";
	var routeId='5747dad8c26a38006ba38f22';
	
	rawResult=[];
	procRes=[];

	mini(routeId,routeStartString,response);
	
});

var rawResult=[];
var procRes=[];

function mini(routeId, routeStartString,response) {
		console.log(rawResult.length);
		
		var routeStart=new Date(routeStartString);
		
		var query=new AV.Query(AV_Trace);
		query.addAscending('createdAt');
		//query.greaterThan('createdAt', routeStart);
		query.equalTo('installationId','a22ab7855db1ec45e62f0c06705340ae')
		query.skip(rawResult.length);
		query.find().then(function(rm) {
			rawResult=rawResult.concat(rm);
			if(rm.length!==0)
				mini();
			else {
				number=40;
				console.log('a');
				if(rawResult.length<number)
					procRes=rawResult;
				else {
					var interval=Math.floor((rawResult.length-1)/(number-1));
					procRes=[];
					console.log(rawResult.length);
					console.log(interval);
					console.log(number);
					console.log('sssss');
					for(var i=0; i<rawResult.length; i=i+interval) {
						procRes.push(rawResult[i]);
						console.log(i);
					}
					response.success(procRes);
				}
			}
		}, function(err) {console.log(err)});
}
	

AV.Cloud.define('getTraceByVehicle', function(request,response) {

	var vehicleId = request.params["vehicleId"];
	
	//1, who is responsible for the trace?
	var query=new AV.Query('Device');
	query.equalTo('vehicleId',vehicleId);
	query.equalTo('type','location');
	query.addDescending('priority');
	query.first().then(function (locationDevice) {
		
		
		locationDevice=JSON.parse(JSON.stringify(locationDevice));
		var locationDeviceId=locationDevice.installation_id;
		
		//2, get all the data related with that
		
		var rawData=[];
		var dupRemoved=[];
		var procRes=[];
		readAllByThisTracer();
		function readAllByThisTracer() {
			var query=new AV.Query(AV_Trace);
			query.addAscending('createdAt');
			query.equalTo('installationId',locationDeviceId)
			query.skip(rawData.length);
			query.find().then(function(thisSegment) {
				rawData=rawData.concat(thisSegment);
				if(thisSegment.length!==0)
					readAllByThisTracer();
				else {
					//3, All has been read
					for(var i=0;i<rawData.length;i++) {
						rawData[i]=[rawData[i].attributes.location.longitude,rawData[i].attributes.location.latitude];
					}

					dupRemoved[0]=rawData[0];
					for(var i=1;i<rawData.length;i++) {
						var j=dupRemoved.length-1;
						var threshold=0.0001;
						if(Math.abs(rawData[i][0]-dupRemoved[j][0])>threshold || Math.abs(rawData[i][1]-dupRemoved[j][1])>threshold)
							dupRemoved[j+1]=rawData[i];
					}

					
					//4, Turning dupRemoved into the number of point we want
					response.success({'vehicleId':vehicleId, 'trace':dupRemoved});
					
				}
			},function(err) {console.log(err); response.error(err);}
			);
		}
		
	},function(err) {console.log(err); response.error(err);}
	);
});

//qpjia
AV.Cloud.define('getTraceByVehicleInterval', function(request,response) {
	var date = new Date();
    var seperator1 = "-";
    var seperator2 = ":";
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var statedate = year + seperator1 + month + seperator1 + strDate
            + " " + "00" + seperator2 + "00"
            + seperator2 + "00";
	var enddate = year + seperator1 + month + seperator1 + strDate
            + " " + "24" + seperator2 + "00"
            + seperator2 + "00";
	var vehicleId = request.params["vehicleId"];
	
	//1, who is responsible for the trace?
	var query=new AV.Query('Device');
	query.equalTo('vehicleId',vehicleId);
	query.equalTo('type','location');
	query.addDescending('priority');
	query.first().then(function (locationDevice) {
		
		
		locationDevice=JSON.parse(JSON.stringify(locationDevice));
		var locationDeviceId=locationDevice.installation_id;
		
		//2, get all the data related with that
		
		var rawData=[];
		var dupRemoved=[];
		var procRes=[];
		readAllByThisTracer();
		function readAllByThisTracer() {
			var query=new AV.Query(AV_Trace);
			query.addAscending('createdAt');
			query.equalTo('installationId',locationDeviceId)
			query.skip(rawData.length);
			query.greaterThanOrEqualTo('createdAt',statedate)
			query.lessThanOrEqualTo('createdAt',enddate)
			query.find().then(function(thisSegment) {
				rawData=rawData.concat(thisSegment);
				if(thisSegment.length!==0)
					readAllByThisTracer();
				else {
					//3, All has been read
					for(var i=0;i<rawData.length;i++) {
						rawData[i]=[rawData[i].attributes.location.longitude,rawData[i].attributes.location.latitude];
					}

					dupRemoved[0]=rawData[0];
					for(var i=1;i<rawData.length;i++) {
						var j=dupRemoved.length-1;
						var threshold=0.0001;
						if(Math.abs(rawData[i][0]-dupRemoved[j][0])>threshold || Math.abs(rawData[i][1]-dupRemoved[j][1])>threshold)
							dupRemoved[j+1]=rawData[i];
					}

					
					//4, Turning dupRemoved into the number of point we want
					response.success({'vehicleId':vehicleId, 'trace':dupRemoved});
					
				}
			},function(err) {console.log(err); response.error(err);}
			);
		}
		
	},function(err) {console.log(err); response.error(err);}
	);
});	


AV.Cloud.define('SMStest', function(request,response) {
	//ReadyToBoard();
	ConfirmBoard('574afcd6a341310059177d75');
	response.success('aha');
});

AV.Cloud.define('resetOnboardStatus', function(request,response) {
	
	var query=new AV.Query(AV_people);
	
	query.find().then(function(result){
		for(var i=0;i<result.length;i++) {
			result[i].set('onboard',0);
			result[i].save();
		}
	});
	response.success('aha');
});

//b5b182c7-eab1-4988-aa99-b5c1517008d9,424,X

AV.Cloud.define('checkin', function (request, response) {
	

	
	var installationId = request.params["installationId"],
        type = request.params["type"], // ibeacon, location, face
        content = request.params["content"];
		
	
    //console.log('installationId:' + installationId);
	//console.log('type:' + type);
	
	if ('ibeacon' === type) {
		var SAMPLE_ibeacon = {
			installationId:'test',
			type:'ibeacon',
			content: {
				timestamp: 1464547533560,//Date().getTime()
				beacons: {
					"UUID1,MAJOR,MINOR": {vendor: "estimote", distance: 3.15},
					"UUID2,MAJOR,MINOR": {vendor: "estimote", distance: 5.25}
				}
			}
		}
		
		//Refresh Device list
		updateDevice(installationId,'beacon');
			
		var entryId=[];
		console.log(content);
		console.log(installationId+' found '+Object.keys(content.beacons).length + ' beacons. Our beacons are:')
		for(var tagId in content.beacons) {
			//Our beacon?
			if(tagId.substring(0,40)!='b5b182c7-eab1-4988-aa99-b5c1517008d9,424') continue
			
			//Make log
			console.log(tagId+', '+content.beacons[tagId].distance)

			//Take action
			if(instId[installationId]==='busDoor'&&content.beacons[tagId].distance<boardingDistance) closeToBus(beacon2people[tagId]);
			if(instId[installationId]==='schoolGate') closeToSchool(beacon2people[tagId]);

			
			
			//Record the discovery
			if (false) {
				var beacon=new AV_ibeaconLog();
				beacon.set('tagId', tagId);
				beacon.set('installationId',installationId);
				beacon.set('vendor', content.beacons[tagId].vendor);
				beacon.set('distance', content.beacons[tagId].distance);
				beacon.set('clientTimeStamp', new Date(content.timestamp));
				
				beacon.save().then(
					function(beacon) {
						
						//console.log('iBeacon trace log saved: '+ beacon.id);
						entryId.push(beacon.id);
					} ,
					function(err) {
						console.log(err);
						response.error(err);
						return;
					}
				);
			}
		}
		
		
		// response.success({code:0, entries:entryId}); // Issue with async calls 
		response.success({code:0});
	}

	if ('location' === type) {
		
		var SAMPLE_location = {
			installationId:'test',
			type:'location',
			content: {
				timestamp: 1464547533560, //Date().getTime()
				lat: 40.215345, // Double
				lng: 116.34124, // Double
				source: "amap",
				accuracy: 20 //Double
			}
		}
		


		
		if(content.lat>0.1) { //Simple sanity check
		updateDevice(installationId,'location');
				
		//Save it
		var trace=new AV_Trace();
		
		trace.set('clientTimeStamp', new Date(content.timestamp));
		trace.set('location', new AV.GeoPoint({latitude:content.lat,longitude:content.lng}));
		trace.set('source', content.source);
		trace.set('accuracy', content.accuracy);
		trace.set('installationId', installationId);
		
		console.log(installationId+' located: '+content.lat+','+content.lng)
		
		
		trace.save().then(		//Refresh Device list
		
			
			function(trace) {
				var message={code:0, message:'Location saved: '+ trace.id}
				console.log(message);
				response.success(message);
			} ,
			function(err) {
				console.log(err);
				response.error(err);
			}
		);
		} else {
			console.log('False data. Not saved.')
		}
	}

	if ('face' === type) {
		var SAMPLE_face = {
			installationId:'test',
			type:'face',
			content: {
				timestamp: 1464547533560,//Date().getTime()
				people: { // ObjectId of a people record
					"people_A_id": {confidence: 0.8},
					"people_B_id": {confidence: 0.9}
				}
			}
		}
		
		
		
		var entryId=[];
		
		for(var peopleId in content.people) {
			var person=new AV_faceLog();
			
			if(peopleId.length>10) {
				person.set('peopleId', peopleId);
				faceDetected(peopleId);
			} else {
				faceDetected(peopleDB[peopleId]);
				person.set('peopleId', peopleDB[peopleId]);
				person.set('note',peopleId);
			}
			person.set('installationId',installationId);
			person.set('confidence', content.people[peopleId].confidence);
			person.set('clientTimeStamp', new Date(content.timestamp));
			person.save().then(
				function(person) {
					console.log('Face detection log saved: '+ person.id);
					entryId.push(person.id);
				} ,
				function(err) {
					console.log(err);
					response.error(err);
					return;
				}
			);
		}
		
		// response.success({code:0, entries:entryId}); // Issue with async calls 
		response.success({code:0});
	}

	return response;
	
})




function faceDetected(peopleId) {
	
	var query=new AV.Query(AV_people);
	
	query.get(peopleId).then(function(result){
		if(result.get('onboard')!==10) return;
		ConfirmBoard(peopleId);
		result.set('onboard',20);
		result.save();
	},function(err) {
		console.log(err);
	});
}


function closeToBus(peopleId) {
	console.log('closeToBus: '+peopleId);
	var query=new AV.Query(AV_people);
	if(typeof peopleId==='undefined') return;
	query.get(peopleId).then(function(result){

		if(result.get('onboard')!==0) return;
		ConfirmBoard(peopleId)
		result.set('onboard',20);
		result.save().then(function() {console.log('saved');},function(err) {console.log('save failed');console.log(err)});
		
	},function(err) {
		//console.log(err);	
	});
}

function closeToSchool(peopleId) {
	
	var query=new AV.Query(AV_people);
	
	query.get(peopleId).then(function(result){
		if(result.get('onboard')!==20) return;
		ConfirmArrive(peopleId);
		result.set('onboard',30);
		result.save();
	},function(err) {
		console.log(err);
	});
}


function ConfirmBoard(peopleId) {
	AV.Cloud.requestSmsCode({
		mobilePhoneNumber: cellphone[peopleId],
		template: 'v2_接到乘客',
		student_name: peopleDB2[peopleId],
		destination_address: '学校'
	})
}

function ConfirmArrive(peopleId) {
	AV.Cloud.requestSmsCode({
		mobilePhoneNumber: cellphone[peopleId],
		template: 'v2_到达目的地',
		student_name: peopleDB2[peopleId],
		destination_address: '学校'
	})
}

function ReadyToBoard() {
	for (i in cellphone) {
		AV.Cloud.requestSmsCode({
			mobilePhoneNumber: cellphone[i],
			template: '校车即将到达',
		})
	}
	console.log('ReadyToBoard messages sent');
}




//By FXP, streaming, end

var Pili = require('pili');
var ACCESS_KEY = 'B8ujKnZEYrgY0CgQxWsZ1u2_nmROkmgYzhDDD3cF';
var SECRET_KEY = 'wZtc6zI29zoNgA2nXCq0uqDLpLCoiGKJBHinedaw';
var HUB = 'schoolbus'; // The Hub must be exists before use
var credentials = new Pili.Credentials(ACCESS_KEY, SECRET_KEY);
var hub = new Pili.Hub(credentials, HUB);

var Device = AV.Object.extend("Device");
AV.Cloud.define('stream', function (req, res) {
    var installation_id = req.params.installation_id;
    if (installation_id) {
        var query = new AV.Query(Device);
        query.equalTo("installation_id", installation_id);
        query.first().then(function (device) {
            if (device) {
                res.success(device.get("stream_conf"));
            } else {
                var options = {
                    publishSecrity: "static" // optional, can be "dynamic" or "static", "dynamic" as default
                };
                hub.createStream(options, function (err, stream) {
                    if (!err) {
                        var result = stream.toJSONString();
                        new Device({
                            installation_id: installation_id,
                            stream_conf: stream.toJSONString()
                        }).save();
                        res.success(result);
                    } else {
                        res.error(err);
                    }
                });
            }
        }, function (err) {
            res.error(err);
        })
    } else {
        res.error("invalid params");
    }
});

//By FXP, streaming, end



const streamingService=require('./streamModule');
AV.Cloud.define('streamURLs', function(request, response) {
	var installationId=request.params["installationId"];
	var channel=installationId;
	var requestedBy=request.params["requestedBy"];
	
	if(channel)
		response.success({
			'push':streamingService.push(channel),
			'rtmp':streamingService.rtmp(channel),
			'm3u8':streamingService.m3u8(channel)
		});
	else
		response.error('installationId not valid');
	if(requestedBy==='camera') {
		updateDevice(installationId,'camera');

	}
		
});


function updateDevice(installationId,type) {
	console.log('Updating database for device:' +installationId+'('+type+')');
	var query=new AV.Query('Device');
	query.equalTo('installation_id',installationId);
	query.equalTo('type',type);
	query.find().then(function (results) {
		if(results.length===0) {
			console.log('New device');
			var device=new AV_Device();
			device.set('type',type);
			device.set('installation_id',installationId);
			device.save().then(function() {console.log('Entry added.')}, function() {console.log('Failed to add entry.')});
		} else {
			console.log('Existing device');
			var device=results[0];
			device.save().then(function() {console.log('Entry updated.')}, function() {console.log('Failed to update entry.')});;
		}
	},function (error) {
		console.log(error);
	});
}



module.exports = AV.Cloud;





