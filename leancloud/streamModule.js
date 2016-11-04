var md5 = require("blueimp-md5")


/*
record rtmp push url:
rtmp://video-center.alivecdn.com/schoolbus/[channelname]?vhost=live.gaidonghai.com&auth_key=[md5hash]


tohash: '/schoolbus/[CHANNELNAME]-[EXPIRATION-TIMESTAMP]-0-0-[KEY]'
method: md5sum

channelname=installationID
EXPIRATION-TIMESTAMP=1475090311, something like that
key=abc123

------------

record pull url:

rtmp://live.gaidonghai.com/schoolbus/5799cf716be3ff0065cbdf98?auth_key=1475090851-0-0-e1842fbbbe24773af05375cceb5d2a3b
http://live.gaidonghai.com/schoolbus/5799cf716be3ff0065cbdf98.m3u8?auth_key=1475090851-0-0-173b8f2e09a1f2c24a677ddb8e5f0901


tohash:
rtmp: '/schoolbus/[CHANNELNAME]-[EXPIRATION-TIMESTAMP]-0-0-[KEY]'
hlc:  '/schoolbus/[CHANNELNAME].m3u8-[EXPIRATION-TIMESTAMP]-0-0-[KEY]'


*/


var streaming={}; //This module to export

var programName='schoolbus';
var expiration=Math.round(new Date().getTime()/1000)+86400*365; //365 days. For god sake. 
var key='abc123';
//for channels, installationID will be used



function authkey_push(channel) {
	authString='/'+programName+'/'+channel+'-'+expiration+'-0-0-'+key;
	return expiration+'-0-0-'+md5(authString)
}

function authkey_rtmp(channel) {
	authString='/'+programName+'/'+channel+'-'+expiration+'-0-0-'+key;
	return expiration+'-0-0-'+md5(authString)
}


function authkey_m3u8(channel) {
	authString='/'+programName+'/'+channel+'.m3u8-'+expiration+'-0-0-'+key;
	return expiration+'-0-0-'+md5(authString)
}

var pushServer='rtmp://video-center.alivecdn.com';
var vhost='live.gaidonghai.com';

streaming.push = function (channel) {
	     //rtmp://video-center.alivecdn.com/  schoolbus    /  mi5a     ?vhost=  live.gaidonghai.com&auth_key=1483727678-0-0-c94e87578a69bc746854e55fecdd7a85
	return pushServer+                    '/'+programName+'/'+channel+'?vhost='+vhost+            '&auth_key='+authkey_push(channel);
}

streaming.rtmp = function (channel) {
	      //rtmp:// live.gaidonghai.com/   schoolbus     /  5799cf716be3ff0065cbdf98?auth_key=  1475090851-0-0-e1842fbbbe24773af05375cceb5d2a3b
	return 'rtmp://' + vhost +        '/'+programName+'/'+channel+               '?auth_key='+authkey_rtmp(channel);
}

streaming.m3u8 = function (channel) {
		  //http://live.gaidonghai.com/    schoolbus     /  5799cf716be3ff0065cbdf98.m3u8?auth_key=1475090851-0-0-173b8f2e09a1f2c24a677ddb8e5f0901
	return 'http://' + vhost +        '/'+programName+'/'+channel+               '.m3u8?auth_key='+authkey_m3u8(channel);
}


module.exports=streaming;