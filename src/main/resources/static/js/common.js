layui.use(['element', 'layer', 'util'], function () {
    var element = layui.element;
    var layer = layui.layer;
    var util = layui.util;
    var $ = layui.$;

    //头部事件
    util.event('lay-header-event', {
        queryBookPage: function () {
            window.location.href = "/query/queryBook";
        },
        queryBorrowPage: function () {
            window.location.href = "/query/queryBorrow";
        },
        queryCardPage: function () {
            window.location.href = "/query/queryCard";
        },
        borrowBookPage: function () {
            window.location.href = "/borrowBook";
        }
    });
});