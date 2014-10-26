var $ = require('jquery');
var _ = require('lodash');

exports.groupCtrl = function($scope, $http, $location, $interval) {

  $scope.showSaveFailed = false;
  $scope.showSaveOk = false;
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

  $scope.selectGroup = function(item) {
    if ($scope.isSelectedGroup(item)) {
      $scope.selectedGroup = undefined;
    } else {
      $scope.selectedGroup = angular.copy(item);
    }
  };

  $scope.isSelectedGroup = function(item) {
    return $scope.anyGroupSelected() && $scope.selectedGroup.group_id === item.group_id;
  };

  $scope.anyGroupSelected = function() {
    return $scope.selectedGroup !== undefined;
  };

  $scope.deleteGroup = function() {
    var deleted = $scope.selectedGroup;
    $scope.close();
    $http.delete('/deleteGroup/' + deleted.group_id).
      success(function(data) {
        $scope.fetchGroups();
        $.notify("Group deleted", "success");
      }).
      error(function(data) {
        $scope.fetchGroups();
        $.notify("Delete failed", "error");
      });
  };

  $scope.addGroup = function() {
    var newGroup = { name: "", index: "" };
    $scope.groups.push(newGroup);
    $scope.selectGroup(newGroup);
  };

  $scope.saveGroup = function() {
    $http.post('/saveGroup', $scope.selectedGroup).
      success(function(data) {
        $scope.fetchGroups();
        $.notify("Group saved", "success");
      }).
      error(function(data) {
        $scope.fetchGroups();
        $.notify("Save failed", "error");
      });
  };

  $scope.close = function() {
    $scope.selectedGroup = undefined;
  };

  $scope.fetchGroups();
};
