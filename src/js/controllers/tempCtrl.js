var angular = require('angular');

exports.tempCtrl = function($scope, $http, $interval) {
  var stop;

  $scope.fetchTemps = function() {
    if (angular.isDefined(stop)) {
      return;
    }
  
    stop = $interval(function() {
      $http({method: 'GET', url: '/temps'}).
        success(function(data, status, headers, config) {
          $scope.temps = data;
      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
      });
    }, 1000);
  };

  $scope.stopFetch = function() {
    if (angular.isDefined(stop)) {
      $interval.cancel(stop);
      stop = undefined;
    }
  };

  $scope.$on('$destroy', function() {
    // Make sure that the interval is destroyed too
    $scope.stopFetch();
  });

  $scope.fetchTemps();
};