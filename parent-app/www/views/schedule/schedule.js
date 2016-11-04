angular.module('App').controller('ScheduleCtrl', function ($scope, $http, $ionicLoading) {
    $scope.currentUser=AV.User.current();
});
