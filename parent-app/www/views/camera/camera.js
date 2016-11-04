angular.module('App').controller('CameraCtrl', function ($scope, $http, $ionicLoading) {



  $scope.videoPlayers=[];

  addNewVideoPlayer({
    name:"测试视频信号",
    comment:"流播录像，用于测试及向未签约用户展示功能",
    file: "http://live.gaidonghai.com/schoolbus/demo.m3u8?auth_key=1508243125-0-0-e412e309fa5ecb30d1bbeaeb8379a41f"
  });

  addNewVideoPlayer({
    name:"车载视频信号（1）",
    comment:"实时直播信号",
    file: "http://live.gaidonghai.com/schoolbus/1064daf9f9975453b31d8918f5c55274.m3u8?auth_key=1508421882-0-0-969f15d2fbc058c745357c52f4682ed4"
  });


  addNewVideoPlayer({
    name:"车载视频信号（2）",
    comment:"实时直播信号",
    file: "http://live.gaidonghai.com/schoolbus/f1293c045337c9bffa973aed80178a3c.m3u8?auth_key=1508421882-0-0-7285ae63c8f1caf9a740d0d2bdb722fe"
  });

  addNewVideoPlayer({
    name:"车载视频信号（3）",
    comment:"实时直播信号",
    file: "http://live.gaidonghai.com/schoolbus/85b208a552a6734382b45ffd1248e332.m3u8?auth_key=1508421882-0-0-2eb503d1070b51b0b1f1835af41605d1"
  });





  //Helper functions
  function addNewVideoPlayer(parameters) {

    //Create with a default
    var newPlayer={
      options:{
        file: parameters.file,
        width:"100%",
        aspectratio:"16:9",
        autostart: true
      }
    };

    if(parameters.name) newPlayer.name=parameters.name;
    if(parameters.comment) newPlayer.comment=parameters.comment;
    $scope.videoPlayers.push(newPlayer);


  }
});
