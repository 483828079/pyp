// 参数自动注入
app.controller("brandController", function ($scope, $controller,brandService) {
    $controller('baseController',{$scope:$scope});
    // 查询条件
    $scope.searchEntity={};
    // 请求分页页面
    $scope.search=function(page,pageSize){
        brandService.search(page, pageSize, $scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    };

    // 修改和添加都会将模态框显示，确定之后执行save
    // 通过entity.id是否为空判断是哪个操作
    $scope.save = function () {
        var obj;
        // 只有编辑的时候才会执行fileOne entity.id才会有值
        if ($scope.entity.id == null) {
            console.log($scope.entity);
            obj = brandService.add($scope.entity);
        } else {
            obj = brandService.update($scope.entity);
        }
        // 只能使用post请求了。。
        // 用entity记录编辑框的值，在绑定entity属性的时候entity对象就存在了。
        obj.success(
            function(response){
                if(response.success) {
                    // 说明保存成功，重新加载页面
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        );
    };

    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                // 存储到entity的元素，entity在绑定元素的时候初始化
                $scope.entity= response;
            }
        )
    };

    // 根据id删除元素
    $scope.delete = function () {
        if ($scope.selectIds.length <= 0) {
            alert("只有选中才能够删除。");
            return;
        }

        brandService.delete($scope.selectIds).success(
            function (response) {
                if(response.success) {
                    // 说明删除成功，重新加载页面
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        );
    };



});