
describe("Slot filter spec suite", function() {
    var slotFilterService;

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
        slotFilterService = $injector.get("slotFilterService");
    }));

    var allSlots = [
        {start: "140910 10:00",end:"140910 11:00",length:60,ref:"a"},
        {start: "140910 12:00",end:"140910 13:00",length:60,ref:"b"},
        {start: "140910 10:00",end:"140910 10:10",length:10,ref:"c"},
        {start: "140910 10:20",end:"140910 10:30",length:10,ref:"d"},
        {start: "140910 10:00",end:"140910 12:00",length:120,ref:"e"}
    ];

    it('should start with all selected', function() {
        expect(slotFilterService).not.toBe(null);
        expect(slotFilterService.filterValue.ten).toBe(true);
    });

    it('should return all by default', function() {
        expect(slotFilterService.doFilter(allSlots).length).toBe(5);
    });

    it('should return only 60 minutes', function() {
        slotFilterService.filterValue.sixty = true;
        slotFilterService.filterValue.ten = false;
        slotFilterService.filterValue.others = false;
        var result = slotFilterService.doFilter(allSlots);
        expect(result.length).toBe(2);
        expect(result[0].ref).toBe("a");
        expect(result[1].ref).toBe("b");
    });


    it('should return only 10 minutes', function() {
        slotFilterService.filterValue.sixty = false;
        slotFilterService.filterValue.ten = true;
        slotFilterService.filterValue.others = false;
        var result = slotFilterService.doFilter(allSlots);
        expect(result.length).toBe(2);
        expect(result[0].ref).toBe("c");
        expect(result[1].ref).toBe("d");
    });

    it('should return only others', function() {
        slotFilterService.filterValue.sixty = false;
        slotFilterService.filterValue.ten = false;
        slotFilterService.filterValue.others = true;
        var result = slotFilterService.doFilter(allSlots);
        expect(result.length).toBe(1);
        expect(result[0].ref).toBe("e");
    });

    it('should return combination', function() {
        slotFilterService.filterValue.sixty = false;
        slotFilterService.filterValue.ten = true;
        slotFilterService.filterValue.others = true;
        var result = slotFilterService.doFilter(allSlots);
        expect(result.length).toBe(3);
        expect(result[0].ref).toBe("c");
        expect(result[1].ref).toBe("d");
        expect(result[2].ref).toBe("e");
    });
});
