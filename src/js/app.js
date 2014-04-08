'use strict';
var angular = require('angular');
require('ngRoute');
require('d3');
require('nvd3');
require('nvd3ChartDirectives');

// Declare app level module which depends on filters, and services
var myApp = angular.module('myApp', ['ngRoute', 'nvd3ChartDirectives']);

myApp.controller('tempCtrl', require('./controllers/tempCtrl.js').tempCtrl);
myApp.controller('tempGraphCtrl', require('./controllers/tempGraphCtrl.js').tempGraphCtrl);
myApp.directive('tempDirective', require('./directives/tempDirective.js').tempDirective);
