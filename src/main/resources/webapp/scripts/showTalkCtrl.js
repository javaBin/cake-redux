angular.module('cakeReduxModule')
    .controller('ShowTalkCtrl', ['$scope', '$http', '$routeParams', 'talkList','roomSlotFactory','slotFilterService','talkManagerService',
        function($scope, $http, $routeParams,talkList,roomSlotFactory,slotFilterService,talkManagerService) {
            $scope.filterSlot = slotFilterService.filterValue;
            $scope.roomsSlots = {};


            var talkRef = $routeParams.talkId;
            $scope.showError = false;
            $scope.comments = [];
            $scope.ratings = [];

            var updateFromServer = function(data) {
                if ($scope.aTalk) {
                    for (var prop in data) {
                        $scope.aTalk[prop] = data[prop];
                    }
                } else {
                    $scope.aTalk = data;
                    $scope.comments = $scope.aTalk.comments;
                    $scope.ratings = $scope.aTalk.ratings;
                }
                var talkSpeakers = $scope.aTalk.speakers;

                //talkManagerService.talkList($scope.aTalk.eventSlug);
                if ($scope.aTalk) {
                    document.title = $scope.aTalk.title;
                    roomSlotFactory.roomsSlotsForEvent($scope.aTalk.eventId).then(function(rs) {
                        $scope.roomsSlots = rs;
                    })

                }

            };

            $scope.toCommaSeperated = function(arr) {
                return _.reduce(arr,function(a,b) {
                   return a + ", " + b;
                });
            }

            $scope.slotFilterUpdated = function() {
                $scope.roomsSlots.slots = slotFilterService.doFilter($scope.roomsSlots.allSlots);
            };


            $scope.aTalk = _.findWhere(talkList.allTalks,{ref: talkRef});
            if ($scope.aTalk) {
                document.title = $scope.aTalk.title;
            }
            if (!$scope.aTalk || !$scope.aTalk.lastModified) {
                $http({method: "GET", url: "data/atalk?talkId=" + talkRef})
                    .success(updateFromServer);
            }

            $scope.reloadTalk = function() {
                $http({method: "GET", url: "data/atalk?talkId=" + talkRef})
                    .success(updateFromServer);
            };

            $scope.newTagTyped = function() {
                var n = $scope.newTag;
                $scope.aTalk.tags.push(n);
            };

            $scope.newKeywordTyped = function() {
                var n = $scope.newKeyword;
                $scope.aTalk.keywords.push(n);
            };



            $scope.removeTag = function(tag) {
                var index = $scope.aTalk.tags.indexOf(tag);
                if (index > -1) {
                    $scope.aTalk.tags.splice(index,1);
                }
            };

            $scope.removeKeyword = function(keyword) {
                var index = $scope.aTalk.keywords.indexOf(keyword);
                if (index > -1) {
                    $scope.aTalk.keywords.splice(index,1);
                }
            };



            $scope.saveTalk = function() {
                $scope.showError = false;
                var savebtn = $("#saveButton");
                savebtn.button("loading");
                var t = $scope.aTalk;
                var postData = {
                    ref: t.ref,
                    lastModified: t.lastModified,
                    tags : t.tags,
                    state: t.state,
                    keywords: t.keywords
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


            $scope.addComment = function () {
                $http({
                    method: "POST",
                    url: "data/addComment",
                    data: {
                        talkref: talkRef,
                        comment: $scope.newCommentText,
                        lastModified: $scope.aTalk.lastModified
                    }
                }).success(function (data) {
                    $scope.comments = data;
                    $scope.newCommentText = "";
                });
            };

            $scope.ratingClicked = function (givenRating) {
                $http({
                    method: "POST",
                    url: "data/giveRating",
                    data: {
                        talkref: talkRef,
                        rating: givenRating,
                        lastModified: $scope.aTalk.lastModified
                    }
                }).success(function (data) {
                    $scope.ratings = data;
                });
            };

            $scope.addPubComment = function () {
                $http({
                    method: "POST",
                    url: "data/addPubComment",
                    data: {
                        talkref: talkRef,
                        comment: $scope.newPubCommentText,
                        lastModified: $scope.aTalk.lastModified
                    }
                }).success(function (data) {
                    $scope.aTalk.pubcomments = data;
                    $scope.newPubCommentText = "";
                });

            };

            $scope.publishChanges = function () {
                $http({
                    method: "POST",
                    url: "data/publishChanges",
                    data: {
                        talkref: talkRef
                    }
                }).success(function (data) {
                    window.location.reload();
                });
            };

            $scope.newStarttime = function () {
                $http({
                    method: "POST",
                    url: "data/updateroomslot",
                    data: {
                        talkref: talkRef,
                        starttime: $scope.newstarttime
                    }
                }).success(function (data) {
                    window.location.reload();
                });
            };

            $scope.newRoom = function () {
                $http({
                    method: "POST",
                    url: "data/updateroomslot",
                    data: {
                        talkref: talkRef,
                        room: $scope.newroom
                    }
                }).success(function (data) {
                    window.location.reload();
                });
            };

            $scope.selectRoom = function (selroom) {
                $scope.newroom = selroom;
            }


        }]);

