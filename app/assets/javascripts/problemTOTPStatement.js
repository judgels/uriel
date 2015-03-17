require(["jquery"], function( __tes__ ) {
    $.ajax({
        url: sandalphonTOTPURL,
        type: 'POST',
        data: body,
        contentType: 'text/plain',
        success: function (data) {
            $(".problem_statement").html(data);
        }
    });
});