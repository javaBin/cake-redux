angular.module('cakeReduxModule')
.controller('TalkListCtrl', ['$scope', '$http', '$routeParams', 'eventFactory',
    function($scope, $http, $routeParams,eventFactory) {
        eventFactory(function(data) {
            $scope.events = data;
        });
		
}]);
