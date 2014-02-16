angular.module('cakeReduxModule')
    .factory("eventFactory",["$http", function($http) {
        var res = function (callback) {

            $http({method: "GET", url: "data/events"})
                .success(function(data) {
                    callback(data);
                });

        };
         return res;
    }])
    .factory('talkList',[function() {
        var li = {
            allTalks : [],
            talks: []
        };
        return _.clone(li);
    }])
    .factory('filterService',[function() {
        var fis = {
            filters : [],
            doFilter : function(talks,allTalks) {
                var self = this;
                talks.splice(0,talks.length);
                _.each(allTalks,function(talk) {
                   var match=true;
                    _.each(self.filters,function(filter) {
                        if (!match) {
                            return;
                        }
                        if (talk.title.indexOf(filter.title) == -1) {
                            match = false;
                        }
                    });
                    if (match) {
                        talks.push(talk);
                    }
                });
            }
        };
        return fis;
    }])
;
