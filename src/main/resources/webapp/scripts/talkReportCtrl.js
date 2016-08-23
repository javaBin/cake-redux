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

            $scope.exportCsv = function () {
                var filteredTalks;
                if ($scope.showEquipment) {
                    filteredTalks = _.filter($scope.talks, function (theTalk) {
                        return theTalk.equipment && (theTalk.equipment.trim().length > 0);
                    });
                } else {
                    filteredTalks = $scope.talks;
                }
                var mapped = _.map(filteredTalks,function (theTalk) {
                    var speakerNames = _.reduce(_.map(theTalk.speakers,function (theSpeaker) {
                        return theSpeaker.name;
                    }), function (a, b) {
                        return a + " and " + b;
                    });
                    var equip = "";
                    if ($scope.showEquipment) {
                        equip = ";" + theTalk.equipment;
                    }
                    return theTalk.title + ";" + speakerNames + equip;
                });

                var headerRow = "title;speakernames";
                if ($scope.showEquipment) {
                    headerRow = headerRow + ";equipment";
                }
                $scope.exportText = headerRow + "\n" + _.reduce(mapped,function(a,b) {
                    return a + "\n" + b;
                })
            };


        }
    ]
);