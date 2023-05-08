const queryBookHeaderButton = document.getElementById("queryBookHeaderButton");
queryBookHeaderButton.className += " layui-this";
var queryBookSelectValue = "category";
var queryBookSortByValue = "BOOK_ID";
var queryBookSortOrderValue = "ASC";

layui.use(function () {
    var form = layui.form;
    var layer = layui.layer;

    form.on("select(queryBookSelect)", function (data) {
        initSelected(data.value, queryBookSortByValue, queryBookSortOrderValue);
    });

    form.on("select(queryBookSortBy)", function (data) {
        queryBookSortByValue = data.value;
    });

    form.on("select(queryBookSortOrder)", function (data) {
        queryBookSortOrderValue = data.value;
    });

    form.on("submit(queryBookSearchButton)", function (data) {
        queryBookSearch("queryBookSearchButton");
    });
    
    form.on("submit(queryBookSearchTwiceButton)", function (data) {
        queryBookSearch("queryBookSearchTwiceButton");
    });
});

function queryBookSearch(mode) {
    const queryBookSearchInput = document.getElementById("queryBookSearchInput");
    const queryBookSearchRangeMinInput = document.getElementById("queryBookSearchRangeMinInput");
    const queryBookSearchRangeMaxInput = document.getElementById("queryBookSearchRangeMaxInput");
    if (queryBookSelectValue === "publishYear" || queryBookSelectValue === "price") {
        if (queryBookSearchRangeMinInput.value === "" || queryBookSearchRangeMaxInput.value === "") {
            layer.msg("请输入范围");
            return false;
        }
        if (queryBookSearchRangeMinInput.value > queryBookSearchRangeMaxInput.value) {
            layer.msg("最小值不能大于最大值");
            return false;
        }
        if (mode === "queryBookSearchButton") {
            window.location.href = "/query/queryBook?queryBookSelect=" + queryBookSelectValue + "&queryBookSearchRangeMin=" + queryBookSearchRangeMinInput.value + "&queryBookSearchRangeMax=" + queryBookSearchRangeMaxInput.value + "&queryBookSortBy=" + queryBookSortByValue + "&queryBookSortOrder=" + queryBookSortOrderValue;
        } else {
            window.location.href = "/query/queryBook?queryBookSelect=" + queryBookSelectValue + "&queryBookSearchRangeMin=" + queryBookSearchRangeMinInput.value + "&queryBookSearchRangeMax=" + queryBookSearchRangeMaxInput.value + "&queryBookSortBy=" + queryBookSortByValue + "&queryBookSortOrder=" + queryBookSortOrderValue + "&queryBookSearchTwice=1";
        }
    } else {
        if (queryBookSearchInput.value === "") {
            layer.msg("请输入查询内容");
            return false;
        }
        if (mode === "queryBookSearchButton") {
            window.location.href = "/query/queryBook?queryBookSelect=" + queryBookSelectValue + "&queryBookSearch=" + queryBookSearchInput.value + "&queryBookSortBy=" + queryBookSortByValue + "&queryBookSortOrder=" + queryBookSortOrderValue;
        } else {
            window.location.href = "/query/queryBook?queryBookSelect=" + queryBookSelectValue + "&queryBookSearch=" + queryBookSearchInput.value + "&queryBookSortBy=" + queryBookSortByValue + "&queryBookSortOrder=" + queryBookSortOrderValue + "&queryBookSearchTwice=1";
        }
    }
    return true;
}

function initSelected(queryBookSelect, queryBookSortBy, queryBookSortOrder) {
    queryBookSelectValue = queryBookSelect;
    queryBookSortByValue = queryBookSortBy;
    queryBookSortOrderValue = queryBookSortOrder;

    const queryBookSearch = document.getElementById("queryBookSearch");
    const queryBookSearchRangeMin = document.getElementById("queryBookSearchRangeMin");
    const queryBookSearchRangeMax = document.getElementById("queryBookSearchRangeMax");
    if (queryBookSelectValue === "publishYear" || queryBookSelectValue === "price") {
        queryBookSearch.className = "layui-col-md6 layui-hide";
        queryBookSearchRangeMin.className = "layui-col-md3";
        queryBookSearchRangeMax.className = "layui-col-md3";
    } else {
        queryBookSearch.className = "layui-col-md6";
        queryBookSearchRangeMin.className = "layui-col-md3 layui-hide";
        queryBookSearchRangeMax.className = "layui-col-md3 layui-hide";
    }
}