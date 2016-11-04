angular.module('App').controller('TourCtrl', function ($scope, $location, $ionicPopup) {

  $scope.start = function () {
      localStorage.setItem('firstVisit', true);
      $location.url('/map');
  }
});
