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
                    }).
                    when("/showTalk/:talkId", {
                        templateUrl: 'templates/showTalk.html',
                        controller: 'ShowTalkCtrl'
                    }).
                    when("/accept", {
                        templateUrl: 'templates/acceptTalk.html',
                        controller: 'AcceptTalkCtrl'
                    }).
                    when("/massUpdate", {
                        templateUrl: 'templates/acceptTalk.html',
                        controller: 'AcceptTalkCtrl'
                    })
                    ;
        }]);
        
        angular.bootstrap(document,['cakeRedux']);
        
    };

    bootstrap();


}());
