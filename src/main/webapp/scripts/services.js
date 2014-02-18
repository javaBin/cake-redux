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
        var isMatch= function(filter,obj) {
            if (filter && filter.length > 0 && obj.toLowerCase().indexOf(filter.toLowerCase()) == -1) {
                return false;
            }
            return true;
        }

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
                        if (!isMatch(filter.title,talk.title)) {
                            match = false;
                            return;
                        }
                        if (filter.speaker && filter.speaker.length > 0) {
                            var foundSpeaker = false;
                            _.each(talk.speakers,function(speaker) {
                                if (foundSpeaker) {
                                    return;
                                }

                                if (isMatch(filter.speaker,speaker.name)) {
                                    foundSpeaker = true;
                                    return;
                                }
                            });
                            match = foundSpeaker;
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
