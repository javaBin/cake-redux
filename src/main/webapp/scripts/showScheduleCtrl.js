angular.module('cakeReduxModule')
    .controller('ShowScheduleCtrl', ['$scope','talkList','showScheduleService','filterService',
        function($scope,talkList,showScheduleService,filterService) {
            var sorted = showScheduleService(talkList.talks);

            $scope.rooms = sorted.rooms;
            $scope.slots = sorted.slots;

            $scope.talkSelected = function(talk) {
                filterService.selectedTalk = talk;
            }
        }]);


