var angular = require('angular');

var parseDate = d3.time.format("%Y%m%dT%H%M%S.%LZ").parse;

var makeData = function(dataArrays) {
  var data = [];
  for (var key in dataArrays) {
    data.push({ "key": key, "values": dataArrays[key] });
  }

  return data;
};

exports.tempGraphCtrl = function($scope, $http, $interval) {

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
    $http({method: 'GET', url: '/sensors', params: { start: "2014-04-06T00:00:00Z", end: "2014-04-07T00:00:00Z" }}).
        success(function(data, status, headers, config) {
          $scope.tempData1 = makeData(data[0]);
          $scope.tempData2 = makeData(data[1]);
      }).
      error(function(data, status, headers, config) {
        console.log("temps failed: " + status);
      });
  };

  $scope.fetchSensors();
};