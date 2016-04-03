(function () {
    "use strict";

    angular.module('cakeReduxModule', ['ngRoute']);

    angular.module('cakeRedux', ['cakeReduxModule']).
    config(['$routeProvider', function($routeProvider) {
        $routeProvider.when("/", {
            templateUrl: 'templates/talkOverview.html',
            controller: 'TalkOverviewController'
        }).when("/talks/:eventSlug", {
            templateUrl: 'templates/talkOverview.html',
            controller: 'TalkOverviewController'
        }).when("/showTalk/:talkId", {
            templateUrl: 'templates/showTalk.html',
            controller: 'ShowTalkCtrl'

        }).when("/accept", {
            templateUrl: 'templates/acceptTalk.html',
            controller: 'AcceptTalkCtrl'
        }).when("/publish", {
            templateUrl: 'templates/publishTalk.html',
            controller: 'PublishTalkCtrl'
        }).when("/massUpdate", {
            templateUrl: 'templates/acceptTalk.html',
            controller: 'AcceptTalkCtrl'
        }).when("/showSchedule", {
            templateUrl: 'templates/showSchedule.html',
            controller: 'ShowScheduleCtrl'
        }).when("/report", {
            templateUrl: "templates/talkReport.html",
            controller: "TalkReportCtrl"
        })
        ;
    }])
    .run(['$rootScope',
        function($rootScope) {}
    ]);

        


}());
