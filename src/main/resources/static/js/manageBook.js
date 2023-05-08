document.getElementById("manageBookHeaderButton").className += " layui-this";
document.getElementById("queryMenu").className = "layui-nav-item";
document.getElementById("manageMenu").className = "layui-nav-item layui-nav-itemed";
var manageBookManageSelectValue = "new";

layui.use(function () {
    var form = layui.form;
    var layer = layui.layer;

    form.on("select(manageBookManageSelect)", function (data) {
        initSelected(data.value);
    });

    form.on("submit(manageBookManageButton)", function (data) {
        if (manageBookManageSelectValue === "new") {
            if (document.getElementById("manageBookManageCategory").value === "" || document.getElementById("manageBookManageTitle").value === "" || document.getElementById("manageBookManagePress").value === "" || document.getElementById("manageBookManagePublisherYear").value === "" || document.getElementById("manageBookManageAuthor").value === "" || document.getElementById("manageBookManagePrice").value === "" || document.getElementById("manageBookManageStock").value === "") {
                layer.msg("请填写完整信息");
                return;
            }
        } else if (manageBookManageSelectValue === "update") {
            if (document.getElementById("manageBookManageID").value === "" || document.getElementById("manageBookManageCategory").value === "" || document.getElementById("manageBookManageTitle").value === "" || document.getElementById("manageBookManagePress").value === "" || document.getElementById("manageBookManagePublisherYear").value === "" || document.getElementById("manageBookManageAuthor").value === "" || document.getElementById("manageBookManagePrice").value === "") {
                layer.msg("请填写完整信息");
                return;
            }
        } else if (manageBookManageSelectValue === "delete" || manageBookManageSelectValue === "stock") {
            if (document.getElementById("manageBookManageID").value === "") {
                layer.msg("请填写完整信息");
                return;
            }
        }
        window.location.href = "/manage/manageBook?manageBookManageSelect=" + manageBookManageSelectValue + "&manageBookManageID=" + document.getElementById("manageBookManageID").value + "&manageBookManageCategory=" + document.getElementById("manageBookManageCategory").value + "&manageBookManageTitle=" + document.getElementById("manageBookManageTitle").value + "&manageBookManagePress=" + document.getElementById("manageBookManagePress").value + "&manageBookManagePublisherYear=" + document.getElementById("manageBookManagePublisherYear").value + "&manageBookManageAuthor=" + document.getElementById("manageBookManageAuthor").value + "&manageBookManagePrice=" + document.getElementById("manageBookManagePrice").value + "&manageBookManageStock=" + document.getElementById("manageBookManageStock").value;
    });
});

function initSelected(manageBookManageSelect) {
    manageBookManageSelectValue = manageBookManageSelect;
    if (manageBookManageSelect === "delete" || manageBookManageSelect === "stock") {
        document.getElementById("manageBookManageID").disabled = false;
        document.getElementById("manageBookManageCategory").disabled = true;
        document.getElementById("manageBookManageTitle").disabled = true;
        document.getElementById("manageBookManagePress").disabled = true;
        document.getElementById("manageBookManagePublisherYear").disabled = true;
        document.getElementById("manageBookManageAuthor").disabled = true;
        document.getElementById("manageBookManagePrice").disabled = true;
        if (manageBookManageSelect === "delete") {
            document.getElementById("manageBookManageStock").disabled = true;
        }
        else {
            document.getElementById("manageBookManageStock").disabled = false;
            document.getElementById("manageBookManageStock").value = "";
        }
    } else {
        document.getElementById("manageBookManageCategory").disabled = false;
        document.getElementById("manageBookManageTitle").disabled = false;
        document.getElementById("manageBookManagePress").disabled = false;
        document.getElementById("manageBookManagePublisherYear").disabled = false;
        document.getElementById("manageBookManageAuthor").disabled = false;
        document.getElementById("manageBookManagePrice").disabled = false;
        if (manageBookManageSelect === "new") {
            document.getElementById("manageBookManageID").disabled = true;
            document.getElementById("manageBookManageStock").disabled = false;
        } else if (manageBookManageSelect === "update") {
            document.getElementById("manageBookManageID").disabled = false;
            document.getElementById("manageBookManageStock").disabled = true;
        }
    }
    if (manageBookManageSelect === "stock") {
        document.getElementById("manageBookManageStock").placeholder = "请输入库存改变量";
    } else {
        document.getElementById("manageBookManageStock").placeholder = "请输入库存";
    }
}