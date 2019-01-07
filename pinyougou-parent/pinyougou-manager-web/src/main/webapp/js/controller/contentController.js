 //控制层 
app.controller('contentController' ,function($scope,$controller,contentCategoryService, uploadService,contentService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		contentService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		contentService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){				
		contentService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	};
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=contentService.update( $scope.entity ); //修改  
		}else{
			serviceObject=contentService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					swal("", response.message, "error");
				}
			}		
		);				
	};
	
	 
	//批量删除 
	$scope.dele=function(){
		if ($scope.selectIds.length == 0) {
			swal("", "请选中内容后再进行删除", "error");
			return;
		}
		//获取选中的复选框			
		contentService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	};
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		contentService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	// 清空上传控件选中的文件名
    $scope.clearFile = function () {
        var obj = document.getElementById('file');
        obj.outerHTML = obj.outerHTML;
    };

    // 上传文件
    $scope.uploadFile = function () {
		uploadService.uploadFile().success(
			function (response) {
                if (response.success) {
                    swal("", "上传成功", "success");
                    $scope.entity.pic = response.message;
				} else {
                    swal("", "上传失败", "error");
				}
            }
		)
    };

    $scope.status = ['无效', '有效'];

    $scope.findContentCategory = function () {
        contentCategoryService.findAll().success(
        	function (response) {
				$scope.contentCategoryList = response;
       		}
        )
    };

    $scope.findContentCategory();

    $scope.updateStatus = function (status) {
        if ($scope.selectIds.length == 0) {
            swal("", "请选中内容后再进行操作", "error");
            return;
        }
		contentService.updateStatus($scope.selectIds, status).success(
			function (response) {
				if (response.success) {
                    swal("", "状态更新成功", "success");
                    $scope.reloadList();//重新加载
                    $scope.selectIds=[];//重置数组
				} else {
                    swal("", "状态更新失败", "error");
				}
            }
		)
    };
});	
