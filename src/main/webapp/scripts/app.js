(function() {
    angular.module('cakeReduxModule', []);

    var bootstrap;
    bootstrap = function() {
        angular.module('quizzical', ['cakeReduxModule']).
        config(['$routeProvider', function($routeProvider) {
                $routeProvider.
                    when('/', {
                        templateUrl: 'templates/talkList.html',
                        controller: 'TalkListCtrl'
                    });
        }]);
        
        angular.bootstrap(document,['quizzical']); 
        
    };

    bootstrap();


}());
