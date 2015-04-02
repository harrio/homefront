exports.datetimepicker = function ($parse) {
    return function(scope, element, attrs) {
      var ngModel = $parse(attrs.ngModel);
      $(function() {
        element.datetimepicker({
          parse: 'loose',
          dateFormat: 'd.M.yy',
          timeFormat: 'd.M.yy H:mm',
          onSelect: function(dateText) {
            scope.$apply(function(scope){
              ngModel.assign(scope, dateText);
            });
          }
        });
      });
    };
    }

exports.timepicker = function ($parse) {
    return function(scope, element, attrs) {
      var ngModel = $parse(attrs.ngModel);
      $(function() {
        element.timePicker({
          show24Hours: true,
          separator:':'
        });
        element.change(function() {
            scope.$apply(function(scope){
              var time = $.timePicker(element).getTime();
              ngModel.assign(scope, element.val());
            });
        });
      });
    };
    }
