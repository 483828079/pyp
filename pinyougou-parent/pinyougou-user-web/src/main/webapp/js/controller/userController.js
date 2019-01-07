 //控制层 
app.controller('userController', function($scope, userService){
    $scope.entity = {};
    $scope.isChecked = false;

    // 用户注册
    $scope.register= function () {
        if (!checkedUserInfo()) {
            return;
        }

        if (! $scope.isChecked) {
            swal("", "同意协议后才能够注册!", "error");
            return;
        }

        userService.add($scope.entity,$scope.smscode).success(
            function (response) {
                if (response.success) {
                    swal("", response.message, "success");
                } else {
                    swal("", response.message, "error");
                }
            }
        )
    };

    $scope.updateChecked = function(e) {
        $scope.isChecked = e.target.checked;
    };

    $scope.sendCode = function () {
        if (isEmpty($scope.entity.phone)) {
            swal("", "手机号为空请重新输入。", "error");
            return false;
        }

        userService.sendCode($scope.entity.phone).success(
            function (response) {
                if (response.success) {
                    swal("", response.message, "success");
                } else {
                    swal("", response.message, "error");
                    $scope.entity.phone = "";
                }
            }
        )
    };

    function checkedUserInfo() {
        if (isEmpty($scope.entity.username)) {
            swal("", "用户名为空请重新输入。", "error");
            return false;
        }

        if (isEmpty($scope.entity.password)) {
            swal("", "密码为空请重新输入。", "error");
            return false;
        }

        if (isEmpty($scope.rePassword)) {
            swal("", "请确认密码！", "error");
            return false;
        }

        if ($scope.entity.password != $scope.rePassword) {
            swal("", "两次输入密码不一致。", "error");
            $scope.entity.passowrd = "";
            $scope.rePassword = "";
            return false;
        }

        if (isEmpty($scope.entity.phone)) {
            swal("", "手机号为空请重新输入。", "error");
            return false;
        }

        if (isEmpty($scope.smscode)) {
            swal("", "验证码为空请重新输入。", "error");
            return false;
        }

        return true;
    }

    // 判断字符是否为空
    function isEmpty(obj){
        if(obj == undefined || obj == null || obj == ""){
            return true;
        }else{
            return false;
        }
    }
});	
