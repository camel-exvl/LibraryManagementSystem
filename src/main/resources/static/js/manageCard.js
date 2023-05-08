document.getElementById("manageCardHeaderButton").className += " layui-this";
document.getElementById("queryMenu").className = "layui-nav-item";
document.getElementById("manageMenu").className = "layui-nav-item layui-nav-itemed";
var manageCardManageSelectValue = "new";
var manageCardManageTypeSelectValue = "Student";

layui.use(function () {
    var form = layui.form;
    var layer = layui.layer;

    form.on("select(manageCardManageSelect)", function (data) {
        initSelected(data.value, manageCardManageTypeSelectValue);
        form.render("select");
    });

    form.on("select(manageCardManageTypeSelect)", function (data) {
        initSelected(manageCardManageSelectValue, data.value);
    });

    form.on("submit(manageCardManageButton)", function (data) {
        if (manageCardManageSelectValue === "new") {
            if (document.getElementById("manageCardManageName") === "" || document.getElementById("manageBookManageDepartment") === "") {
                layer.msg("请填写完整信息");
                return;
            }
        } else if (manageCardManageSelectValue === "delete") {
            if (document.getElementById("manageCardManageID") === "") {
                layer.msg("请填写完整信息");
                return;
            }
        }
        window.location.href = "/manage/manageCard?manageCardManageSelect=" + manageCardManageSelectValue + "&manageCardManageID=" + document.getElementById("manageCardManageID").value + "&manageCardManageName=" + document.getElementById("manageCardManageName").value + "&manageCardManageDepartment=" + document.getElementById("manageCardManageDepartment").value + "&manageCardManageTypeSelect=" + manageCardManageTypeSelectValue;
    });
});

function initSelected(manageCardManageSelect, manageCardManageTypeSelect) {
    manageCardManageSelectValue = manageCardManageSelect;
    manageCardManageTypeSelectValue = manageCardManageTypeSelect;
    if (manageCardManageSelect === "delete") {
        document.getElementById("manageCardManageID").disabled = false;
        document.getElementById("manageCardManageName").disabled = true;
        document.getElementById("manageCardManageDepartment").disabled = true;
        document.getElementById("manageCardManageTypeSelect").disabled = true;
    } else if (manageCardManageSelect === "new") {
        document.getElementById("manageCardManageID").disabled = true;
        document.getElementById("manageCardManageName").disabled = false;
        document.getElementById("manageCardManageDepartment").disabled = false;
        document.getElementById("manageCardManageTypeSelect").disabled = false;
    }
}