document.getElementById("queryCardHeaderButton").className += " layui-this";
document.getElementById("queryMenu").className = "layui-nav-item layui-nav-itemed";
document.getElementById("manageMenu").className = "layui-nav-item";

layui.use(function () {
    var form = layui.form;
    var layer = layui.layer;

    form.on("submit(queryCardSearchButton)", function (data) {
        queryCardSearch("queryCardSearchButton");
    });
});

function queryCardSearch() {
    window.location.href = "/query/queryCard?queryCardSearch=1"
}