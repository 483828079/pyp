app.controller('indexController', function ($scope, loginService) {
    $scope.showLoginName = function(){
        loginService.loginName().success(
            function(response){
                $scope.loginName=response.loginName;
            }
        );
    };

    $scope.showLoginName(); // 初始化loginName到域中
});