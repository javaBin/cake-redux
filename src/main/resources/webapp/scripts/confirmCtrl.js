(function() {

    angular.module('openCakeModule', []);

    angular.module('cakeOpen',['openCakeModule'])
        .run(['$rootScope',
            function($rootScope) {}
        ])
    .controller('ConfirmCtrl', ['$scope', '$http',
        function($scope, $http) {
            $scope.showMain = false;
            $scope.showSuccess = false;
            $scope.showError = false;
            var para = function(name){
                var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
                return results ? results[1] || 0 : 0;
            };
            $scope.dinner = "yes";
            var talkId = para("id");
            $http({method: "GET", url: "data/atalk?talkId=" + talkId})
                .success(function(data) {
                    var status = data.status;
                    if (status === "error") {
                        $scope.message = data.message;
                        $scope.showMain = false;
                        $scope.showError = true;
                        return;
                    }
                   $scope.showMain = true;
                   $scope.talk = data.talk;
                });
            $scope.confirmTalk = function() {
                var din = $scope.dinner;
                var postData = {
                    id      : talkId,
                    dinner  : din,
                    contactPhone: $scope.contactPhone
                };
                $http({
                    method: "POST",
                    url: "data/atalk",
                    data: postData
                }).success(function(data) {
                    $scope.showMain = false;
                    if (data.status === "ok") {
                        $scope.showSuccess = true;
                    } else {
                        $scope.showError = true;
                        $scope.message = data.message;
                    }

                });
            };


        }]);

}());