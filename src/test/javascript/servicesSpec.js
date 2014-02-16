
describe("A suite", function() {
    var filterService;
    beforeEach(function() {
        module('cakeReduxModule');
    });


    beforeEach(inject(function($injector) {
        filterService = $injector.get("filterService");
    }));

    it('should start empty', function() {
        expect(filterService).not.toBe(null);
    });
});
