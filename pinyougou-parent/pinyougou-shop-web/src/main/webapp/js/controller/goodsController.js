//控制层
app.controller('goodsController', function ($scope, $controller, $location, typeTemplateService, uploadService, itemCatService, goodsService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    // 查询实体
    // 需求是修改和添加使用同一个页面
    // 判断添加和修改的条件是url上是否带有id参数
    $scope.findOne = function () {
        // 获取url上的请求参数id的值
        // {id: "149187842867951", name: "zs"}
        // 获取到的是一个对象，通过key获取值
        var id = $location.search()['id'];
        /*如果id是空，说明不是修改可以不用查询*/
        if (id == null) {
            return;
        }

        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                // 给富文本编辑器设置值
                editor.html($scope.entity.goodsDesc.introduction);
                // 显示商品图片列表
                /*[{"color":"黄色","url":"http://192.168.25.153/group1/M00/00/01/wKgZmVv_5iuAJY3rAA2h27nruwU943.jpg"}]*/
                // 查询到的是字符串，需要转换为对象
                $scope.entity.goodsDesc.itemImages = JSON.parse(response.goodsDesc.itemImages);
                // 拓展属性
                $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                // 规格,规格的显示形式是复选框，应该判断规格是否存在来决定是否勾选
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                // sku的spec是字符串形式的，而spu对应着多个sku
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec =
                        JSON.parse( $scope.entity.itemList[i].spec);
                }
            }
        );
    };

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    swal("", "保存成功", "success");
                    // 保存成功后跳转回商品页面
                    location.href="goods.html";
                } else {
                    swal("", response.message, "error");
                }
            }
        );
    };


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };


    $scope.searchEntity = {};//定义搜索对象
    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //添加
    $scope.add = function () {
        // 添加富文本框内容到goodsDesc属性中。
        $scope.entity.goodsDesc.introduction = editor.html();
        /*让表单绑定entity,然后点击添加。请求entity到Controller*/
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    swal("", "添加成功", "success");
                    // 添加成功后需要能够继续录入下一条信息,所以要清空entity
                    $scope.entity = {};
                    /*设置富文本编辑器中的内容*/
                    editor.html('');

                } else {
                    swal("","添加失败", "error");
                }
            }
        );
    };

    $scope.image_entity = {url: ""};
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {//如果上传成功，取出url
                $scope.image_entity.url = response.message;//设置文件地址
            } else {
                alert(response.message);
            }
        }).error(function () {
            swal("", "上传发生错误", "error");
        });
    };

    $scope.entity = {goodsDesc: {itemImages: []}};//定义页面实体结构
    //添加图片列表
    // itemImages是一个数组，但是参数属性是String所以存的是json格式的字符串
    $scope.add_image_entity = function () {
        // 点击保存后将image_entity={color:'',url:''}放入itemImages
        // 新建后清空image_entity
        // 将itemImages遍历到页面
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    };

    $scope.clearFile = function () {
        var obj = document.getElementById('file');
        obj.outerHTML = obj.outerHTML;
    };

    //列表中移除图片
    // 点击移除将数组中索引对应元素删除
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    };

    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                if (response.success) {
                    $scope.itemCat1List = response.obj;
                } else {
                    swal("", "列表加载失败", "error");
                }
            }
        )
    };
    // 默认加载一级列表
    $scope.selectItemCat1List();

    /*$watch用来监控变量，如果该变量发生变化就执行方法
    * 第一个参数变量改变的值，第二个参数变量之前的值
    * 为什么要这样设置参数的位置？因为想要获取改变后的变量比较多些吧，这样就不用写第二个参数
    * */
    $scope.$watch('entity.goods.category1Id', function (newId, oldId) {
        if (newId != undefined && newId != oldId) {
            /*当选中的一级列表发生改变时执行,查询到二级列表*/
            itemCatService.findByParentId(newId).success(
                function (response) {
                    if (response.success) {
                        $scope.itemCat2List = response.obj;
                        /*应该选中第一个后表示重新加载，清空第三个，加载第二个*/
                        $scope.itemCat3List = {};
                        /*选中一级分类后应该清空分类对应的模板id*/
                        $scope.entity.goods.typeTemplateId = "";
                    } else {
                        swal("", "列表加载失败", "error");
                    }
                }
            )
        }
    });

    $scope.$watch('entity.goods.category2Id', function (newId, oldId) {
        if (newId != undefined && newId != oldId) {
            itemCatService.findByParentId(newId).success(
                function (response) {
                    if (response.success) {
                        $scope.itemCat3List = response.obj;
                        /*选中二级分类后应该清空分类对应的模板id*/
                        $scope.entity.goods.typeTemplateId = "";
                    } else {
                        swal("", "列表加载失败", "error");
                    }
                }
            )
        }
    });

    /*三级列表选中之后读取商品分类的模板id*/
    $scope.$watch('entity.goods.category3Id', function (newId, oldId) {
        if (newId != undefined && newId != oldId) {
            itemCatService.findOne(newId).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            )
        }
    });
    /*
        tb_item_cat 找到具体的商品类型，goods记录多级的商品类型。
        * 通过tb_item_cat 找到模板表，模板表记录商品类型对应的多个商品信息
        * 所以可以使用下拉框选择。
        * 模板表中有brand相关信息
        * goods需要的是brandIds，显示brand.text,而实际值是brandIds.
        *
        * goodsDesc需要customAttributeItems，显示customAttributeItems.text。
        * 作为表单可以录入value。也就是最后存在数据库的customAttributeItems也是json格式。
        * 这些表记录的都是SPU信息，goods就是SPU goodsDesc是其拓展信息，用来记录大数据的字段值
        * goods.id=goodsDesc.id 其实他们可以看做一张表。而保存实际是将其他表和goods关联起来
        * */


    // 当模板id有值后执行
    $scope.$watch('entity.goods.typeTemplateId', function (newId, oldId) {
        if (newId != undefined && newId != oldId && newId != "") {
            typeTemplateService.findOne(newId).success(
                function (response) {
                    // 继续通过模板id查询typeTemplate具体信息
                    // goods只需要brandsId,typeTemplate用来显示下拉框信息
                    $scope.typeTemplate = response;
                    // 因为要使用到brandsId所以先将字符串格式的json转换为对象
                    // 之后请求的时候属性为String就会重新变成String类型的json
                    $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);
                    if ($location.search()['id'] == null) {
                        // 修改的时候不能使用该方式初始化拓展属性，因为是查询的typeTemplate
                        // 所以value是空
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
                    }
                }
            );
            // 模板表有值后去查询spec和对应的specOptions
            typeTemplateService.findSpecList(newId).success(
                function (response) {
                    $scope.specList = response;
                }
            );
        }
    });

    // 初始化specificationItems
    // [{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
    $scope.entity.goodsDesc.specificationItems = [];

    // 规格选中后执行
    $scope.updateSpecAttribute = function (event, name, value) {
        var items = $scope.entity.goodsDesc.specificationItems;
        var item = $scope.searchObjectByKey(items, 'attributeName', name);
        if (item != null) {
            if (event.target.checked) {
                item.attributeValue.push(value);
            } else {
                item.attributeValue.splice(item.attributeValue.indexOf(value), 1);
                if (item.attributeValue.length == 0) {
                    items.splice(items.indexOf(item, 1));
                }
            }
        } else {
            if (event.target.checked) {
                items.push({"attributeName": name, "attributeValue": [value]});
            }
        }
    };

    //创建SKU列表
    /*
    *
[{"spec":{"网络":"移动3G","机身内存":"16G"},"price":0,"num":99999,"status":"0","isDefault":"0"},{"spec":{"网络":"移动4G","机身内存":"16G"},"price":0,"num":99999,"status":"0","isDefault":"0"}]
    * */
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}];//初始
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {

            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    };
    // [{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]


    //添加列值
    // [{“attributeName”:”规格名称”,”attributeValue”:[“规格选项1”,“规格选项2”.... ]  } , ....  ]
    addColumn = function (list, columnName, columnValues) {
        var newList = [];//新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    };


    /*页面上一些状态或者只有id信息的一种解决方式：
    * 可以使用数组来记录要显示的信息，数组索引为id或者状态代表的数字
    * 就直接可以通过数组索引在页面上显示出要显示的信息
    * */
    $scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

    $scope.auditStatusList = [];
    var auditStatusListInit = function () {
        itemCatService.findAll().success(
            function (response) {
                /*response是一个数组
                * 遍历数组的所有元素
                * 对auditStatusList进行初始化
                * */
                $(response).each(function () {
                    $scope.auditStatusList[this.id] = this.name;
                });
            }
        )
    };
    auditStatusListInit();

    // 判断规格specName和对应的optionName是否存在
    $scope.checkAttributeValue=function(specName,optionName){
        // 规格
            var items= $scope.entity.goodsDesc.specificationItems;
            var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if (object != null) {// 不是null，规格存在继续判断
            if (object.attributeValue.indexOf(optionName)>=0) { //规格和对应的规格列表都存在
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    };

    $scope.updateMarketable = function (status) {
        goodsService.updateMarketable(status, $scope.selectIds).success(
            function (response) {
                if (response.success) {
                    swal("", "商品状态更新成功", "success");
                } else {
                    swal("", "商品状态更新失败", "error");
                }
            }
        )
    };
});

