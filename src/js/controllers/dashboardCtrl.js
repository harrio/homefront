var angular = require('angular');
var _ = require('lodash');

var format = d3.time.format.utc("%Y-%m-%dT%H:%M:%SZ");
var parseDate = format.parse;

exports.dashboardCtrl = function($scope, $http, $interval) {
  var stop;

  $scope.trendDown = function(values) {
    if (values.length < 2) {
      return false;
    } else {
      return values[0].value < values[1].value;
    }
  };

  $scope.trendUp = function(values) {
    if (values.length < 2) {
      return false;
    } else {
      return values[0].value > values[1].value;
    }
  };

  $scope.scheduleFetch = function() {
    if (angular.isDefined(stop)) {
      return;
    }

    stop = $interval(function() {
      $scope.fetchLatestData();
    }, 60000);
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

  $scope.fetchLatestData = function() {
    $http({method: 'GET', url: '/latestSensorData'}).
        success(function(data, status, headers, config) {
          $scope.sensorData = data;
      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
      });
  };

  $scope.fetchLatestData();
  $scope.scheduleFetch();
};
