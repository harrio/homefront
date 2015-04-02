var angular = require('angular');
var _ = require('lodash');
var moment = require('moment');

var format = d3.time.format.utc("%Y-%m-%dT%H:%M:%SZ");
var parseDate = format.parse;
var formatOut = d3.time.format("%d.%m.%Y %H:%M")

exports.dashboardCtrl = function($scope, $http, $interval) {
  var stop;

  $scope.formatDate = function(timeStr) {
    var date = parseDate(timeStr);
    return formatOut(date);
  };

  $scope.trendDown = function(values) {
    if ($scope.probeTimeout(values[0])) {
      return false;
    }

    if (values.length < 2) {
      return false;
    } else {
      return values[0].value < values[1].value;
    }
  };

  $scope.trendUp = function(values) {
    if ($scope.probeTimeout(values[0])) {
      return false;
    }

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

  $scope.probeTimeout = function(value) {
    var m = moment(parseDate(value.time));
    var now = moment();
    var diff = now.diff(m, 'minutes');
    return diff > 10;
  }

  $scope.catTimeout = function() {
    if ($scope.catHeartbeat == null) {
      return true;
    }
    var m = moment(parseDate($scope.catHeartbeat.time));
    var now = moment();
    var diff = now.diff(m, 'minutes');
    return diff > 2;
  }

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

    $http({method: 'GET', url: '/catHeartbeat'}).
        success(function(data, status, headers, config) {
          $scope.catHeartbeat = data;
      }).
      error(function(data, status, headers, config) {
        console.log("heartbeat failed: " + status);
    });
  };

  $scope.fetchLatestData();
  $scope.scheduleFetch();
};
