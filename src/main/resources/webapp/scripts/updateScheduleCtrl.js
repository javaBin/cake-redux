angular.module('cakeReduxModule')
    .controller('UpdateScheduleCtrl', ['$scope', '$http','filterService','$window',
        function($scope, $http, filterService,$window) {
                $scope.emsExportDisabled = false;
                $scope.exportMessage = "";
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

                $scope.moveSchedule = function (ref, direction) {
                        var moveData = {
                                talkReferences: allRefs,
                                wantedRooms: [],
                                wantedSlots: [],
                                talkRef: ref,
                                moveDirection: direction
                        };
                        $http({
                                method: "POST",
                                url: "data/scheduleUpdate",
                                data: moveData
                        }).success(function(data) {
                                $scope.grid = data;
                        });
                };
                
                $scope.exportToEms = function () {
                        $scope.exportMessage = "";
                        $scope.emsExportDisabled = true;
                        $http({
                                method: "POST",
                                url: "data/exportScheduleEms",
                                data: allRefs
                        }).success(function (data) {
                                $scope.emsExportDisabled = false;
                                $scope.exportMessage = data;
                        }).failure(function () {
                                $scope.emsExportDisabled = false;
                                
                        })
                        
                }
                
                $scope.openTalk = function (ref) {
                        $window.open("#/showTalk/" + ref);
                };
                
                $scope.setForEdit = function (ref) {
                        $scope.toEdit = ref;
                };

                $scope.editOneTalk = function () {
                        var data = {
                                talkref: $scope.toEdit.ref,
                                editedSlot: $scope.editedSlot,
                                editedRoom: $scope.editedRoom,
                                talkReferences: allRefs
                        };
                        console.log(data);
                }


        }]);


