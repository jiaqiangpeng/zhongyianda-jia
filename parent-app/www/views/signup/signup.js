angular.module('App').controller('SignUpCtrl', function ($scope, $http, $ionicLoading) {

    $scope.currentUser=AV.User.current();
    
    $scope.pendingSMSText = "获取验证码";
    $scope.pendingSMS = false;
    $scope.doSendSMS = function (loginForm) {
        if ($scope.pendingSMS) {
            return;
        }
        AV.Cloud.requestSmsCode(loginForm.phoneNo).then(function () {
            $scope.lastError = "发送成功";
            $scope.$apply();
        }, function (err) {
            $scope.lastError = "错误," + err.message;
            $scope.$apply();
        });
        var count = 30;
        $scope.pendingSMS = true;
        var timerId = setInterval(function () {
            count--;
            $scope.pendingSMSText = count + "秒";
            $scope.$apply();
            if (count == 0) {
                clearInterval(timerId);
                $scope.pendingSMSText = "获取验证码";
                $scope.pendingSMS = false;
                $scope.$apply();
            }
        }, 1000);
    };

    // Perform the login action when the user submits the login form
    $scope.doLogin = function (loginForm) {
        var user = new AV.User();
        user.signUpOrlogInWithMobilePhone({
            mobilePhoneNumber: loginForm.phoneNo,
            smsCode: loginForm.verificationCode,
        }).then(function (user) {
            alert("登录成功");
            $scope.currentUser=AV.User.current();
            $scope.$apply();
        }, function (error) {
            $scope.lastError = "错误," + err.message;
            $scope.$apply();
        });

    };

    // Perform the login action when the user submits the login form
    $scope.doLogOut = function () {
        AV.User.logOut();
        alert("已经退出登录");
        $scope.currentUser=AV.User.current();
        $scope.$apply();
    };
});
