app.service("cartService", function ($http) {
    // 查找购物车列表
    this.findCartList = function () {
        return $http.get("cart/findCartList.do");
    };

    // 添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    };

    this.mergerCartList=function(){
        return $http.get("cart/mergerCartList.do");
    };

    this.searchOrderItemBySellerIdAndItemId = function (cartList, sellerId, itemId) {
        for (var i = 0; i < cartList.length; i ++) {
            if (cartList[i].sellerId == sellerId) {
                for (var j = 0; j < cartList[i].orderItemList.length; j++) {
                    if (cartList[i].orderItemList[j].itemId == itemId) {
                        return cartList[i].orderItemList[j];
                    }
                }
            }
        }
        return null;
    };

    this.searchCartBySellerId = function (cartList, sellerId) {
        for (var i = 0; i < cartList.length; i++) {
            if (cartList[i].sellerId == sellerId) {
                return cartList[i];
            }
        }

        return null;
    };

    // 传来的是引用。改变返回值还是改变引用空间内数据。
    this.searchOrderItemByItemId = function (orderItemList, itemId) {
        for (var i = 0; i < orderItemList.length; i++) {
            if (orderItemList[i].itemId == itemId) {
                return orderItemList[i];
            }
        }

        return null;
    };

    this.isCartInCartList = function (cartList, cart) {
        for (var i = 0; i < cartList.length; i++) {
            if (cartList[i].sellerId == cart.sellerId) {
                return true;
            }
        }
        return false;
    };

    this.isOrderItemInOrderItemList = function (orderItemList, orderItem) {
        for (var i = 0; i < orderItemList.length; i++) {
            if (orderItemList[i].itemId == orderItem.itemId) {
                return true;
            }
        }
        return false;
    };

    //获取地址列表
    this.findAddressList=function(){
        return $http.get('address/findListByLoginUser.do');
    };

    this.insertCheckOrderItemList = function (itemId, num) {
        return $http.get("cart/insertCheckOrderItemList.do?itemId=" + itemId + "&num=" + num);
    };

    this.findCheckOrderItemList = function () {
        return $http.get("cart/findCheckOrderItemList.do");
    };

    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }
});