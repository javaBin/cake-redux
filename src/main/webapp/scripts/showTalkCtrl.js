angular.module('cakeReduxModule')
    .controller('ShowTalkCtrl', ['$scope', '$http', '$routeParams', 'talkList',
        function($scope, $http, $routeParams,talkList) {
            var talkRef = $routeParams.talkId;
            $scope.aTalk = _.findWhere(talkList.allTalks,{ref: talkRef});

            $scope.newTagTyped = function() {
                var n = $scope.newTag;
                $scope.aTalk.tags.push(n);
            }

            $scope.tagSelected = function() {
                var n = $scope.selectedTag;
                $scope.aTalk.tags.push(n);

            }

            $scope.removeTag = function(tag) {
                var index = $scope.aTalk.tags.indexOf(tag);
                if (index > -1) {
                    $scope.aTalk.tags.splice(index,1);
                }
            }

        }]);

