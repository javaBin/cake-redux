(function () {
    "use strict";

    function talkManagerService($http) {
        function builldMap(event) {
            var res = _.clone(event);
            res.talkList = $http({method: "GET", url: "data/talks?eventId=" + res.ref});
            return res;
        }
        this.eventMap = $http({method: "GET", url: "data/events"})
            .then(function(data) {
                return _.map(data,builldMap)
            });

        var self = this;

        this.talkList = function (eventSlug) {
            return self.eventMap.then(function(eventList) {
                return _.findWhere(eventList,{slug: eventSlug}).talkList;
            })
        }
    }

    angular.module('cakeReduxModule')
        .service('talkManagerService', talkManagerService);
}());
