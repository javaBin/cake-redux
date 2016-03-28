(function () {
    "use strict";

    function TalkOverviewController($scope,$routeParams,talkManagerService) {
        (function init() {
            talkManagerService.eventMap.then(function(events) {
                $scope.events = events;
                if ($routeParams.eventSlug) {
                    
                }
            });
        }());
    }

    angular.module('cakeReduxModule')
        .controller('TalkOverviewController', TalkOverviewController);
}());
