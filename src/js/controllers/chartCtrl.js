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

var makeGroups = function(data) {
  return _.map(data, makeSensor);
}

var makeProbeHumidity = function(probeData) {
  return { label: probeData.name, data: _.map(probeData.humidity, makePoint) };
}

var makeSensorHumidity = function(sensorData) {
  return { name: sensorData.name, data: _.map(sensorData.probe, makeProbeHumidity) };
}

var makeGroupsHumidity = function(data) {
  return _.map(data, makeSensorHumidity);
}

var makeFlot = function(dataArrays) {
  var data = [];
  for (var key in dataArrays) {
    data.push({ label: key, data: _.map(dataArrays[key], makePoint) });
  }

  return data;
};

var makeStartTime = function() {
  var time = new Date();
  time.setHours(0);
  time.setMinutes(0);
  time.setSeconds(0);
  time.setMilliseconds(0);
  return time;
}

var makeEndTime = function() {
  var time = new Date();
  time.setHours(23);
  time.setMinutes(59);
  time.setSeconds(59);
  time.setMilliseconds(0);
  return time;
}

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
            },
            legend: {
              show: true,
              noColumns: 3,
              position: "ne",
            }
          };

  $scope.loadingTemps = false;
  $scope.loadingHums = false;
  $scope.startTime = makeStartTime();
  $scope.endTime = makeEndTime();
  $scope.format = 'dd.MM.yyyy';
  $scope.dateOptions = {
    formatYear: 'yy',
    startingDay: 1
  };

  $scope.loading = function() {
    return $scope.loadingTemps || $scope.loadingHums;
  }

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

  $scope.fetchGroups = function() {
    $http({method: 'GET', url: '/groupData', params: { start: format($scope.startTime), end: format($scope.endTime) }}).
        success(function(data, status, headers, config) {

          $scope.groups = makeGroups(data, makeProbe);
          $scope.loadingTemps = false;

      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
        $scope.loadingTemps = false;
      });
  };

  $scope.fetchGroupsHumidity = function() {
    $http({method: 'GET', url: '/groupHumidityData', params: { start: format($scope.startTime), end: format($scope.endTime) }}).
        success(function(data, status, headers, config) {

          $scope.groupsHumidity = makeGroupsHumidity(data);
          $scope.loadingHums = false;
      }).
      error(function(data, status, headers, config) {
        console.log("hums failed: " + status);
        $scope.loadingHums = false;
      });
  };

  $scope.fetchAll = function() {
    $scope.loadingTemps = true;
    $scope.loadingHums = true;
    $scope.fetchGroups();
    $scope.fetchGroupsHumidity();
  }

  $scope.fetchAll();
};
