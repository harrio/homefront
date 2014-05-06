var $ = require('jquery');
var _ = require('lodash');

exports.sensorCtrl = function($scope, $http, $location, $interval) {

  $scope.showSaveFailed = false;
  $scope.showSaveOk = false;
  
  $scope.fetchSensors = function() {
    $scope.showSaveOk = false;
    $scope.showSaveFailed = false;
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
      $scope.selectedSensor = angular.copy(item);
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
    $http.delete('/deleteSensor/' + deleted._id).
      success(function(data) {
        $scope.fetchSensors();
        $scope.showSaveOk = true;
      }).
      error(function(data) {
        $scope.fetchSensors();
        $scope.showSaveFailed = true;
      });
  };

  $scope.addSensor = function() {
    var newSensor = { name: "", key: "", mac: "", active: true, probes: [] };
    $scope.sensors.push(newSensor);
    $scope.selectSensor(newSensor);
  };

  $scope.saveSensor = function() {
    $http.post('/saveSensor', $scope.selectedSensor).
      success(function(data) {
        $scope.fetchSensors();
        $scope.showSaveOk = true;
      }).
      error(function(data) {
        $scope.fetchSensors();
        $scope.showSaveFailed = true;
      });
  };

  $scope.selectProbe = function(item, $index) {
    if ($scope.isSelectedProbe(item)) {
      $scope.selectedProbe = undefined;
    } else {
      $scope.selectedProbe = angular.copy(item);
      $scope.selectedProbeIndex = $index;
    }
  };

  $scope.isSelectedProbe = function(item) {
    return $scope.selectedProbe === item;
  };
    
  $scope.anyProbeSelected = function() {
    return $scope.selectedProbe !== undefined;
  };

  $scope.saveProbe = function() {
    //var probe = _.find($scope.selectedSensor.probes, function(item) { return item._id === $scope.selectedProbe._id; });
    var probe = $scope.selectedSensor.probes[$scope.selectedProbeIndex];
    if (probe === undefined) {
      probe = { name: "", key: "", humidity: false };
      $scope.selectedSensor.probes.push(probe);
    }
    probe.name = $scope.selectedProbe.name;
    probe.key = $scope.selectedProbe.key;
    probe.humidity = $scope.selectedProbe.humidity;
  };

  $scope.deleteProbe = function() {
    var deleted = $scope.selectedProbe;
    $scope.selectedProbe = undefined;
    $scope.selectedSensor.probes = _.reject($scope.selectedSensor.probes, function(probe) { return deleted._id === probe._id; });
  };

  $scope.addProbe = function() {
    if ($scope.selectedSensor.probes === undefined) {
      $scope.selectedSensor.probes = [];
    }
    var newProbe = { name: "", key: "", humidity: false };
    $scope.selectProbe(newProbe);
  };
    
  $scope.close = function() {
    $scope.selectedSensor = undefined;
    $scope.selectedProbe = undefined;
  };

  $scope.fetchSensors();
};