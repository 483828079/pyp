 //控制层 
app.controller('itemCatController' ,function($scope,$controller,typeTemplateService,itemCatService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	};

	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	$scope.findByParentId = function(parentId) {
		itemCatService.findByParentId(parentId).success(
			function (response) {
			    if (response.success) {
                    /*查询后将当前信息显示在页面上*/
                    $scope.list = response.obj;
                }
            }
		)
	};

	/*第一次加载不用显示所有目录，显示一级目录*/
    $scope.findByParentId(0);

    $scope.listArr = [];
    $scope.rest = 1;
    $scope.addList = function (id, name) {
        $scope.listArr.push({id:id,name:name});
        $scope.rest = $scope.listArr.length;
    };
    $scope.removeList = function (index) {
    	/*删除从当前位置和之后所有元素*/
        $scope.listArr.splice(index+1);
        $scope.rest = $scope.listArr.length;
    };

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=itemCatService.update( $scope.entity ); //修改
        }else{
            if ($scope.listArr.length == 0) {
                $scope.entity.parentId = 0;
            } else {
                $scope.entity.parentId = $scope.listArr[$scope.listArr.length - 1].id;
            }
            serviceObject=itemCatService.add( $scope.entity);//增加
        }

        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.findByParentId($scope.entity.parentId)//重新加载
                }else{
                    swal("", response.message, "error");
                }
            }
        ).error(function () {
            swal("", "类型错误", "error");
        });
    };

    //批量删除
    $scope.dele=function(){
            //获取选中的复选框
            itemCatService.dele( $scope.selectIds ).success(
                function(response){
                    if(response.success){
                        var parentId = 0;
                        if ($scope.listArr.length == 0) {
                            parentId = 0;
                        } else {
                            parentId = $scope.listArr[$scope.listArr.length - 1].id;
                        }
                        $scope.findByParentId(parentId);//重新加载
                        $scope.selectIds=[];
                    } else {
                        swal("", response.message, "error");
                    }
                }
            );
    };

    $scope.typeTemplateList = {data:{}};
    $scope.findTypeTemplateList = function () {
        typeTemplateService.findTypeTemplateList().success(
            function (response) {
                $scope.typeTemplateList.data = response;
            }
        ).error(
            function () {
                swal("", "类型模板加载失败", "error");
            }
        );
    };


    //初始化templateList
    $scope.findTypeTemplateList();
});	
