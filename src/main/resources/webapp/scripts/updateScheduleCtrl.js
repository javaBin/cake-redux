angular.module('cakeReduxModule')
    .controller('UpdateScheduleCtrl', ['$scope', '$http','filterService',
        function($scope, $http, filterService) {
                $scope.talks = filterService.filteredTalks;

                var allRefs = _.pluck($scope.talks,"ref");
                var postData = {
                        talkReferences: allRefs,
                        wantedRooms: [],
                        wantedSlots: []
                };
                $scope.grid = {
                        rooms: [],
                        slots: []
                };
                $http({
                        method: "POST",
                        url: "data/computeSchedule",
                        data: postData
                }).success(function(data) {
                        $scope.grid = data;
                });

        }]);


