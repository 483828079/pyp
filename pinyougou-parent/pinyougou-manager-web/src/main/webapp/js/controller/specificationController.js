 //控制层 
app.controller('specificationController' ,function($scope,$controller   ,specificationService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll = function(){
		specificationService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage = function(page,rows){
		specificationService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne = function(id){
		specificationService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	};
	
	//保存 
	$scope.save = function(){
		var serviceObject;//服务层对象
		// add只会将文本框内容放入entity.specification
		// update之前会findAll()=entity 将id也会放入entity.specification
		// add之前又清空entity，所以能够保证add没有id，update有id
		if($scope.entity.specification.id!=null){//如果有ID
			serviceObject=specificationService.update( $scope.entity ); //修改  
		}else{
			serviceObject=specificationService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	};
	
	 
	// 批量删除
	// 选中复选框之后会将该规格id添加到selectIds数组中
	// 全选之后会将所有规格id添加到selectIds数组中
		// 先清空数组，然后遍历当前list，也就是当前页分页元素
	    // 只用获取到list中的id，存放到数组，也就是当前页所有id
	// 点击删除后请求所有选中元素的id
	$scope.dele = function(){
		//获取选中的复选框			
		specificationService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	};
	
	$scope.searchEntity = {};//定义搜索对象
	
	//搜索
	$scope.search = function(page,rows){
		specificationService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	// 在使用之前必须初始化
	// entity.specificationOptionList = []; 这种方式是错误的
	// 这样相当于使用entity该属性，但是并没有声明.
	// 声明一个entity对象，有一个属性specificationOptionList，是个数组
	// 如果是ng-model绑定的属性会在鼠标离开文本框的时候进行初始化，文本框的值就是该属性的值
	// 关于对象属性存在问题
	// 只要对象.属性 = ? 属性就被声明并初始化。
	// 但是要是想对象.属性.xx 就会报错，因为该属性并不存在。
    $scope.entity = {specificationOptionList:[]};// 明显加载页面的时候整个js都会初始化，不过方法需要调用。

    // 点击之后长度+1，用来做新增规格选项卡的增加
	// 遍历之后存放specificationOption的元素，很明显要用entity作为
	// 请求参数绑定参数，做增删该查操作。
    $scope.addTableRow = function(){
        $scope.entity.specificationOptionList.push({});
    };

    // 删除选项卡
    $scope.deleteTableRow = function(index) {
        $scope.entity.specificationOptionList.splice(index, 1);
    };
    
});	
