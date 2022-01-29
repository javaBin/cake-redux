function getParameterByName(name) {
    var url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

var talkid = getParameterByName("id");

function updateVideo() {
    let vidval = $("#videoaddr").val();
    let $statustext = $("#statustext");
    if (vidval.endsWith("/")) {
        $statustext.empty();
        $statustext.append("Value can not end with /");
        return;
    }
    $("#updatebutton").prop("disabled","true");
    var payload = {
        id: talkid,
        video: vidval
    };
    $.ajax({
        method: "POST",
        data: JSON.stringify(payload),
        url: "api/updatevideo",
        success: function( fromServer ) {
            $statustext.empty();
            $statustext.append("Video was updated");
        },
        error: function( jqXHR, textStatus, errorThrown ){
            $statustext.empty();
            $statustext.append("Error: " + errorThrown);
            $("#updatebutton").prop("disabled","false");
        }
    });

}

$(function () {
    $.ajax({
        method: "GET",
        url: "api/one?id=" + talkid,
        success: function( fromServer ) {
            $("#header").append(fromServer.title);
            $("#videoaddr").val(fromServer.video);
        },
        error: function( jqXHR, textStatus, errorThrown ){
            window.alert(jqXHR);
        }
    });
    $("#updatebutton").click(updateVideo);
});
