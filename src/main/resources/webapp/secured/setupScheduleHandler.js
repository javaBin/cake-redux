var selectedTalks = JSON.parse(sessionStorage.getItem("selectedTalks"));

var cellTemplate = '<td><ul id="talklist"></ul></td>';

function clickMe(message) {
    console.log(message);
};

function dropTarget(room,slot) {
    return function (event,ui) {
        var talkid = $(ui.draggable).attr("id");
        var payload = {
            id: talkid,
            room: room,
            slot: slot
        };
        $.ajax({
            url: "data/sendForRoomSlotUpdate",
            method: "POST",
            data:JSON.stringify(payload),
            success: function( fromServer ) {
                window.location.reload();
            },
            error: function( jqXHR, textStatus, errorThrown ){
                window.alert(errorThrown);
            }
        });
        //console.log("Dropped " + talkid + " in " + room + " "  +slot);
    };
}

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
                $cellcontent = $("<li></li>");
                $cellcontent.append(onetalk.display);
                $cellcontent.attr("id",onetalk.id);
                $cellcontent.draggable();
                //$cellcontent.attr("onClick","clickMe('" + onetalk.id + " ')");
                $onecell.find("#talklist").append($cellcontent);
            });


            if (onecell.room !== "No room" && onecell.slot !== "No slot") {
                $droptarget = $('<p class="slotupd">Add to slot...</p>');
                $droptarget.droppable({
                    drop: dropTarget(onecell.room, onecell.slot),
                    activeClass: "ui-state-hover"
                    //classes: {'ui-droppable-hover': 'highlight'}
                });
                $onecell.append($droptarget);
            }

            $trow.append($onecell);
        });

        $("#gridTable").append($trow);
    });
};

$(function () {

    $.ajax({
        url: "data/readSlotForUpdate",
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
