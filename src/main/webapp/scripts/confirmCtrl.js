(function() {

    angular.module('openCakeModule', []);

    angular.module('cakeOpen',['openCakeModule'])
    .controller('ConfirmCtrl', ['$scope', '$http',
        function($scope, $http) {
            $scope.noe = "Hoi";
        }]);

    angular.bootstrap(document,['cakeOpen']);
}());