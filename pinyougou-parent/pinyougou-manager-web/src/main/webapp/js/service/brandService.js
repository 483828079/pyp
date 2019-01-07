app.service('brandService', function ($http) {
    this.search = function (page, pageSize, searchEntity) {
        return $http.post('../brand/findPage.do?page='+page+'&pageSize='+pageSize,searchEntity);
    };

    this.findOne = function (id) {
        return $http.get('../brand/findOne.do?id='+id);
    };

    this.delete = function (selectIds) {
        return  $http.get("../brand/delete.do?ids="+selectIds);
    };

    this.add = function (entity) {
        return $http.post('../brand/add.do',entity)
    };

    this.update = function (entity) {
        return $http.post('../brand/update.do',entity)
    };

    // 下拉表数据
    this.selectOptionList=function(){
        return $http.get('../brand/selectOptionList.do');
    };
});