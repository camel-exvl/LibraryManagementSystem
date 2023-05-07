const queryBorrowHeaderButton = document.getElementById("queryBorrowHeaderButton");
queryBorrowHeaderButton.className += " layui-this";

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