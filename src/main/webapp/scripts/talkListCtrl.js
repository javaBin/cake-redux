angular.module('cakeReduxModule')
.controller('TalkListCtrl', ['$scope', '$http', '$routeParams', 'eventFactory','talkList','filterService',
    function($scope, $http, $routeParams,eventFactory,talkList,filterService) {
        document.title = "Cake redux - it's the truth";
        $scope.selectedTalk = filterService.selectedTalk;
        $scope.showFilters = true;
        $scope.doShowFilters = function() {
            $scope.showFilters =true ;
        };
        $scope.doHideFilters = function() {
            $scope.showFilters = false;
        }
        $scope.filters = filterService.filters;
        $scope.allTalks = talkList.allTalks;
        $scope.talks = talkList.talks;
        if ($scope.allTalks.length == 0) {
            eventFactory(function(data) {
                $scope.events = data;
                if ($routeParams.eventSlug) {
                    $scope.chosenEvent = _.findWhere($scope.events,{slug: $routeParams.eventSlug});
                    if ($scope.chosenEvent) {
                        $http({method: "GET", url: "data/talks?eventId=" + $scope.chosenEvent.ref})
                            .success(function(talklist) {
                                $scope.allTalks = talklist;
                                $scope.talks = _.clone(talklist);
                                talkList.allTalks = $scope.allTalks;
                                talkList.talks = $scope.talks;
                            });
                    }
                }
            });
        }

        $scope.reloadTalks = function() {
            if ($scope.chosenEvent) {
                $http({method: "GET", url: "data/talks?eventId=" + $scope.chosenEvent.ref})
                    .success(function(talklist) {
                        $scope.allTalks = talklist;
                        $scope.talks = _.clone(talklist);
                        talkList.allTalks = $scope.allTalks;
                        talkList.talks = $scope.talks;
                        filterService.doFilter($scope.talks,$scope.allTalks);
                    });
            }
        };

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
        }

        $scope.talkSelected = function(talk) {
            filterService.selectedTalk = talk;
        }

        $scope.talkRowClass = function(talk) {
            if ($scope.selectedTalk && $scope.selectedTalk.title == talk.title) {
                return "success";
            }
            return "";
        }

		
}]);
