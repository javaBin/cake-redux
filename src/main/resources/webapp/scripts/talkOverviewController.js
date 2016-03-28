(function () {
    "use strict";

    function TalkOverviewController($scope,talkManagerService) {
        (function init() {
            talkManagerService.eventMap.then(function(events) {
                $scope.events = events;
            });
        }());
    }

    angular.module('cakeReduxModule')
        .controller('TalkOverviewController', TalkOverviewController);
}());
