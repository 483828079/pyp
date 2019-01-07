//控制层
app.controller('seckillGoodsController' ,function($scope,$location, $interval,seckillGoodsService){
    //读取列表数据绑定到表单中
    $scope.findList=function(){
        seckillGoodsService.findList().success(
            function(response){
                $scope.list=response;
            }
        );
    };

    //查询实体
    $scope.findOne=function(){
        seckillGoodsService.findOne($location.search()['id']).success(
            function(response){
                $scope.entity= response;
                // 加载页面信息后，开启定时。对活动结束时间-活动开始时间。
                allsecond =Math.floor( (  new Date($scope.entity.endTime).getTime()- (new Date().getTime())) /1000); //总秒数
                var time= $interval(function(){
                    if(allsecond>0){ // 如果剩余时间>0，定时器继续，并且时间每过1s-1
                        allsecond =allsecond-1;
                        $scope.timeString=convertTimeString(allsecond);//转换时间字符串
                    }else{
                        $interval.cancel(time); // 清除定时器
                        swal("", "秒杀服务已结束", "error");
                    }
                },1000);
            }
        );
    };


    //转换秒为   天小时分钟秒格式  XXX天 10:22:33
    convertTimeString=function(allsecond){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小数数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";
        if(days>0){
            timeString=days+"天 ";
        }
        return timeString+hours+":"+minutes+":"+seconds;
    };

    //提交订单
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function(response){
                if(response.success){
                    swal("", "下单成功，请在5分钟内完成支付", "success");
                    location.href="pay.html";
                }else{
                    swal("", response.message, "error");
                }
            }
        );
    }
});