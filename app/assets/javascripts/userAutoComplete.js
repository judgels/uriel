require(["jquery", "jquery-ui"], function( __tes__ ) {
    $(".user_autocomplete").autocomplete({
        source: function( request, response ) {
            $.ajax({
                url: jophielAutoCompleteUrl + "?term=" + request.term,
                type: 'GET',
                dataType: "json",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", "Bearer " + encodedAccessToken);
                },
                success: function( data ) {
                    response( data );
                }
            });
        },
        minLength: 2
    });
});