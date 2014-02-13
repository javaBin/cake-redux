(function() {
    angular.module('cakeReduxModule', []);

    var bootstrap;
    bootstrap = function() {
        angular.module('cakeRedux', ['cakeReduxModule']).
        config(['$routeProvider', function($routeProvider) {
                $routeProvider.
                    when('/', {
                        templateUrl: 'templates/talkList.html',
                        controller: 'TalkListCtrl'
                    }).
                    when("/talks/:eventSlug", {
                        templateUrl: 'templates/talkList.html',
                        controller: 'TalkListCtrl'
                    })
                    ;
        }]);
        
        angular.bootstrap(document,['cakeRedux']);
        
    };

    bootstrap();


}());
