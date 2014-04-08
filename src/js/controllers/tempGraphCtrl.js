var angular = require('angular');

exports.tempGraphCtrl = function($scope, $http, $interval) {
   $scope.exampleData = [
    {
                    "key": "Series 1",
                    "values": [ [1 , 0] , [2 , 5] , [3 , 8] , [4 , 9] , [5 , -1] , [6 , 2] , [7 , 2] , [8 , -3] , [9 , -5] , [10 , 9] ]
    },
    {
                    "key": "Series 2",
                    "values": [ [1 , 5] , [2 , 3] , [3 , -8] , [4 , 3] , [5 , -11] , [6 , 3] , [7 , 7] , [8 , -4] , [9 , -8] , [10 , 6] ]
    }];
};