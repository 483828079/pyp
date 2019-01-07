app.controller("searchController", function ($scope, $location ,searchService) {
    // 初始化搜索项
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{}, 'price':'', 'pageNo':'1', 'pageSize':'40', 'sort' : '', 'sortField' : ''};
    $scope.resultMap={'totalPage':0, 'total':0, "brandList" :[]};
    // 添加搜索项
    $scope.addSearchItem=function(key,value){
        if (key == "category" || key == "brand" || key == "price") {
            // 如果是category,brand或者price，直接将key对应的value存储。
            $scope.searchMap[key] = value;
        } else {
            // 只能是spec，如果是spec，在spec中添加key和value.(属性和属性值)
            // 会替换掉相同的key
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    };

    $scope.removeSearchItem = function(key) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = "";
        } else {
            // 清除spec中对应属性
            delete $scope.searchMap.spec[key];
        }
        // 在对查询条件进行增加或者删除之后重新进行查询
        $scope.search();
    };

    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                if (response.rows != null) {
                    $scope.resultMap = response;
                    // 每次查询的时候都去构建分页页码
                    buildPageLable();
                }
            }
        );
    };

    // 根据分页进行查询
    $scope.findByPage = function(page) {
        // 如果当前页小于最小页大于最大页，不用进行查询。
        if (page < 1 || page > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = page + "";
        $scope.search();
        goto_top();
    };

    // 下一页
    $scope.nextPage = function() {
        var currentPage = parseInt($scope.searchMap.pageNo) + 1;
        $scope.findByPage(currentPage);
    };

    // 上一页
    $scope.prePage = function() {
        var currentPage = parseInt($scope.searchMap.pageNo) - 1;
        $scope.findByPage(currentPage);
    };

    // 是否是第一页
    $scope.isTopPage = function() {
          if($scope.searchMap.pageNo == 1) {
              return true;
          }   else {
              return false;
          }
    };

    // 是否是最后一页
    $scope.isEndPage = function() {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        } else {
            return false;
        }
    };

    // 构建分页页码
    buildPageLable = function () {
        $scope.pageLable = []; // 分页栏
        var maxPageNo = $scope.resultMap.totalPages;
        var startPage = 1; // 开始页码
        var endPage = maxPageNo;// 结束页码
        var currentPage = parseInt($scope.searchMap.pageNo);
        // 如果小于等于3页，显示5页。
        if (maxPageNo > 5) { // 如果总页数大于5，显示5页。
            if ($scope.searchMap.pageNo <= 3) {
                endPage = 5; // 如果当前页等于3或者小于3，都显示5页。
                $scope.preNot = false;
                $scope.nextNot = true;
            } else if ($scope.searchMap.pageNo >= maxPageNo - 2) {
                startPage = endPage -4;
                $scope.preNot = false;
                $scope.nextNot = true;
            } else {
                startPage = currentPage - 2;
                endPage = currentPage + 2;
                $scope.preNot = true;
                $scope.nextNot = true;
            }
        } else {
            $scope.preNot = false;
            $scope.nextNot = false;
        }
        for (var i = startPage; i <= endPage; i++) { // 构建分页页码
            $scope.pageLable.push(i);
        }
    };

    $scope.sortSearch = function (sort, sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();
    };

    $scope.updateSortActive = function (event) {
        var ele = event.target;
        if ($(ele).text() == "价格↑") {
            $(ele).text("价格↓");
            $scope.sortSearch('ASC', 'price');
        } else if ($(ele).text() == "价格↓") {
            $(ele).text("价格↑");
            $scope.sortSearch('DESC', 'price');
        }

        $(ele).parent("li").addClass("active").siblings("li").removeClass("active");
    };

    // 判断keywords是不是包含了品牌，如果包含了品牌就隐藏品牌列表
    $scope.keywordsIsBrand = function () {
        var brandList = $scope.resultMap.brandList;
        var keywords = $scope.searchMap.keywords;
        for (var i = 0; i < brandList.length; i++) {
            if (keywords.indexOf(brandList[i].text) >= 0) {
                return true;
            }
        }
        return false;
    };

    $scope.loadKeywordsSearch = function () {
        var keywords = $location.search()['keywords'];
        if (keywords) {
            $scope.searchMap.keywords = keywords;
            $scope.search();
        }
    };
});