app.controller('baseController', function ($scope) {
    /*分页配置*/
    $scope.paginationConf = {
        currentPage: 1, /*当前页位置*/
        totalItems: 0, /*总条数*/
        itemsPerPage: 10, /*当前页显示个数*/
        perPageOptions: [10, 20, 30, 40, 50], /*下拉框数据*/
        onChange: function(){
            $scope.reloadList();//第一次加载执行，然后选中其他下拉选项后再执行
            // 第一次调用 page=1,pageSize=10进行请求，然后遍历当前页。
            // 需要paginationConf.totalItems总条数，然后根据总条数和当前页显示个数
            // 计算出共有多少页。
            // 所以每次需要后端传来当前页数据用来显示数据，总条数用来计算出多少页
        }
    };

    // 重新加载页面
    $scope.reloadList=function(){
        //切换页码
        $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    // 复选框处理，用来存储选中的id
    $scope.selectIds = []; // 声明一个数组用来记录选中复选框的id
    // 复选框点击后会执行该方法记录ids
    $scope.updateSelection = function (even, id) { // 让所有复选框绑定该方法
        // 判断是否选中，只有选中才添加到数组中去
        // 点击后触发，看是选中还是取消选中。选中添加到数组中去
        // 取消选中从数组中删除
        if (even.target.checked) {
            $scope.selectIds.push(id);
        } else {
            // 删除数组中的id，从id的位置开始删除一个元素
            $scope.selectIds.splice($scope.selectIds.indexOf(id), 1);
        }
    };

    // 对于复选框全选的处理
    // 要想使用让某个复选框作为全部复选框的开关，
    // 然后绑定该方法
    $scope.selectAll = function (even) {
        // 清空
        $scope.selectIds = [];
        if (even.target.checked) {
            $($scope.list).each(function () {
                // 添加到集合
                $scope.selectIds.push(this.id);
            });
        }
    };

    /*提供一个json对象，取出所有key对应的value拼接成字符串*/
    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将json字符串转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key];
        }
        return value;
    };

    // [{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
    // list存储了多个对象的数组， 通过某个对象的属性名和属性值获取当前对象
    $scope.searchObjectByKey=function(list,key,keyValue){
        for (var i = 0; i < list.length; i++) {
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        return null;
    };
});


