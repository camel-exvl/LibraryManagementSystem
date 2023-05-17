document.getElementById("queryBorrowHeaderButton").className += " layui-this";
document.getElementById("queryMenu").className = "layui-nav-item layui-nav-itemed";
document.getElementById("manageMenu").className = "layui-nav-item";

layui.use(function () {
    var form = layui.form;
    var layer = layui.layer;

    form.on("submit(queryBorrowSearchButton)", function (data) {
        queryBorrowSearch("queryBorrowSearchButton");
    });
});

function queryBorrowSearch() {
    const queryBorrowSearchInputValue = document.getElementById("queryBorrowSearchInput");
    window.location.href = "/query/queryBorrow?queryBorrowSearch=" + queryBorrowSearchInputValue.value;
}