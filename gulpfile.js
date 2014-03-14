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
  scripts: ['src/js/**/*.js']
};
 
var brConfig = {
	shim: {
    angular: {
      path: './bower_components/angular/angular.js',
      exports: 'angular'
    },
    ngRoute: {
      path: './bower_components/angular-route/angular-route.js',
      exports: 'ngRoute'
    },
    ngSocket: {
      path: './bower_components/angular-socket-io/socket.js',
      exports: 'socketFactory'
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
 
gulp.task('default', ['scripts', 'templates', 'watch']);