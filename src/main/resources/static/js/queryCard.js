const queryCardHeaderButton = document.getElementById("queryCardHeaderButton");
queryCardHeaderButton.className += " layui-this";

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