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
;
