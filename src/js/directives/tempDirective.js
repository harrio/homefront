exports.tempDirective = function() {
  return {
    restrict: 'EA',
    scope: {
      data: '=' // bi-directional data-binding
    },
      // directive code
    link: function(scope, element, attrs) {
      
    }
  };
};