(function () {
    "use strict";

    function TalkOverviewController($scope,$routeParams,talkManagerService) {
        (function init() {
            talkManagerService.eventMap.then(function(events) {
                $scope.events = events;
                if ($routeParams.eventSlug) {
                    talkManagerService.talkList($routeParams.eventSlug).then(function(data) {
                       $scope.talks = data.data;
                    });
                } else {
                    $scope.talks = [];
                }
            });
        }());
    }

    angular.module('cakeReduxModule')
        .controller('TalkOverviewController', TalkOverviewController);
}());
