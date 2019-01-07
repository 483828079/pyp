app.service("searchService", function ($http) {
    this.search = function (searchMap) {
        return $http.post("itemSearch/search.do", searchMap); // 该地址由引用的页面在工程中的路径决定
    };
});