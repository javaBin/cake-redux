(function () {
    "use strict";

    angular.module('cakeReduxModule', ['ngRoute']);

    angular.module('cakeRedux', ['cakeReduxModule']).
    config(['$routeProvider', function($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: 'showTalk.html',
            controller: 'ShowTalkCtrl'
        })
        ;
    }])
    .run(['$rootScope',
        function($rootScope) {}
    ]);

        


}());
