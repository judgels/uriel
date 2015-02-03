require(["jquery"], function( __tes__ ) {
    $.get(sandalphonTOTPURL, function( data ) {
        $(".problem_statement").html(data);
    });
});