angular.module('cakeReduxModule')
    .controller('ShowScheduleCtrl', ['$scope','talkList',
        function($scope,talkList) {
            $scope.talks = talkList.talks;
        }]);


