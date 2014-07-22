angular.module('cakeReduxModule')
    .controller('ShowScheduleCtrl', ['$scope','talkList','showScheduleService',
        function($scope,talkList,showScheduleService) {
            var sorted = showScheduleService(talkList.talks);

            $scope.rooms = sorted.rooms;
            $scope.slots = sorted.slots;
        }]);


