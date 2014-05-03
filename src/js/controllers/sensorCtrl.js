var $ = require('jquery');

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

  $scope.selectSensor = function(item){
    if ($scope.isSelectedSensor(item)){
      $scope.selectedSensor = undefined;
    } else {
      $scope.selectedSensor = item;
    }
    $scope.selectedProbe = undefined;
  };

  $scope.isSelectedSensor = function(item){
    return $scope.selectedSensor === item;
  };
    
  $scope.anySensorSelected = function() {
    return $scope.selectedSensor !== undefined;
  };

  $scope.selectProbe = function(item){
    if ($scope.isSelectedProbe(item)){
      $scope.selectedProbe = undefined;
    } else {
      $scope.selectedProbe = item;
    }
  };

  $scope.isSelectedProbe = function(item){
    return $scope.selectedProbe === item;
  };
    
  $scope.anyProbeSelected = function() {
    return $scope.selectedProbe !== undefined;
  };
    
  $scope.close = function() {
    $scope.selectedSensor = undefined;
  };

  $scope.fetchSensors();
};