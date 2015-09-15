require(["jquery"], function( __tes__ ) {
    $(document).ready(function() {
        $.post(unansweredClarificationUrl, function(data) {
            if (data) {
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
            $.post(unansweredClarificationUrl, function(data) {
                if (data) {
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