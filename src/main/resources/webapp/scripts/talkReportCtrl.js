angular.module('cakeReduxModule')
    .controller('TalkReportCtrl', ['$scope', '$http', '$routeParams', 'talkList','roomSlotFactory','slotFilterService',
        function($scope, $http, $routeParams,talkList,roomSlotFactory,slotFilterService) {
            $scope.talks = talkList.talks;
        }
    ]
);