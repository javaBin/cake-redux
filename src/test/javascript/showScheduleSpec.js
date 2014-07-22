
describe("Show schedule spec suite", function() {
    var showScheduleService;

    beforeEach(function() {
        module('cakeReduxModule',function($provide) {
            $provide.value("cookieService", {
                setCookie : function(cname,cvalue,exdays) {

                },
                getCookie : function(cname) {
                    return "[]";
                }
            });
        });
    });

    beforeEach(inject(function($injector) {
        showScheduleService = $injector.get("showScheduleService");
    }));



    it('should group all in one group when not scheduled', function() {
        expect(showScheduleService).not.toBe(null);
        var talks = [
            {title: "One"},
            {title: "Two"}
        ];
        var result = showScheduleService(talks);
        var rooms = result.rooms;
        expect(rooms.length).toBe(1);
        expect(rooms[0]).toBe("No room");

        var slots = result.slots;
        expect(slots.length).toBe(1);
        var slot = slots[0];
        expect(slot.name).toBe("No slot");
        expect(slot.rooms.length).toBe(1);
        var slotRoom = slot.rooms[0];
        expect(slotRoom.name).toBe("No room")
        expect(slotRoom.talks.length).toBe(2);
        expect(slotRoom.talks[0].title).toBe("One");
    });


});
