angular.module('cakeReduxModule')
.controller('TalkListCtrl', ['$scope', '$http', '$routeParams', 'eventFactory',
    function($scope, $http, $routeParams,eventFactory) {
        $scope.allTalks = [];
        $scope.talks = [];
        eventFactory(function(data) {
            $scope.events = data;
            if ($routeParams.eventSlug) {
                $scope.chosenEvent = _.findWhere($scope.events,{slug: $routeParams.eventSlug});
                if ($scope.chosenEvent) {
                    $http({method: "GET", url: "data/talks?eventId=" + $scope.chosenEvent.ref})
                        .success(function(talklist) {
                            $scope.allTalks = talklist;
                            $scope.talks = _.clone(talklist);
                        });
                }
            }
        });
		
}]);
