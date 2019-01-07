app.controller("contentController", function ($scope, contentService) {
    // 初始化广告容器，广告分类id作为数组索引。可以通过数组索引
    // 拿到多个content信息的数组
    $scope.contentList = [];
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                // 会先初始化首页轮播图，分类id为1
                $scope.contentList[categoryId] = response;
            }
        )
    };

    $scope.search=function(){
        location.href="http://localhost:6001/search.html#?keywords="+$scope.keywords;
    };
});