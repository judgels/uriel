requirejs.config({
    shim: {
        'ckeditor-jquery': {
            deps: ['jquery', 'ckeditor-core']
        }
    },
    paths: {
        'ckeditor-core': '/assets/lib/ckeditor/ckeditor',
        'ckeditor-jquery': '/assets/lib/ckeditor/adapters/jquery'
    }
});

require(["jquery", "ckeditor-jquery"], function( __jquery__ ) {
    CKEDITOR.config.toolbar = [
        ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'RemoveFormat'], ['NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote'], ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'], ['Image', 'Link', 'Table'], ['Styles', 'Format'], ['Source', '-', 'Preview']
    ];
    $('.ckeditor').ckeditor();
});