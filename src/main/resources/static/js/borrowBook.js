const borrowBookHeaderButton = document.getElementById("borrowBookHeaderButton");
borrowBookHeaderButton.className += " layui-this";

layui.use(function () {
    var form = layui.form;
    var layer = layui.layer;

    form.on("submit(borrowBookBorrowButton)", function (data) {
        borrowBook("borrow");
    });

    form.on("submit(borrowBookReturnButton)", function (data) {
        borrowBook("return");
    });
});

function borrowBook(mode) {
    const borrowCardID = document.getElementById("borrowBookBorrowCardIDInput").value;
    const borrowBookID = document.getElementById("borrowBookBorrowBookIDInput").value;
    const returnCardID = document.getElementById("borrowBookReturnCardIDInput").value;
    const returnBookID = document.getElementById("borrowBookReturnBookIDInput").value;
    if (mode === "borrow") {
        if (borrowCardID === "") {
            layer.msg("借书证号不能为空！");
            return;
        }
        if (borrowBookID === "") {
            layer.msg("图书编号不能为空！");
            return;
        }
        window.location.href = "/borrowBook?mode=borrow&borrowCardID=" + borrowCardID + "&borrowBookID=" + borrowBookID + "&returnCardID=" + returnCardID + "&returnBookID=" + returnBookID;
    }else if (mode === "return") {
        if (returnCardID === "") {
            layer.msg("借书证号不能为空！");
            return;
        }
        if (returnBookID === "") {
            layer.msg("图书编号不能为空！");
            return;
        }
        window.location.href = "/borrowBook?mode=return&borrowCardID=" + borrowCardID + "&borrowBookID=" + borrowBookID + "&returnCardID=" + returnCardID + "&returnBookID=" + returnBookID;
    }
}