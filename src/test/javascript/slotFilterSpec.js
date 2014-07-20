
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

    it('should start with all selected', function() {
        expect(slotFilterService).not.toBe(null);
        expect(slotFilterService.filterValue.ten).toBe(true);
    });


});
