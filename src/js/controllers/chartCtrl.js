var angular = require('angular');
var _ = require('lodash');

var format = d3.time.format.utc("%Y-%m-%dT%H:%M:%SZ");
var parseDate = format.parse;

var makeData = function(dataArrays) {
  var data = [];
  for (var key in dataArrays) {
    data.push({ "key": key, "values": dataArrays[key] });
  }

  return data;
};

var makePoint = function(item) {
  return [ parseDate(item.time).getTime(), item.value ];
};

var makeProbe = function(probeData) {
  return { label: probeData.name, data: _.map(probeData.temperature, makePoint) };
}

var makeSensor = function(sensorData) {
  return { name: sensorData.name, data: _.map(sensorData.probe, makeProbe) };
}

var makeSensors = function(data) {
  return _.map(data, makeSensor);
}

var makeFlot = function(dataArrays) {
  var data = [];
  for (var key in dataArrays) {
    data.push({ label: key, data: _.map(dataArrays[key], makePoint) });
  }

  return data;
};

exports.chartCtrl = function($scope, $http, $interval) {
  var stop;

  $scope.flotOptions = {
              grid: {
              hoverable: true,
              borderColor: "#9d9d9d",
              borderWidth: 1,
              tickColor: "#9d9d9d"
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
              content: "%s %x: %y.2",
              shifts: {
                x: -60,
                y: 25
              }
            },
            yaxis: {
              show: true,
              color: "#000000"
            },
            xaxis: {
              axisLabel: "Time",
              show: true,
              color: "#000000",
              mode: "time",
              timeformat: "%d.%m.%Y %H:%M",
              timezone: "browser"
            }
          };

  $scope.startTime = new Date();
  $scope.endTime = new Date();
  $scope.format = 'dd.MM.yyyy';
  $scope.dateOptions = {
    formatYear: 'yy',
    startingDay: 1
  };

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

  $scope.openStart = function($event) {
    $event.preventDefault();
    $event.stopPropagation();

    $scope.startOpened = true;
  };

  $scope.openEnd = function($event) {
    $event.preventDefault();
    $event.stopPropagation();

    $scope.endOpened = true;
  };

  $scope.fetchSensors = function() {
    $http({method: 'GET', url: '/sensorData', params: { start: format($scope.startTime), end: format($scope.endTime) }}).
        success(function(data, status, headers, config) {

          $scope.sensors = makeSensors(data);

      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
      });
  };

  $scope.fetchSensors();
};
