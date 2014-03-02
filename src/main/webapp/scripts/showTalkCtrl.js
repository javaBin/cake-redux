angular.module('cakeReduxModule')
    .controller('ShowTalkCtrl', ['$scope', '$http', '$routeParams', 'talkList',
        function($scope, $http, $routeParams,talkList) {
            var talkRef = $routeParams.talkId;
            $scope.showError = false;


            $scope.aTalk = _.findWhere(talkList.allTalks,{ref: talkRef});
            if (!$scope.aTalk || !$scope.aTalk.lastModified) {
                $http({method: "GET", url: "data/atalk?talkId=" + talkRef})
                    .success(function(data) {
                        if ($scope.aTalk) {
                            for (var prop in data) {
                                $scope.aTalk[prop] = data[prop];
                            }
                        } else {
                            $scope.aTalk = data;
                        }
                    });
            }

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

            $scope.saveTalk = function() {
                $scope.showError = false;
                var savebtn = $("#saveButton");
                savebtn.button("loading");
                var t = $scope.aTalk;
                var postData = {
                  ref: t.ref,
                  lastModified: t.lastModified,
                  tags : t.tags
                };
                $http({
                    method: "POST",
                    url: "data/editTalk",
                    data: postData
                }).success(function(data) {
                    savebtn.button("reset");
                    if (data.error) {
                        $scope.errormessage = data.error;
                        $scope.showError = true;
                        return;
                    }
                    $scope.aTalk.lastModified = data.lastModified;
                }).error(function(data, status, headers, config) {
                    savebtn.button("reset");
                });
            };

            $scope.joinArr = function(arr) {
                if (!arr || arr.length == 0) {
                    return null;
                }
                var res = _.reduce(arr,function(a,b) {
                    return a + ", " + b;
                });
                return res;
            };




        }]);

