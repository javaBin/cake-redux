angular.module('cakeReduxModule')
    .controller('TalkReportCtrl', ['$scope', '$http', '$routeParams', 'talkList','roomSlotFactory','slotFilterService',
        function($scope, $http, $routeParams,talkList,roomSlotFactory,slotFilterService) {
            $scope.talks = talkList.talks;

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


        }
    ]
);