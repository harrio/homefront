var gulp = require('gulp');
var browserify = require('gulp-browserify');
var rename = require('gulp-rename');
var jade = require('gulp-jade');

gulp.task('templates', function() {
  gulp.src('./templates/*.jade')
    .pipe(jade())
    .pipe(gulp.dest('./resources/public/'));
});
 
var paths = {
  scripts: ['src/js/**/*.js', 'templates/*']
};
 
var brConfig = {
	shim: {
    angular: {
      path: './resources/public/bower_components/angular/angular.js',
      exports: 'angular'
    },
    ngRoute: {
      path: './resources/public/bower_components/angular-route/angular-route.js',
      exports: 'ngRoute'
    },
    ngSocket: {
      path: './resources/public/bower_components/angular-socket-io/socket.js',
      exports: 'socketFactory'
    },
    d3: {
      path: './resources/public/bower_components/d3/d3.js',
      exports: 'd3'
    },
    nvd3: {
      path: './resources/public/bower_components/nvd3/nv.d3.js',
      exports: 'nv'
    },
    nvd3ChartDirectives: {
      path: './resources/public/bower_components/angularjs-nvd3-directives/dist/angularjs-nvd3-directives.js',
      exports: 'nvd3ChartDirectives'
    }
  },
  debug: true
};

var browserifier = function() {
  gulp.src('src/js/app.js')
    .pipe(browserify(brConfig))
    .pipe(rename('bundle.js'))
    .pipe(gulp.dest('./resources/public/js/'));
};

gulp.task('scripts', browserifier);

gulp.task('watch', function () {
  gulp.watch(paths.scripts, ['scripts']);
});
 
gulp.task('default', ['scripts', 'templates']);