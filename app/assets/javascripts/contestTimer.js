require(["jquery", "jquery-timer"], function( __tes__ ) {
    var localTime = new Date();
    var time_index = 0;
    var seconds = secondsLeft % 60;
    if (secondsLeft > 0) {
        if (contestStartTimeLeft > 0) {
            $("#contest-time-label").html(contestNotStartedMessage);
        } else {
            $("#contest-time-label").html(timeLeftMessage);
            if (seconds > 0) {
                $("#contest-second-left").html(seconds + " " + $("#contest-second-left").attr("span-label") + ((seconds > 1) ? second : ""));
                time_index++;
            } else {
                $("#contest-second-left").html("");
            }
            var minutes = Math.floor(secondsLeft % 3600 / 60);
            if (minutes > 0) {
                $("#contest-minute-left").html(minutes + " " + $("#contest-minute-left").attr("span-label") + ((minutes > 1) ? second : "") + ((time_index > 0) ? "," : ""));
                time_index++;
            } else {
                $("#contest-minute-left").html("");
            }
            var hours = Math.floor(secondsLeft % 86400 / 3600);
            if (hours > 0) {
                $("#contest-hour-left").html(hours + " " + $("#contest-hour-left").attr("span-label") + ((hours > 1) ? second : "") + ((time_index > 0) ? "," : ""));
                time_index++;
            } else {
                $("#contest-hour-left").html("");
            }
            var days = Math.floor(secondsLeft / 86400);
            if (days > 0) {
                $("#contest-day-left").html(days + " " + $("#contest-day-left").attr("span-label") + ((days > 1) ? second : "") + ((time_index > 0) ? "," : ""));
                time_index++;
            } else {
                $("#contest-day-left").html("");
            }
        }
        $(document).stopTime("contest");
        $(document).everyTime('1s', "contest", function (i) {
            if (secondsLeft > 0) {
                var currentTime = new Date();
                secondsLeft = secondsLeft - ((currentTime.getTime() - localTime.getTime()) / 1000);
                contestStartTimeLeft = contestStartTimeLeft - ((currentTime.getTime() - localTime.getTime()) / 1000);
                localTime = currentTime;
                if (contestStartTimeLeft > 0) {
                    $("#contest-time-label").html(contestNotStartedMessage);
                } else {
                    $("#contest-time-label").html(timeLeftMessage);
                    var time_index = 0;
                    var seconds = Math.floor(secondsLeft % 60);
                    if (seconds > 0) {
                        $("#contest-second-left").html(seconds + " " + $("#contest-second-left").attr("span-label") + ((seconds > 1) ? second : ""));
                        time_index++;
                    } else {
                        $("#contest-second-left").html("");
                    }
                    var minutes = Math.floor(secondsLeft % 3600 / 60);
                    if (minutes > 0) {
                        $("#contest-minute-left").html(minutes + " " + $("#contest-minute-left").attr("span-label") + ((minutes > 1) ? second : "") + ((time_index > 0) ? "," : ""));
                        time_index++;
                    } else {
                        $("#contest-minute-left").html("");
                    }
                    var hours = Math.floor(secondsLeft % 86400 / 3600);
                    if (hours > 0) {
                        $("#contest-hour-left").html(hours + " " + $("#contest-hour-left").attr("span-label") + ((hours > 1) ? second : "") + ((time_index > 0) ? "," : ""));
                        time_index++;
                    } else {
                        $("#contest-hour-left").html("");
                    }
                    var days = Math.floor(secondsLeft / 86400);
                    if (days > 0) {
                        $("#contest-day-left").html(days + " " + $("#contest-day-left").attr("span-label") + ((days > 1) ? second : "") + ((time_index > 0) ? "," : ""));
                        time_index++;
                    } else {
                        $("#contest-day-left").html("");
                    }
                }
            } else {
                secondsLeft = 0;
                $("#contest-time-label").html(contestEndMessage);
            }
        });
    } else {
        $(document).stopTime("contest");
        $("#contest-time-label").html(contestEndMessage);
    }
});