require(["jquery"], function( __tes__ ) {
    $(document).ready(function() {
        $.post(unreadAnnouncementUrl, function(data) {
            if (data > 0) {
                if ($(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").find(".badge").size() > 0) {
                    $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").find(".badge").html(data);
                } else {
                    $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").append(" <span class=\"badge\">" + data + "</span>");
                }
            } else {
                if ($(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").find(".badge").size() > 0) {
                    var html = $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").html();
                    var splitHtml = html.split(" ");
                    var res = [];
                    for (var i=0;i<=splitHtml.length-2;++i) {
                        res.push(splitHtml[i]);
                    }
                    $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").html(res.join(" "));
                }
            }
        }, "json");
        $.post(unreadClarificationUrl, function(data) {
            if (data > 0) {
                if ($(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").find(".badge").size() > 0) {
                    $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").find(".badge").html(data);
                } else {
                    $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").append(" <span class=\"badge\">" + data + "</span>");
                }
            } else {
                if ($(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").find(".badge").size() > 0) {
                    var html = $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").html();
                    var splitHtml = html.split(" ");
                    var res = [];
                    for (var i=0;i<=splitHtml.length-2;++i) {
                        res.push(splitHtml[i]);
                    }
                    $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").html(res.join(" "));
                }
            }
        }, "json");
        setInterval(function () {
            $.post(unreadAnnouncementUrl, function(data) {
                if (data > 0) {
                    if ($(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").find(".badge").size() > 0) {
                        $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").find(".badge").html(data);
                    } else {
                        $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").append(" <span class=\"badge\">" + data + "</span>");
                    }
                } else {
                    if ($(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").find(".badge").size() > 0) {
                        var html = $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").html();
                        var splitHtml = html.split(" ");
                        var res = [];
                        for (var i=0;i<=splitHtml.length-2;++i) {
                            res.push(splitHtml[i]);
                        }
                        $(".navbar-nav > li > a[href=\"" + contestAnnouncementUrl + "\"]").html(res.join(" "));
                    }
                }
            }, "json");
            $.post(unreadClarificationUrl, function(data) {
                if (data > 0) {
                    if ($(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").find(".badge").size() > 0) {
                        $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").find(".badge").html(data);
                    } else {
                        $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").append(" <span class=\"badge\">" + data + "</span>");
                    }
                } else {
                    if ($(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").find(".badge").size() > 0) {
                        var html = $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").html();
                        var splitHtml = html.split(" ");
                        var res = [];
                        for (var i=0;i<=splitHtml.length-2;++i) {
                            res.push(splitHtml[i]);
                        }
                        $(".navbar-nav > li > a[href=\"" + contestClarificationUrl + "\"]").html(res.join(" "));
                    }
                }
            }, "json");
        }, 30000);
    });
});