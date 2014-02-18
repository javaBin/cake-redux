
describe("Filter test suite", function() {
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
       var allTalks = [{title: "Title one", speakers: []},{title: "Title two", speakers:[]}];
       var talks = [];
       filterService.doFilter(talks,allTalks);
       expect(talks.length).toBe(1);
       expect(talks[0].title).toBe("Title one");
    });

    it('should not filter when filter is empty',function() {
        filterService.filters.push({title: null});
        var allTalks = [{title: "Title one",speakers:[]},{title: "Title two",speakers:[]}];
        var talks = [];
        filterService.doFilter(talks,allTalks);
        expect(talks.length).toBe(2);
    });
    it('should filter speaker',function() {
        filterService.filters.push({speaker: "one"});
        var allTalks = [
            {title: "Title en",speakers: [{name: "Speaker One"}]},
            {title: "Title to",speakers: [{name: "Speaker Two"}]},
            {title: "Title tre",speakers: [{name: "Speaker One"},{name: "Speaker two"}]}
            ];
        var talks = [];
        filterService.doFilter(talks,allTalks);
        expect(talks.length).toBe(2);
        expect(talks[0].title).toBe("Title en");
        expect(talks[1].title).toBe("Title tre");
    })
});
