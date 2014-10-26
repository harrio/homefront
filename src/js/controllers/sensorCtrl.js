var $ = require('jquery');
var _ = require('lodash');

exports.sensorCtrl = function($scope, $http, $location, $interval) {

  $scope.showSaveFailed = false;
  $scope.showSaveOk = false;
  $scope.sensors = [];
  $scope.groups = [];

  $scope.fetchGroups = function() {
    $scope.showSaveOk = false;
    $scope.showSaveFailed = false;
    $http({ method: 'GET', url: '/groups' }).
        success(function(data, status, headers, config) {
          $scope.groups = data;
      }).
      error(function(data, status, headers, config) {
        console.log("groups failed: " + status);
      });
  };

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
    return $scope.anySensorSelected() && $scope.selectedSensor.sensor_id === item.sensor_id;
  };

  $scope.anySensorSelected = function() {
    return $scope.selectedSensor !== undefined;
  };

  $scope.deleteSensor = function() {
    var deleted = $scope.selectedSensor;
    $scope.close();
    $http.delete('/deleteSensor/' + deleted.sensor_id).
      success(function(data) {
        $scope.fetchSensors();
        $.notify("Sensor deleted", "success");
      }).
      error(function(data) {
        $scope.fetchSensors();
        $.notify("Delete failed", "error");
      });
  };

  $scope.addSensor = function() {
    var newSensor = { name: "", key: "", mac: "", active: true, probe: [] };
    $scope.sensors.push(newSensor);
    $scope.selectSensor(newSensor);
  };

  $scope.saveSensor = function() {
    $http.post('/saveSensor', $scope.selectedSensor).
      success(function(data) {
        $scope.fetchSensors();
        $.notify("Sensor saved", "success");
      }).
      error(function(data) {
        $scope.fetchSensors();
        $.notify("Save failed", "error");
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
    var probe = $scope.selectedSensor.probe[$scope.selectedProbeIndex];
    if (probe === undefined) {
      probe = { name: "", key: "", humidity: false };
      $scope.selectedSensor.probe.push(probe);
    }
    probe.name = $scope.selectedProbe.name;
    probe.key = $scope.selectedProbe.key;
    probe.humidity = $scope.selectedProbe.humidity;
    probe.sensor_id = $scope.selectedProbe.sensor_id;
    probe.group_id = $scope.selectedProbe.group_id;
  };

  $scope.deleteProbe = function() {
    var deleted = $scope.selectedSensor.probe[$scope.selectedProbeIndex];
    $scope.selectedProbe = undefined;
    $scope.selectedSensor.probe = _.reject($scope.selectedSensor.probe, function(probe) { return deleted === probe; });
  };

  $scope.addProbe = function() {
    if ($scope.selectedSensor.probe === undefined) {
      $scope.selectedSensor.probe = [];
    }
    var newProbe = { name: "", key: "", humidity: false, sensor_id: $scope.selectedSensor.sensor_id };
    $scope.selectProbe(newProbe);
  };

  $scope.close = function() {
    $scope.selectedSensor = undefined;
    $scope.selectedProbe = undefined;
  };

  $scope.groupName = function(group_id) {
    var group = _.find($scope.groups, { 'group_id': group_id });
    if (group) {
      return group.name;
    } else {
      return "";
    }
  }


  $scope.fetchGroups();
  $scope.fetchSensors();
};
