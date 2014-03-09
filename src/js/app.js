'use strict';
var angular = require('angular');
require('ngRoute');

// Declare app level module which depends on filters, and services
var myApp = angular.module('myApp', ['ngRoute']);

myApp.controller('tempCtrl', require('./controllers/tempCtrl.js').tempCtrl);
myApp.directive('tempDirective', require('./directives/tempDirective.js').tempDirective);
