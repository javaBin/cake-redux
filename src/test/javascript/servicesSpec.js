
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
        expect(filterService.filters.length).toBe(0);
    });

    it('should filter on part of title',function() {
       var filter = {title : "one"};
       filterService.filters.push(filter);
       var allTalks = [{title: "Title one"},{title: "Title two"}];
       var talks = [];
       filterService.doFilter(talks,allTalks);
       expect(talks.length).toBe(1);
       expect(talks[0].title).toBe("Title one");
    });

    it('should not filter when filter is empty',function() {
        filterService.filters.push({title: null});
        var allTalks = [{title: "Title one"},{title: "Title two"}];
        var talks = [];
        filterService.doFilter(talks,allTalks);
        expect(talks.length).toBe(2);
    });
});
