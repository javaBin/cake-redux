angular.module('cakeReduxModule')
    .controller('AcceptTalkCtrl', ['$scope', '$http', 'talkList','$location',
        function($scope, $http, talkList,$location) {

            $scope.accept = ($location.path() === "/accept");
            $scope.title = ($scope.accept) ? "Accept talks" : "Mass update";


            $scope.talks = talkList.talks;
            $scope.numTalks = $scope.talks.length;
            $scope.statusLines = [];

            $scope.acceptTalks = function() {
                var talkList = [];
                _.each($scope.talks,function(talk) {
                    talkList.push({ref: talk.ref});
                });
                $http({
                    method: "POST",
                    url: "data/acceptTalks",
                    data: {talks: talkList}
                }).success(function(data) {
                    $scope.statusLines = data;
                }).error(function(data, status, headers, config) {
                    console.log("ERROR");
                });
            };

            $scope.lineClass = function(status) {
                if (status === "error") {
                    return "danger";
                }
                return "";
            }
        }]);


