var angular = require('angular');
var _ = require('lodash');

var parseDate = d3.time.format("%Y%m%dT%H%M%S.%LZ").parse;

var makeData = function(dataArrays) {
  var data = [];
  for (var key in dataArrays) {
    data.push({ "key": key, "values": dataArrays[key] });
  }

  return data;
};

var makePoint = function(item) {
  return [ parseDate(item.time), item.temp ];
};

var makeFlot = function(dataArrays) {
  var data = [];
  for (var key in dataArrays) {
    data.push({ label: key, data: _.map(dataArrays[key], makePoint) });
  }

  return data;
};

exports.dashboardCtrl = function($scope, $http, $interval) {
  var stop;

  $scope.fetchData = function() {
    if (angular.isDefined(stop)) {
      return;
    }
  
    stop = $interval(function() {
      $http({method: 'GET', url: '/sensors'}).
        success(function(data, status, headers, config) {
          $scope.sensors = data;
      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
      });
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

  $scope.getX = function(){
    return function(d) {
      return parseDate(d.time);
    };
  };

  $scope.getY = function(){
    return function(d) {
      return d.temp;
    };
  };

  $scope.xAxisTickFormat = function() {
    return function(d) {
      return d3.time.format('%H:%M:%S')(new Date(d));
    };
  };

  $scope.fetchSensors = function() {
    $http({method: 'GET', url: '/sensorData', params: { start: "2014-04-06T10:00:00Z", end: "2014-04-06T11:00:00Z" }}).
        success(function(data, status, headers, config) {
          $scope.tempData1 = makeData(data[0]);
          $scope.tempData2 = makeData(data[1]);

          $scope.flotData1 = makeFlot(data[0]);
          $scope.flotData2 = makeFlot(data[1]);
          $scope.flotOptions = {
            grid: {
              hoverable: true,
              borderColor: "#f3f3f3",
              borderWidth: 1,
              tickColor: "#f3f3f3"
            },
            series: {
              shadowSize: 0,
              lines: {
                show: true
              },
              points: {
                show: true
              }
            },
            lines: {
              fill: false,
              color: ["#3c8dbc", "#f56954"]
            },
            tooltip: true,
            tooltipOpts: {
              content: "%s %x: %y",
              shifts: {
                x: -60,
                y: 25
              }
            },
            yaxis: {
              show: true,
            },
            xaxis: {
              show: true,
              mode: "time",
              timeformat: "%d.%m.%Y %H:%M",
              timezone: "browser"
            }
          };
      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
      });
  };

  $scope.fetchSensors();
};