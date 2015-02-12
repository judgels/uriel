require(["jquery", "jquery-timer"], function( __tes__ ) {
    var time_index = 0;
    var seconds = secondsLeft % 60;
    if (seconds > 0) {
        $("#contest-second-left").html(seconds+" "+$("#contest-second-left").attr("span-label")+((seconds > 1) ? second : ""));
        time_index++;
    } else {
        $("#contest-second-left").html("");
    }
    var minutes = Math.floor(secondsLeft % 3600 / 60);
    if (minutes > 0) {
        $("#contest-minute-left").html(minutes+" "+$("#contest-minute-left").attr("span-label")+((minutes > 1) ? second : "")+((time_index > 0) ? "," : ""));
        time_index++;
    } else {
        $("#contest-minute-left").html("");
    }
    var hours = Math.floor(secondsLeft % 86400 / 3600);
    if (hours > 0) {
        $("#contest-hour-left").html(hours+" "+$("#contest-hour-left").attr("span-label")+((hours > 1) ? second : "")+((time_index > 0) ? "," : ""));
        time_index++;
    } else {
        $("#contest-hour-left").html("");
    }
    var days = Math.floor(secondsLeft / 86400);
    if (days > 0) {
        $("#contest-day-left").html(days+" "+$("#contest-day-left").attr("span-label")+((days > 1) ? second : "")+((time_index > 0) ? "," : ""));
        time_index++;
    } else {
        $("#contest-day-left").html("");
    }
    $(document).stopTime("contest");
    $(document).everyTime('1s', "contest", function(i) {
        if (secondsLeft > 0) {
            secondsLeft -- ;
        } else {
            secondsLeft = 0 ;
        }
        var time_index = 0;
        var seconds = secondsLeft % 60;
        if (seconds > 0) {
            $("#contest-second-left").html(seconds+" "+$("#contest-second-left").attr("span-label")+((seconds > 1) ? second : ""));
            time_index++;
        } else {
            $("#contest-second-left").html("");
        }
        var minutes = Math.floor(secondsLeft % 3600 / 60);
        if (minutes > 0) {
            $("#contest-minute-left").html(minutes+" "+$("#contest-minute-left").attr("span-label")+((minutes > 1) ? second : "")+((time_index > 0) ? "," : ""));
            time_index++;
        } else {
            $("#contest-minute-left").html("");
        }
        var hours = Math.floor(secondsLeft % 86400 / 3600);
        if (hours > 0) {
            $("#contest-hour-left").html(hours+" "+$("#contest-hour-left").attr("span-label")+((hours > 1) ? second : "")+((time_index > 0) ? "," : ""));
            time_index++;
        } else {
            $("#contest-hour-left").html("");
        }
        var days = Math.floor(secondsLeft / 86400);
        if (days > 0) {
            $("#contest-day-left").html(days+" "+$("#contest-day-left").attr("span-label")+((days > 1) ? second : "")+((time_index > 0) ? "," : ""));
            time_index++;
        } else {
            $("#contest-day-left").html("");
        }
    });
});