angular.module('cakeReduxModule')
    .factory("eventFactory",["$http", function($http) {
        var res = function (scope) {
            scope.events =
                //[{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy8xY2E4OWFlZi1jN2NlLTQ1YjUtOTVhMC02MzBmNTVkN2VmYTY=","name":"JavaZone 2006"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy80MDdmMzY4Zi00MWQ0LTQyNDgtYjgyNS0zOWQwZjk3M2RmZTE=","name":"JavaZone 2007"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy81ZTEzMDM3Mi0yODVkLTQ5Y2QtYWVkYi03ZjMwNmQ5N2IwNGQ=","name":"JavaZone 2008"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9iNTgyYTA3MS1kNGMyLTRhNDgtYWM2Ni04MTJhNWVmOTRjMWI=","name":"JavaZone 2009"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy8yOGExYzVjMi1iNjFlLTRkMmYtYTY3ZS1iNjBmOWVmZGMyYTg=","name":"JavaZone 2010"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy8xMzA3ZGJjYy0wNDhlLTRmODAtOWZhYS1mZmExYmVmNDBmZGE=","name":"JavaZone 2011"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy80YzE4ZjQ1YS0wNTRhLTQ2OTktYTJiYy02YTU5YTlkZDgzODI=","name":"JavaZone 2012"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy9jZWUzN2NjMS01Mzk5LTQ3ZWYtOTQxOC0yMWY5YjY0NDRiZmE=","name":"JavaZone 2013"},{"ref":"aHR0cDovL3Rlc3QuMjAxNC5qYXZhem9uZS5uby9lbXMvc2VydmVyL2V2ZW50cy85ZjQwMDYzYS01ZjIwLTRkN2ItYjFlOC1lZDBjNmNjMThhNWY=","name":"JavaZone 2014"}]
            []

            $http({method: "GET", url: "data/events"})
                .success(function(data) {
                    scope.events = data;
                });

        };
         return res;
    }]);
