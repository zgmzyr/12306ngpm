window.fix_input_boxes = function (dialog) {
    $.preferCulture("zh-CN");
    var context = dialog !== undefined ? dialog : document;

    if ($(".DateTimeClass", dialog).size() > 0) {
        $(".DateTimeClass", dialog).datetimepicker({
            timeFormat: 'hh:mm',
            stepHour: 1,
            stepMinute: 5
        });
    }

    if ($(".DateClass", dialog).size() > 0) {
        $(".DateClass", dialog).datepicker();
    }

    // TODO: 在使用jquery.dialog方法打开的对话框里，下面的语句不起作用
    if ($(".HtmlClass", dialog).size() > 0) {
        $(".HtmlClass", dialog).tinymce({
            mode: "textareas",
            theme: "simple"
        });
    }
};