requirejs.config({
    shim: {
        'bootstrap-datetimepicker': {
            deps: ['bootstrap', 'moment']
        }
    },
    paths: {
        'moment': '/assets/lib/momentjs/min/moment.min',
        'bootstrap-datetimepicker': '/assets/lib/Eonasdan-bootstrap-datetimepicker/bootstrap-datetimepicker.min'
    }
});

require(["jquery", "bootstrap-datetimepicker"], function( __tes__ ) {
    $(".form_datetime").datetimepicker({format: 'DD-MM-YYYY, HH:mm'});
});