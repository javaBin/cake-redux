angular.module('cakeReduxModule')
    .controller('TalkReportCtrl', ['$scope', '$http', '$routeParams', 'talkList','roomSlotFactory','slotFilterService','filterService',
        function($scope, $http, $routeParams,talkList,roomSlotFactory,slotFilterService, filterService) {
            //$scope.talks = talkList.talks;
            $scope.talks = filterService.filteredTalks;


            $scope.showTitle = true;
            $scope.showSummary = true;
            $scope.showAbstract = true;
            $scope.showPostcode = true;
            $scope.showOutline = true;
            $scope.showKeywords = true;
            $scope.showAudience = true;
            $scope.showEquipment = true;
            $scope.showState = true;
            $scope.showPublished = true;
            $scope.showFormat = true;
            $scope.showLevel = true;
            $scope.showLanguage = true;
            $scope.showRoom = true;
            $scope.showSlot = true;
            $scope.showSpeakers = true;
            $scope.showSpeakername = true;
            $scope.showSpeakerbio = true;
            $scope.showSpeakerEmail = true;
            $scope.showPostcode = true;
            $scope.showLastModified = true;
            $scope.showTags = true;
            $scope.showContactPhone = true;

            var updateFromServer = function(data) {
                var atalk = _.findWhere($scope.talks,{ref:data.ref});
                if (!atalk) {
                    return;
                }
                for (var prop in data) {
                    atalk[prop] = data[prop];
                }


            };

            $scope.talks.forEach(function(talk) {
                $http({method: "GET", url: "data/atalk?talkId=" + talk.ref})
                    .success(updateFromServer);
            });

            $scope.exportText = "xxx";

            var reduceToSemicolonDiv = function(arr) {
                return _.reduce(arr,function(a,b) {
                    return a + ";" + b;
                });
            };

            $scope.exportCsv = function () {
                var filteredTalks;
                if ($scope.showEquipment) {
                    filteredTalks = _.filter($scope.talks, function (theTalk) {
                        return theTalk.equipment && (theTalk.equipment.trim().length > 0);
                    });
                } else {
                    filteredTalks = $scope.talks;
                }
                var headers = [];
                if ($scope.showTitle) {
                    headers.push("title");
                }
                if ($scope.showSpeakers) {
                    headers.push("speakernames");
                }
                if ($scope.showEquipment) {
                    headers.push("equipment");
                }
                if ($scope.showTags) {
                    headers.push("tags");
                }
                if (headers.length === 0) {
                    $scope.exportText = "Nothing to export";
                    return;
                }
                var headerRow = reduceToSemicolonDiv(headers);
                var mapped = _.map(filteredTalks,function (theTalk) {
                    var theline = [];
                    if ($scope.showTitle) {
                        theline.push(theTalk.title);
                    }
                    if ($scope.showSpeakers) {
                        var speakerNames = _.reduce(_.map(theTalk.speakers,function (theSpeaker) {
                            return theSpeaker.name;
                        }), function (a, b) {
                            return a + " and " + b;
                        });
                        theline.push(speakerNames);
                    }
                    if ($scope.showEquipment) {
                        theline.push(theTalk.equipment);
                    }
                    if ($scope.showTags) {
                        if (theTalk.tags.length === 0) {
                            theline.push("-");
                        } else {
                            var taglist = _.reduce(theTalk.tags,function (a, b) {
                                return a + "+" + b;
                            });
                            theline.push(taglist);
                        }

                    }

                    return reduceToSemicolonDiv(theline);
                });

                $scope.exportText = headerRow + "\n" + _.reduce(mapped,function(a,b) {
                    return a + "\n" + b;
                })
            };


        }
    ]
);