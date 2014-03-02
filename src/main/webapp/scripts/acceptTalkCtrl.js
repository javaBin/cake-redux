angular.module('cakeReduxModule')
    .controller('AcceptTalkCtrl', ['$scope', '$http', 'talkList',
        function($scope, $http, talkList) {
            $scope.talks = talkList.talks;
            $scope.numTalks = $scope.talks.length;

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
                    console.log("success");
                    console.log(data);
                }).error(function(data, status, headers, config) {
                    console.log("ERROR");
                });
            };
        }]);


