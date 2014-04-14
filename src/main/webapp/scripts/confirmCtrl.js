(function() {

    angular.module('openCakeModule', []);

    angular.module('cakeOpen',['openCakeModule'])
    .controller('ConfirmCtrl', ['$scope', '$http',
        function($scope, $http) {
            var para = function(name){
                var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
                return results ? results[1] || 0 : 0;
            };
            $scope.dinner = "yes";
            var talkId = para("id");
            $http({method: "GET", url: "data/atalk?talkId=" + talkId})
                .success(function(data) {
                   $scope.talk = data;
                   $scope.noe = data.title;
                });
            $scope.noe = para("key");
        }]);

    angular.bootstrap(document,['cakeOpen']);
}());