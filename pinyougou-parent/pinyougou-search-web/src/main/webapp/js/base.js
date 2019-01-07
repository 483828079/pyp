/*一个工程该对应一个module*/
var app=angular.module('pinyougou',[]);
                    // 引入$sec服务
app.filter('trustHtml',['$sce', function ($sce) { // 注入$sec
    return function (data) {
        return $sce.trustAsHtml(data);
    };
}]);