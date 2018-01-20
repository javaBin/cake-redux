var linetemplate = '<li><a id="onetalklink"></a></li>';

var addOneTalk = function (index,onetalk) {
    $line = $(linetemplate);
    $line.find("#onetalklink").append(onetalk.title);
    $line.find("#onetalklink").attr("href","editone.html?id=" + onetalk.id);
    $("#talklist").append($line);
};

$(function () {
    $.ajax({
        method: "GET",
        url: "api/all",
        success: function( fromServer ) {
            $.each(fromServer,addOneTalk);
            $("#waitimg").hide();
        },
        error: function( jqXHR, textStatus, errorThrown ){
            window.alert(jqXHR);
        }
    });
});
