app.controller("cartController", function ($scope, cartService) {
    $scope.cartListTemp = [];
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                $scope.cartListTemp = JSON.parse(JSON.stringify(response));
            }
        )
    };


    // 添加商品到购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        console.log("add");
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    // 添加成功后，重新加载购物车列表
                    $scope.findCartList();
                } else {
                    swal("", response.message, "error");
                }
            }
        )
    };

    $scope.updateCartList = function (sellerId, itemId) {
        var orderItemTemp = cartService.searchOrderItemBySellerIdAndItemId($scope.cartListTemp, sellerId, itemId);
        var orderItem = cartService.searchOrderItemBySellerIdAndItemId($scope.cartList, sellerId, itemId);
        if (orderItem.num <= 0) {
            orderItem.num = orderItemTemp.num;
            return;
        }
        $scope.addGoodsToCartList(itemId, orderItem.num - orderItemTemp.num);
    };

    // 删除的商品列表.
    $scope.deleteCartList = [];
    $scope.deleteGoods = function (sellerId, itemId, num) {
        var cart = JSON.parse(JSON.stringify(cartService.searchCartBySellerId($scope.cartList, sellerId)));
        var orderItem = cartService.searchOrderItemByItemId(cart.orderItemList, itemId);
        if (cartService.isCartInCartList($scope.deleteCartList, cart)) {
            var orderItemList = cartService.searchCartBySellerId($scope.deleteCartList, cart.sellerId).orderItemList;
            if (cartService.isOrderItemInOrderItemList(orderItemList, orderItem)) {
                var resultOrderItem = cartService.searchOrderItemByItemId(orderItemList, orderItem.itemId);
                resultOrderItem.num += num;
                resultOrderItem.totalFee = resultOrderItem.num * resultOrderItem.price;
                if (resultOrderItem.num <= 0) {
                    $scope.deleteCartList.splice($scope.deleteCartList.indexOf(orderItemList));
                }
            } else {
                orderItemList.push(orderItem);
            }
        } else {
            orderItem.num = num;
            $scope.deleteCartList.push({
                "sellerId": sellerId,
                "sellerName": cart.sellerName,
                "orderItemList": [orderItem]
            });
        }

        //
        $scope.deleteOrderItemList = [];
        // 移除一个商品后构建移除商品明细列表。
        $scope.createOrderItemList($scope.deleteCartList, $scope.deleteOrderItemList);
    };

    // 商品明细列表
    $scope.deleteOrderItemList = [];
    // 使用删除的购物车列表生成所有的商品明细列表
    $scope.createOrderItemList = function (cartList, orderItemList) {
        var deleteCartList = JSON.parse(JSON.stringify(cartList));
        for (var i = 0; i < deleteCartList.length; i++) {
            for (var j = 0; j < deleteCartList[i].orderItemList.length; j++) {
                var deleteOrderItem = deleteCartList[i].orderItemList[j];
                if (cartService.isOrderItemInOrderItemList(orderItemList, deleteOrderItem)) {
                    var resultDeleteOrderItem = cartService.searchOrderItemByItemId(orderItemList, deleteOrderItem.itemId);
                    resultDeleteOrderItem.num += deleteOrderItem.num;
                    resultDeleteOrderItem.totalFee += deleteCartList.totalFee;
                } else {
                    orderItemList.push(deleteOrderItem);
                }
            }
        }
    };


    $scope.updateDeleteCartList = function (itemId, num) {
        $scope.deleteGoods(searchSellerIdByItemId(itemId), itemId, num);
    };

    var searchSellerIdByItemId = function (itemId) {
        var cartList = JSON.parse(JSON.stringify($scope.cartList));
        for (var i = 0; i < cartList.length; i++) {
            var orderItemList = cartList[i].orderItemList;
            for (var j = 0; j < orderItemList.length; j++) {
                if (orderItemList[j].itemId == itemId) {
                    return cartList[i].sellerId;
                }
            }
        }
    };

    $scope.mergerCartList = function () {
        cartService.mergerCartList().success(
            function (response) {
                location.href = "cart.html";
            }
        )
    };

    // 将选中的商品保存到redis。
    $scope.checkedOrderItem = function (event, orderItem) {
        if (event.target.checked) {
            $scope.updateCheckedOrderItem(orderItem.itemId, orderItem.num);
        } else {
            $scope.updateCheckedOrderItem(orderItem.itemId, -orderItem.num);
        }
    };

    // 将选中的商品保存到redis，并初始化商品详情信息，总价格，总数。
    $scope.updateCheckedOrderItem = function(itemId, num) {
        cartService.insertCheckOrderItemList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCheckOrderItemList();
                } else {
                    swal("", response.message, "error");
                }
            }
        )
    };

    // 更改选中商品在redis中的数量
    $scope.updateCheckedOrderItemNum = function(itemId, num) {
        if ($scope.isChecked(itemId)) {
            $scope.updateCheckedOrderItem(itemId, num);
        }
    };

    // 初始化商品详情信息，总价格，总数。
    $scope.totalValue = {"totalMoney" : 0.0, "totalNum" : 0};
    $scope.findCheckOrderItemList = function () {
        cartService.findCheckOrderItemList().success(
            function (response) {
                $scope.selectedCartList = response;
                $scope.totalValue = {"totalMoney" : 0.0, "totalNum" : 0};
                for (var i = 0; i < $scope.selectedCartList.length; i++) {
                    var selectedOrderItemList = $scope.selectedCartList[i].orderItemList;
                    for (var j = 0; j < selectedOrderItemList.length; j++) {
                        $scope.totalValue.totalMoney += selectedOrderItemList[j].totalFee;
                        $scope.totalValue.totalNum += selectedOrderItemList[j].num;
                    }
                }
            }
        )
    };

    // 判断该商品是否已经被选中
    $scope.isChecked = function(itemId) {
        for (var i = 0; i < $scope.selectedCartList.length; i ++) {
            var selectedOrderItem = $scope.selectedCartList[i].orderItemList;
            for (var j = 0; j < selectedOrderItem.length; j++) {
                if (selectedOrderItem[j].itemId == itemId) {
                    return true;
                }
            }
        }

        return false;
    };

    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;
                for (var i = 0; i < $scope.addressList.length; i++) {
                    if ($scope.addressList[i].isDefault == '1') {
                        $scope.address = $scope.addressList[i];
                    }
                }
            }
        )
    };

    $scope.isSelectedAddress = function (address) {
        if (address == $scope.address) {
            return true;
        } else {
            return false;
        }
    };

    $scope.selectAddress = function (address) {
        // 将选中的address作为需要提交的address
        $scope.address = address;
    };

    $scope.addressHoverShow = function (event) {
        $(".address").removeClass("address-hover");
        $(".address span").removeClass("address-hover");
        $(event.target).addClass("address-hover");
    };

    // 支付类型
    $scope.order = {"paymentType": "1"};
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    };

    //保存订单
    $scope.submitOrder=function(){
        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人
        cartService.submitOrder( $scope.order ).success(
            function(response){
                if(response.success){
                    //页面跳转
                    if($scope.order.paymentType=='1'){//如果是微信支付，跳转到支付页面
                        location.href="pay.html";
                    }else{//如果货到付款，跳转到提示页面
                        location.href="paysuccess.html";
                    }
                }else{
                    location.href="payfail.html";
                }
            }
        );
    };


});