//服务层
app.service('loginService',function($http){
    //获取登录用户
    this.showName = function(){
        return $http.get("../login/user.do?");
    };
});