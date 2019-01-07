 //控制层 
app.controller('typeTemplateController' ,function($scope, $controller, brandService, specificationService, typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
 				/*就和add时一样，SpringMVC将json对象转换为了String属性
				* 所以返回的是{"brandIds":"[{\"id\":1,\"text\":\"联想\"}]"}
				* brandIds为key "[{\"id\":1,\"text\":\"联想\"}]"就是对应的值
				* 因为是String类型所以要加上""
				* 对应着属性的字符串，并不能将其转换为json对象。所以需要自己将
				* json字符串转换为json对象
				* */
				$scope.entity= response;
                $scope.entity.brandIds=  JSON.parse($scope.entity.brandIds);
                $scope.entity.specIds=  JSON.parse($scope.entity.specIds);
                $scope.entity.customAttributeItems= JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	};
	
	//保存 
	$scope.save=function(){
		var serviceObject;//服务层对象
		/*如果是新增id自然不存在，
		* 新增的流程
		* 	entity记录着模态框上的所有信息
		* 	select2，记录着json的数据。但是数据库要的是String类型的
		* 	所以只用传过去就是json格式的字符串
		*   name 直接绑定的文本框
		*	customAttributeItems 多个customAttributeItem的值,用
			customAttribItems的text记录文本框信息 {text:''}
			SpringMVC需要的是json对象，而不是之前的json字符串。
			所以会在参数绑定的时候根据类型转换
			customAttribItems=[{text:''},{text:''}]; // 这个值就直接不转换了
			也就是按照类型转换的时候，请求参数的属性值为json对象的时候
			而方法参数属性为String的时候，会将json对象格式转换为对应的字符串
				[{"id":1,"text":"联想"},{"id":9,"text":"苹果"}],再进行绑定。
			对于文本框清空的问题
				如果是新建会entity = {customAttributeItems : []}，
					清空了entity，但是声明了customAttributeItems。
					customAttributeItems在新增扩展属性会使用到，所以要提前声明。
				如果是修改会findOne，会直接覆盖掉之前表单数据。
		* */
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
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
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
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
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};

	/*必须是该格式才能作为select2下拉框的数据源*/
    $scope.brandList={data:[]};
    $scope.specifcationList={data:[]};

    $scope.selectOptionList = function () {
        brandService.selectOptionList().success(
        	/*响应回来的结果是一个json对象*/
        	function (response) {
            	$scope.brandList = {data:response};
        	}
        );

        specificationService.selectOptionList().success(
        	function (response) {
                $scope.specificationList = {data:response};
            }
		)
    };

    // 在加载页面的时候为下拉框初始化数据源
    $scope.selectOptionList();

    // 拓展属性选项卡, 增加就长度+1
	// entity.customAttributeItems的每个元素都绑定 text
	// 将entity作为请求参数，绑定到复合实体，进行增删该查
	$scope.entity = {customAttributeItems : []};

    $scope.addTableRow=function() {
        $scope.entity.customAttributeItems.push({});
    };

    // 点击删除后，传来当前元素在数组中的索引。
    $scope.deleteTableRow=function(index) {
        $scope.entity.customAttributeItems.splice(index, 1);
    };

});	
