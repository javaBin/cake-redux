angular.module('cakeReduxModule')
    .controller('ShowScheduleCtrl', ['$scope','talkList','showScheduleService','filterService',
        function($scope,talkList,showScheduleService,filterService) {

            var doRefresh = function() {
                var sorted = showScheduleService(filterService.filteredTalks);

                $scope.rooms = sorted.rooms;
                $scope.slots = sorted.slots;
            };

            doRefresh();

            $scope.talkSelected = function(talk) {
                filterService.selectedTalk = talk;
            }

            $scope.refresh = function() {
                doRefresh();
            }

        }]);


