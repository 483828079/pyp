

app.controller("itemController", function ($scope, $http) {

    /**
     * 流程分析：
     * 和之前的不同页面上需要变动的数据，从使用ajax从后端响应回来
     * 然后使用AngularJS的表达式在页面上替换，现在所有变动的数据由freemarker来生成。
     * 而AngularJS只是用来做可以重复的处理。
     *
     * specificationItems[] 用来记录规格信息。
     * 每一次点击规格选项卡的时候调用。修改key和value。
     * 对于改变选项卡的样式，判断每个选项卡是否是specificationItems[]记录的。
     * 也就是，点击之后保存key和value到数组。如果选项卡是specificationItems[]
     * 自动更改样式。得益于AngularJS的双向绑定。
     *
     * 更改默认规格，
     * 通过is_default倒序遍历sku，第一个就是默认的规格。
     * 当加载页面的时候，使用specificationItems[]来记录sku.spec[0]就能显示样式和
     * 记录默认sku.spec。
     *
     * 更改默认选项卡后，更改其他的sku信息
     *  遍历sku，取出每个sku.spec对specificationItems[]中记录的规格进行匹配。
     *  如果匹配成功，将当前sku设置为匹配成功sku。
     *
     *  对于javascript对象比较是否相等
     *  首先先了解什么是声明
     *  var a = {}声明一个a，对象。只有声明了该对象才可以使用它的属性
     *  比如 直接使用a.b = "A"，就是错误行为。
     *  a.b = "A"。 声明a的属性b并初始化。
     *
     *
     *  var a = {};
         a.b = "A";
         console.log(a.b)
     *  var b = {};
         b.c = "A";
         console.log(a.b == b.c)

        毫无疑问 a.b == b.c为true。

     *
     * var a = {};
       var b = {};
       console.log(a.b);
       console.log(a.b == b.c);
     也是true，打印它们的结果可以发现
     undefined 都是。也就是undefined = undefined为true。

     比较两个对象，这里用的是遍历key，比较值的方式。
     对于两个对象，取出它们的key，然后比较其值。
     比较次数等于两个对象key的个数和。
     那它们会不会出现两个key不存在都是undefined却相等的情况？
        不会，因为每次比较两个对象使用的都是同一个key，而key必须存在。
        所以如果相等那么key和值都是相等的。
        如果一个对象的key不存在，他不会报错，只是值是undefined
        也就是匹配不上另一个key存在又不是undefined，会返回true。
        所以如果可以，在遍历匹配之前最好先判断他们长度是否相等。
        如果长度不等，肯定两个对象不一样。

     这里再加上一点
        如果对象没声明就使用它的对象。
     ReferenceError: a is not defined，就会报错，报错和undefined是不同的。
     javascript的报错，之后的代码都不会执行。。。
     所以使用对象的属性之前记得要先把对象声明。
     * */

    $scope.addNum = function (num) {
        $scope.num = parseInt($scope.num);
        $scope.num += num;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };


    $scope.specificationItems = {};
    $scope.selectSpecification = function (key, value) {
        /*如果key不存在，声明key并初始化值value
        * 如果key存在，替换key对应的value
        * */
        $scope.specificationItems[key] = value;
        // 修改规格选项卡之后重新加载sku
        searchSku();
    };

    $scope.isSelected = function (key, value) {
        // 如果当前选项卡，是在selectSpecification列表中的选项卡，表名它为选中状态。
        if ($scope.specificationItems[key] == value) {
            return true;
        } else {
            return false;
        }
    };

    $scope.loadSku = function () {
        // 因为是按照默认sku进行排序，所以使用第一个sku来默认显示在页面
        $scope.sku = skuList[0];
        // 设置默认的规格选项卡
        // 使用深度copy使specificationItems和sku.spec指向地址不一致。
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
    };

    /*判断当前选中的sku是哪个*/
    var searchSku = function() {
        // 更改规格选项卡之后，修改对应的sku信息.
        // 因为specificationItems记录着sku所属的某个规格。所以可以通过
        // specificationItems匹配规格来确定是哪个sku
        for (var i = 0; i < skuList.length; i++) {
            if ($scope.searchObject($scope.specificationItems, skuList[i].spec)) {
                $scope.sku = skuList[i];
                return;
            }
        }
        // 如果没有匹配到，设置错误信息。
        $scope.sku={id:0,title:'--------',price:0};
    };

    // 比较两个对象是否相等，key对应的值是否相同
    $scope.searchObject = function (objA,objB) {
        if (objA.length != objB.length) {
            return false;
        }

        for (key in objA) {
            if (objA[key] != objB[key]) {
                return false;
            }
        }

        for (key in objB) {
            if (objA[key] != objB[key]) {
                return false;
            }
        }

        return true;
    };

    //添加商品到购物车
    $scope.addToCart=function(){
        // AJAX请求必须打开withCredentials属性，服务端必须允许设置cookie。
        // 浏览器才会发送cookie信息。如果只在AJAX设置会使用cookie但是不会接到响应。
        $http.get("http://localhost:2001/cart/addGoodsToCartList.do?itemId="
            +$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(
            function (response) {
                if (response.success) {
                    // 添加到购物车成功后跳转到购物车页面。
                    location.href = "http://localhost:2001/cart.html";
                } else {
                    swal("", response.message, "error")
                }
            }
        );
    };
});


