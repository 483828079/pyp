app.controller("indexController", function ($scope, loginService) {
    $scope.loginName = "";
    $scope.showName = function () {
        loginService.showName().success(
            function (response) {
                $scope.loginName = response.loginName;
            }
        )
    };

    $scope.showName();
});