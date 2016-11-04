angular.module('App').controller('MapCtrl', function ($scope, $http, $ionicLoading) {

  $ionicLoading.show({
    template: '<span class="icon spin ion-loading-d"></span>读取车辆信息'
  });

  // You need to get a key from Open Weather Map and put the key in API_KEY_HERE


  var map = new AMap.Map('mapcontainer', {
    resizeEnable: true,
    center: [116.5423169, 40.211634],
    zoom: 14
  });

/*
  map.plugin(["AMap.MapType"],function(){
    //地图类型切换
    var type= new AMap.MapType({
      defaultType:0 //使用2D地图
    });
    map.addControl(type);
  });
*/


  var mapElement={};
  var vehicleList=['5747d9cac26a38006ba3871e','57ec92cfa22b9d005ba90cf4'];
  var colorList={
    '5747d9cac26a38006ba3871e':'#0000FF',
    '57ec92cfa22b9d005ba90cf4':'#FF0000'
  };


  var icon_A = new AMap.Icon({
    image: "http://webapi.amap.com/theme/v1.3/images/newpc/poi-1.png",
    size: new AMap.Size(32,32),
    imageOffset: new AMap.Pixel(0, -62)
  });
  var icon_X = new AMap.Icon({
    image: "http://webapi.amap.com/theme/v1.3/images/newpc/poi-1.png",
    size: new AMap.Size(32,32),
    imageOffset: new AMap.Pixel(-355, -62)
  });

  var iconList={
    '5747d9cac26a38006ba3871e':icon_X,
    '57ec92cfa22b9d005ba90cf4':icon_A
  };

  refreshPoints=function() {
    console.log("refreshPoints");
    for(var i=0;i<vehicleList.length;i++) {
      var vehicleId=vehicleList[i];
      AV.Cloud.run('getTraceByVehicle',{'vehicleId':vehicleId}).then(function(res) {
        $ionicLoading.hide();
        var vehicleId=res.vehicleId;
        console.log(res);
        //console.log(res.trace.length)

        if(typeof mapElement[vehicleId] === 'undefined') {
          console.log('create:' + vehicleId);
          var polyline = new AMap.Polyline({
            path: res.trace,          //设置线覆盖物路径
            strokeColor: colorList[res.vehicleId], //线颜色
            strokeOpacity: 1,       //线透明度
            strokeWeight: 3,        //线宽
            strokeStyle: "dashed",   //线样式
            //strokeDasharray: [10,2] //补充线样式
          });

          var marker = new AMap.Marker({
            //icon: "http://webapi.amap.com/theme/v1.3/markers/n/mark_b.png",
            position: res.trace[res.trace.length-1],
            icon: iconList[vehicleId]
          });
          //marker.setAnimation('AMAP_ANIMATION_BOUNCE');


          mapElement[vehicleId]={
            'polyline':polyline,
            'marker':marker
          };

          polyline.setMap(map);
          marker.setMap(map);

        } else {
          console.log('update:' + vehicleId);
          mapElement[vehicleId].marker.setPosition(res.trace[res.trace.length-1]);
          mapElement[vehicleId].polyline.setPath(res.trace);
        }
      });


    }
    setTimeout(refreshPoints,5000)
  };

  refreshPoints();
  
	//  confirm 对话框
	$scope.showConfirm = function() {
	 var confirmPopup = $ionicPopup.confirm({
	   title: '重置',
	   template: '确定重置为当天最新的路线吗?'
	 });
	 confirmPopup.then(function(res) {
	   if(res) {
		 console.log('You are sure');
	   } else {
		 console.log('You are not sure');
	   }
	 });
	};
  
  $scope.grid_height={height:window.screen.height-44+'px'};

});
