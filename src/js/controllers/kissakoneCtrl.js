var $ = require('jquery');
var _ = require('lodash');

var format = d3.time.format.utc("%Y-%m-%dT%H:%M:%SZ");
var parseDate = format.parse;
var formatOut = d3.time.format("%d.%m.%Y %H:%M");

exports.kissakoneCtrl = function($scope, $http, $location, $interval) {

  $scope.formatDate = function(timeStr) {
    var date = parseDate(timeStr);
    return formatOut(date);
  };

  $scope.format = 'dd.MM.yyyy';
    $scope.dateOptions = {
      formatYear: 'yy',
      startingDay: 1
    };

  $scope.showSaveFailed = false;
  $scope.showSaveOk = false;
  $scope.feedings = [];

  $scope.openDate = function($event) {
      $event.preventDefault();
      $event.stopPropagation();

      $scope.dateOpened = true;
    };

  $scope.fetchFeedings = function() {
    $scope.showSaveOk = false;
    $scope.showSaveFailed = false;
    $http({ method: 'GET', url: '/feedings' }).
        success(function(data, status, headers, config) {
          $scope.feedings = data;
      }).
      error(function(data, status, headers, config) {
        console.log("feedings failed: " + status);
      });
  };

  $scope.saveFeeding = function() {
    var timeOfDay = new Date("October 13, 2014 " + $scope.form.time + ":00");
    $scope.form.date.setHours(timeOfDay.getHours());
    $scope.form.date.setMinutes(timeOfDay.getMinutes());

    $http.post('/saveFeeding', { time: $scope.form.date }).
      success(function(data) {
        $scope.fetchFeedings();
        $.notify("Feed time saved", "success");
      }).
      error(function(data) {
        $scope.fetchFeedings();
        $.notify("Save failed", "error");
      });
  };

  $scope.fetchFeedings();
};
