app.controller('payController' ,function($scope,$location,payService){
    // 生成支付二维码
    $scope.createNative=function(){
        payService.createNative().success(
            function (response) {
                // 设置金额和订单号，用来回显到页面上。
                $scope.money=  (response.total_fee/100).toFixed(2) ;	//金额
                $scope.out_trade_no= response.out_trade_no;//订单号
                // 在页面生成支付二维码
                var qr = new QRious({
                    // 要生成二维码的元素位置
                    element:document.getElementById('qrious'),
                    size:250, // 二维码带下
                    level:'H', // 细度
                    value:response.code_url // 生成二维码的内容
                });
                // 通过响应回来的订单号监听是否支付成功。
                $scope.queryPayStatus(response.out_trade_no);
            }
        )
    };

    // 根据订单号查询订单状态。
    $scope.queryPayStatus=function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(
            function(response){
                if(response.success){
                    location.href="paysuccess.html#?money="+$scope.money;
                }else{
                    if(response.message=='二维码超时'){
                        location.href="payTimeOut.html";
                    }else{
                        location.href="payfail.html";
                    }
                }
            }
        );
    }

    $scope.getMoney = function () {
        return $location.search()['money']
    }
});
