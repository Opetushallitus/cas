function jqueryReady() {
    $.ajaxSetup({headers: {'Caller-Id': 'cas'}});
    $.getJSON("/login-notifications/api/notifications", function (notifications) {
        var language = $("body").attr("data-language") || "fi";
        var textObjectName = "text_" + language;
        $.each(notifications, function (index, notification) {
            $("<div></div>", {
                "class": "row justify-content-center ilmoitus",
                "text": notification[textObjectName] || notification.text_fi
            }).prependTo("main");
        });
    });
}
