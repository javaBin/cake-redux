var selectedTalks = JSON.parse(sessionStorage.getItem("selectedTalks"));

var cellTemplate = '<td><ul id="talklist"></ul></td>';

function buildTable(tabledata) {
    $.each(tabledata.rooms,function (index,room) {
        $("#headerRow").append("<th>" + room + "</th>");
    });
    $.each(tabledata.talkGrid,function (index,tablerow) {
        $trow = $("<tr></tr>");
        $trow.append("<td>" + tablerow.slot + "</td>");

        $.each(tablerow.row,function (index, onecell) {
            $onecell = $(cellTemplate);

            $.each(onecell.talks,function (index,onetalk) {
                $onecell.find("#talklist").append("<li>" + onetalk.display + "</li>");
            });

            $trow.append($onecell);
        });

        $("#gridTable").append($trow);
    });
};

$(function () {

    $.ajax({
        url: "/secured/data/readSlotForUpdate",
        method: "POST",
        data:JSON.stringify({talks:selectedTalks}),
        success: function( fromServer ) {
            buildTable(fromServer);
        },
        error: function( jqXHR, textStatus, errorThrown ){
            window.alert(errorThrown);
        }
    });
});
