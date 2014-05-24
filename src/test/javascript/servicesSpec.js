
describe("Filter test suite", function() {
    var filterService;
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
    });
    it('should handle tag arrays',function() {
        var allTalks=[{title: "one", tags: ["ja","bekreftet"]},{title: "two",tags: []}];
        var talks = [];
        filterService.filters.push({tag: "ja"});
        filterService.doFilter(talks,allTalks);
        expect(talks.length).toBe(1);
        expect(talks[0].title).toBe("one");
    });
    it('should only match on whole tags',function() {
        var allTalks=[{title: "one", tags: ["ja_xx","bekreftet"]},{title: "two",tags: []}];
        var talks = [];
        filterService.filters.push({tag: "ja"});
        filterService.doFilter(talks,allTalks);
        expect(talks.length).toBe(0);
    });
    it('should not match tag if talk has no tags',function() {
        var allTalks=[
            {title: "one", tags: [],format: "presentation"}];
        var talks = [];

        filterService.filters.push({tag: "test"});

        filterService.doFilter(talks,allTalks);

        expect(talks.length).toBe(0);
    });
    it('should define two filter entries as or',function() {
        var allTalks=[{title: "one", tags: ["ja","bekreftet"]},{title: "two",tags: []},{title:"three"}];
        var talks = [];

        filterService.filters.push({title: "one"});
        filterService.filters.push({title: "two"});

        filterService.doFilter(talks,allTalks);

        expect(talks.length).toBe(2);
    });
    it('should handle operator and',function() {
        var allTalks=[{title: "one", tags: ["ja","bekreftet"]},{title: "two",tags: []},{title:"three"}];
        var talks = [];

        filterService.filters.push({filterOperator: filterService.filterOperators.OP_AND});
        filterService.filters.push({title: "one"});

        filterService.doFilter(talks,allTalks);

        expect(talks.length).toBe(1);
    })
    it('should handle operator not',function() {
        var allTalks=[{title: "one", tags: ["ja","bekreftet"]},{title: "two",tags: []},{title:"three"}];
        var talks = [];

        filterService.filters.push({filterOperator: filterService.filterOperators.OP_NOT});
        filterService.filters.push({title: "one"});

        filterService.doFilter(talks,allTalks);

        expect(talks.length).toBe(2);
        expect(talks[0].title).toBe("two");
    });
    it('should handle multiple filter setup',function() {
        var allTalks=[
            {title: "one", tags: [],format: "presentation"},
            {title: "two",tags: ["test"], format: "presentation"},
            {title:"three", tags: [],format: "workshop"}];
        var talks = [];

        filterService.filters.push({filterOperator: filterService.filterOperators.OP_AND});
        filterService.filters.push({filterOperator: filterService.filterOperators.OP_NOT});
        filterService.filters.push({tag: "test"});
        filterService.filters.push({filterOperator: filterService.filterOperators.OP_END});
        filterService.filters.push({format: "presentation"});

        filterService.doFilter(talks,allTalks);

        expect(talks.length).toBe(1);
        expect(talks[0].title).toBe("one");


    });
});
