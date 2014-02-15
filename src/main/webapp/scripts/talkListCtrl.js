angular.module('cakeReduxModule')
.controller('TalkListCtrl', ['$scope', '$http', '$routeParams', 'eventFactory','talkList',
    function($scope, $http, $routeParams,eventFactory,talkList) {
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
                                $scope.talks = talklist;
                                talkList.allTalks = $scope.allTalks;
                                talkList.talks = $scope.talks;
                            });
                    }
                }
            });
        }

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
		
}]);
