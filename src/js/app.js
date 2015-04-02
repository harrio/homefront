'use strict';
require('jquery');
var angular = require('angular');
require('ngRoute');
require('d3');
require('nvd3');
require('nvd3ChartDirectives');
require('angularFlot');
require('notify');
require('notifyStyle');
require('uiBootstrap');

// Declare app level module which depends on filters, and services
var myApp = angular.module('myApp', ['ngRoute', 'nvd3ChartDirectives', 'angular-flot', 'ui.bootstrap']);

myApp.controller('dashboardCtrl', require('./controllers/dashboardCtrl.js').dashboardCtrl);
myApp.controller('chartCtrl', require('./controllers/chartCtrl.js').chartCtrl);
myApp.controller('sensorCtrl', require('./controllers/sensorCtrl.js').sensorCtrl);
myApp.controller('groupCtrl', require('./controllers/groupCtrl.js').groupCtrl);
myApp.controller('kissakoneCtrl', require('./controllers/kissakoneCtrl.js').kissakoneCtrl);
myApp.directive('tempDirective', require('./directives/tempDirective.js').tempDirective);
myApp.directive('datetimepicker', require('./directives/datetimeDirective.js').datetimepicker);
myApp.directive('timepicker', require('./directives/datetimeDirective.js').timepicker);

myApp.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'partials/dashboard.html',
        controller: 'dashboardCtrl'
      }).
      when('/charts', {
        templateUrl: 'partials/charts.html',
        controller: 'chartCtrl'
      }).
      when('/sensoradmin', {
        templateUrl: 'partials/sensors.html',
        controller: 'sensorCtrl'
      }).
      when('/groupadmin', {
        templateUrl: 'partials/groups.html',
        controller: 'groupCtrl'
      }).
      when('/kissakoneadmin', {
        templateUrl: 'partials/kissakone.html',
        controller: 'kissakoneCtrl'
      }).
      otherwise({
        redirectTo: '/'
      });
    $locationProvider.html5Mode(true);
  }]);
