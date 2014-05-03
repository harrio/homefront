'use strict';
require('jquery');
var angular = require('angular');
require('ngRoute');
require('d3');
require('nvd3');
require('nvd3ChartDirectives');
require('angularFlot');

// Declare app level module which depends on filters, and services
var myApp = angular.module('myApp', ['ngRoute', 'nvd3ChartDirectives', 'angular-flot']);

myApp.controller('dashboardCtrl', require('./controllers/dashboardCtrl.js').dashboardCtrl);
myApp.controller('sensorCtrl', require('./controllers/sensorCtrl.js').sensorCtrl);
myApp.controller('tempGraphCtrl', require('./controllers/tempGraphCtrl.js').tempGraphCtrl);
myApp.directive('tempDirective', require('./directives/tempDirective.js').tempDirective);

myApp.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider.
      when('/', {
        templateUrl: 'partials/dashboard.html',
        controller: 'dashboardCtrl'
      }).
      when('/sensors', {
        templateUrl: 'partials/sensors.html',
        controller: 'sensorCtrl'
      }).
      otherwise({
        redirectTo: '/'
      });
    $locationProvider.html5Mode(true);
  }]);