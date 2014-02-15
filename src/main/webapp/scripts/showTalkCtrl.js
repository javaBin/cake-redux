angular.module('cakeReduxModule')
    .controller('ShowTalkCtrl', ['$scope', '$http', '$routeParams', 'talkList',
        function($scope, $http, $routeParams,talkList) {
            var talkRef = $routeParams.talkId;
            $scope.aTalk = _.findWhere(talkList.allTalks,{ref: talkRef});

            $scope.newTagTyped = function() {
                var n = $scope.newTag;
                $scope.aTalk.tags.push(n);
            }



        }]);

