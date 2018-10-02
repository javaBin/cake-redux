angular.module('cakeReduxModule')
    .controller('TalkReportCtrl', ['$scope', '$http', '$routeParams', 'talkList','roomSlotFactory','slotFilterService','filterService',
        function($scope, $http, $routeParams,talkList,roomSlotFactory,slotFilterService, filterService) {
            //$scope.talks = talkList.talks;
            $scope.talks = filterService.filteredTalks;


            $scope.showTitle = true;
            $scope.showId = true;
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
            $scope.showLength = true;
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
                if ($scope.showId) {
                    headers.push("id");
                }
                if ($scope.showTitle) {
                    headers.push("title");
                }
                if ($scope.showLength) {
                    headers.push("length")
                }
                if ($scope.showSpeakers) {
                    headers.push("speakernames");
                    if ($scope.showSpeakerEmail) {
                        headers.push("speakeremail");
                    }
                }
                if ($scope.showEquipment) {
                    headers.push("equipment");
                }
                if ($scope.showTags) {
                    headers.push("tags");
                }
                if ($scope.showRoom) {
                    headers.push("room")
                }
                if ($scope.showSlot) {
                    headers.push("slot")
                }
                if (headers.length === 0) {
                    $scope.exportText = "Nothing to export";
                    return;
                }
                var headerRow = reduceToSemicolonDiv(headers);
                var allNonSpeakers = [$scope.showTitle, $scope.showId, $scope.showSummary, $scope.showAbstract, $scope.showPostcode, $scope.showOutline, $scope.showKeywords, $scope.showAudience, $scope.showEquipment, $scope.showState, $scope.showPublished, $scope.showFormat, $scope.showLength, $scope.showLevel, $scope.showLanguage, $scope.showRoom, $scope.showSlot,  $scope.showLastModified, $scope.showTags, $scope.showContactPhone];
                var displaylines = filteredTalks;
                if ($scope.showSpeakers && !_.some(allNonSpeakers,function (a) {return a;})) {
                    displaylines = _.flatten(_.map(filteredTalks,function (talk) {
                        return talk.speakers;
                    }));
                }
                var mapped = _.map(displaylines,function (theTalk) {
                    var theline = [];
                    if ($scope.showId) {
                        theline.push(theTalk.ref);
                    }
                    if ($scope.showTitle) {
                        theline.push(theTalk.title);
                    }
                    if ($scope.showLength) {
                        theline.push(theTalk.length);
                    }
                    if ($scope.showSpeakers) {
                        if (theTalk.speakers) {
                            var speakerNames = _.reduce(_.map(theTalk.speakers,function (theSpeaker) {
                                return theSpeaker.name;
                            }), function (a, b) {
                                return a + " and " + b;
                            });
                            theline.push(speakerNames);
                            if ($scope.showSpeakerEmail) {
                                var speakeremail = _.reduce(_.map(theTalk.speakers,function (theSpeaker) {
                                    return theSpeaker.email;
                                }), function (a, b) {
                                    return a + " and " + b;
                                });
                                theline.push(speakeremail);
                            }
                        } else {
                            if ($scope.showSpeakername) {
                                theline.push(theTalk.name);
                            }
                            if ($scope.showSpeakerEmail) {
                                theline.push(theTalk.email);
                            }
                        }

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
                    if ($scope.showRoom) {
                        if (theTalk.room) {
                            theline.push(theTalk.room.name);
                        } else {
                            theline.push("-");
                        }
                    }
                    if ($scope.showSlot) {
                        if (theTalk.slot) {
                            theline.push(theTalk.slot.start);
                        } else {
                            theline.push("-");
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