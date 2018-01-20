(function () {
    "use strict";

    function TalkOverviewController($scope,$routeParams,talkManagerService,filterService) {
        (function init() {
            $scope.showFilters = true;
            $scope.filters = filterService.filters;;

            talkManagerService.eventMap.then(function(events) {
                $scope.events = events;
                $scope.talks = [];
                if ($routeParams.eventSlug) {
                    talkManagerService.talkList($routeParams.eventSlug).then(function(data) {
                        $scope.allTalks = data.data;
                        $scope.filterUpdated();
                    });
                } else {
                   $scope.allTalks = [];
                }
            });
        }());


        $scope.speakerList = function(speakers) {
            var speakersName = _.pluck(speakers, "name");
            var names =_.reduce(speakersName,function(a,b) {
                return a + ", " + b;
            });
            return names;
        }

        $scope.joinArrs = function(arr) {
            if (!arr || arr.length == 0) {
                return null;
            }
            var joined = _.reduce(arr,function(a,b) {
                return a + ", " + b;
            });
            return joined;
        }

        $scope.filterUpdated = function() {
            filterService.doFilter($scope.talks,$scope.allTalks);
            $scope.usedTags = filterService.usedTags;
            $scope.usedKeywords = filterService.usedKeywords;
        }
        $scope.addFilter = function() {
            $scope.filters.push({title: ""})
        };

        $scope.addOpAnd = function() {
            $scope.filters.push({filterOperator: filterService.filterOperators.OP_AND});
            $scope.filterUpdated();
        };
        $scope.addOpOr = function() {
            $scope.filters.push({filterOperator: filterService.filterOperators.OP_OR});
            $scope.filterUpdated();
        };
        $scope.addOpNot = function() {
            $scope.filters.push({filterOperator: filterService.filterOperators.OP_NOT});
            $scope.filterUpdated();
        };
        $scope.addOpEnd = function() {
            $scope.filters.push({filterOperator: filterService.filterOperators.OP_END});
            $scope.filterUpdated();
        };
        $scope.clearAllFilters = function() {
            $scope.filters.splice(0,$scope.filters.length);
            $scope.filterUpdated();
        };
        $scope.isFilterLine = function(filter) {
            return !filter.filterOperator;
        };
        $scope.operatorText = function(filter) {
            switch (filter.filterOperator) {
                case filterService.filterOperators.OP_AND:
                    return "AND (";
                case filterService.filterOperators.OP_OR:
                    return "OR (";
                case filterService.filterOperators.OP_NOT:
                    return "NOT (";
                case filterService.filterOperators.OP_END:
                    return ")";
                default:
                    return "XXX";
            }
        };

        $scope.setupSchedule = function () {
            var selectedTalkRefs = _.pluck($scope.talks,"ref");
            sessionStorage.setItem("selectedTalks",JSON.stringify(selectedTalkRefs));
            window.location = "setupSchedule.html";
        };


    }

    angular.module('cakeReduxModule')
        .controller('TalkOverviewController', TalkOverviewController);
}());
