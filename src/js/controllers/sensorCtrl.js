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

  $scope.open = function(item){
    if ($scope.isOpen(item)){
      $scope.opened = undefined;
    } else {
      $scope.opened = item;
    }
  };

  $scope.isOpen = function(item){
    return $scope.opened === item;
  };
    
  $scope.anyItemOpen = function() {
    return $scope.opened !== undefined;
  };
    
  $scope.close = function() {
    $scope.opened = undefined;
  };

  $scope.fetchSensors();
};