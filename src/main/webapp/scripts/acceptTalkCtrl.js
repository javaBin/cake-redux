angular.module('cakeReduxModule')
    .controller('AcceptTalkCtrl', ['$scope', '$http', 'talkList',
        function($scope, $http, talkList) {
            $scope.talks = talkList.talks;
            $scope.numTalks = $scope.talks.length;
        }]);


