(function () {
    "use strict";

    function talkManagerService($http) {
        function builldMap(event) {
            var res = _.clone(event);
            res.talkList = function() {
                var eventurl = "data/talks?eventId=" + res.ref;
                return $http({method: "GET", url: eventurl});
            };
            return res;
        }
        this.eventMap = $http({method: "GET", url: "data/events"})
            .then(function(data) {
                return _.map(data.data,builldMap)
            });

        var self = this;

        this.talkList = function (eventSlug) {
            return self.eventMap.then(function(eventList) {
                return _.findWhere(eventList,{slug: eventSlug}).talkList();
            })
        }
    }

    angular.module('cakeReduxModule')
        .service('talkManagerService', talkManagerService);
}());
