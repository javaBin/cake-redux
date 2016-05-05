angular.module('cakeReduxModule')
    .controller('PublishTalkCtrl', ['$scope', '$http', '$location','filterService',
        function($scope, $http, $location,filterService) {


            $scope.talks = filterService.filteredTalks;
            _.each($scope.talks,function(talk) {
                talk.wasSelected = true;
            });
            $scope.numTalks = $scope.talks.length;
            $scope.statusLines = [];

            $scope.publishTalks = function() {
                var talkList = [];
                _.each($scope.talks,function(talk) {
                    if (talk.wasSelected) {
                        talkList.push({ref: talk.ref, title:talk.title});
                    }
                });
                if (talkList.length == 0) {
                    return;
                }

                $scope.statusLines = [];


                talkList.forEach(function(talk) {
                    var ind = $scope.statusLines.length;
                    $scope.statusLines.push({title: talk.title,message:"Prosessing"})
                    $http({
                        method: "POST",
                        url: "data/massPublish",
                        data: talk
                    }).success(function(data) {
                        $scope.statusLines[ind].message = "ok";
                        console.log(data);
                    }).error(function(data, status, headers, config) {
                        console.log("ERROR");
                    });
                });

            };

            $scope.lineClass = function(status) {
                if (status === "error") {
                    return "danger";
                }
                return "";
            }
        }]);


