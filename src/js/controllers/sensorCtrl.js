var $ = require('jquery');
var _ = require('lodash');

exports.sensorCtrl = function($scope, $http, $interval) {

  $("#mac").inputmask("Regex", { "oncomplete": function(){ alert('inputmask complete'); }});

  $scope.fetchSensors = function() {
    $http({ method: 'GET', url: '/sensors' }).
        success(function(data, status, headers, config) {
          $scope.sensors = data;
      }).
      error(function(data, status, headers, config) {
        console.log("sensors failed: " + status);
      });
  };

  $scope.selectSensor = function(item) {
    if ($scope.isSelectedSensor(item)) {
      $scope.selectedSensor = undefined;
    } else {
      $scope.selectedSensor = item;
    }
    $scope.selectedProbe = undefined;
  };

  $scope.isSelectedSensor = function(item) {
    return $scope.anySensorSelected() && $scope.selectedSensor._id === item._id;
  };
    
  $scope.anySensorSelected = function() {
    return $scope.selectedSensor !== undefined;
  };

  $scope.deleteSensor = function() {
    var deleted = $scope.selectedSensor;
    $scope.close();
    $scope.sensors = _.reject($scope.sensors, function(sensor) { return deleted._id === sensor._id; });
  };

  $scope.selectProbe = function(item) {
    if ($scope.isSelectedProbe(item)) {
      $scope.selectedProbe = undefined;
    } else {
      $scope.selectedProbe = angular.copy(item);
    }
  };

  $scope.isSelectedProbe = function(item) {
    return $scope.selectedProbe === item;
  };
    
  $scope.anyProbeSelected = function() {
    return $scope.selectedProbe !== undefined;
  };

  $scope.saveProbe = function() {
    var probe = _.find($scope.selectedSensor.probes, function(item) { return item.id === $scope.selectedProbe.id; });
    probe.name = $scope.selectedProbe.name;
    probe.id = $scope.selectedProbe.id;
    probe.humidity = $scope.selectedProbe.humidity;
  };
    
  $scope.close = function() {
    $scope.selectedSensor = undefined;
    $scope.selectedProbe = undefined;
  };

  $scope.fetchSensors();
};