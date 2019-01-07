app.service('seckillGoodsService',function($http){
    //读取列表数据绑定到表单中
    this.findList=function(){
        return $http.get('seckillGoods/findList.do');
    };

    // 获取商品详情页信息
    this.findOne=function(id){
        return $http.get('seckillGoods/findOneFromRedis.do?id='+id);
    };

    //提交订单
    this.submitOrder=function(seckillId){
        return $http.get('seckillOrder/submitOrder.do?seckillId='+seckillId);
    };
});