var gulp = require('gulp');
var browserify = require('gulp-browserify');
var rename = require('gulp-rename');
var jade = require('gulp-jade');
var minifyCSS = require('gulp-minify-css');
var less = require('gulp-less');
var path = require('path');

var paths = {
  scripts: ['src/js/**/*.js', 'templates/*'],
  css: ['bower_components/nvd3/nv.d3.css', 'bower_components/bootstrap/dist/css/bootstrap.css', 'src/css/*.css'],
  fonts: ['fonts/*'],
  nonBundleJs: ['bower_components/bootstrap/dist/js/bootstrap.min.js',
  'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
  'bower_components/flot.tooltip/js/jquery.flot.tooltip.js',
  'lib/js/**/*.js'],
  less: ['src/less/homefront.less']
};

gulp.task('templates', function() {
  gulp.src('./templates/**/*.jade')
    .pipe(jade())
    .pipe(gulp.dest('./resources/public/'));
});

gulp.task('minify-css', function() {

  gulp.src(paths.css)
    .pipe(minifyCSS())
    .pipe(gulp.dest('./resources/public/css/'));
});

gulp.task('less', function () {
  gulp.src(paths.less)
    .pipe(less({
      paths: [ path.join(__dirname, 'less', 'includes') ]
    }))
    .pipe(gulp.dest('./resources/public/css/'));
});

gulp.task('fonts', function() {

  gulp.src(paths.fonts)
    .pipe(gulp.dest('./resources/public/fonts/'));
});

gulp.task('non-bundle-js', function() {

  gulp.src(paths.nonBundleJs)
    .pipe(gulp.dest('./resources/public/js/'));
});

var brConfig = {
	shim: {
    jquery: {
      path: './bower_components/jquery/dist/jquery.js',
      exports: '$'
    },
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
    },
    d3: {
      path: './bower_components/d3/d3.js',
      exports: 'd3'
    },
    nvd3: {
      path: './bower_components/nvd3/nv.d3.js',
      exports: 'nv'
    },
    nvd3ChartDirectives: {
      path: './bower_components/angularjs-nvd3-directives/dist/angularjs-nvd3-directives.js',
      exports: 'nvd3ChartDirectives'
    },
    angularFlot: {
      path: './bower_components/angular-flot/angular-flot.js',
      exports: 'angularFlot'
    },
    notify: {
      path: './bower_components/notify/notify.js',
      exports: 'notify'
    },
    notifyStyle: {
      path: './bower_components/notify/styles/bootstrap/notify-bootstrap.js',
      exports: 'notifyStyle'
    },
    uiBootstrap: {
      path: 'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
      exports: 'uiBootstrap'
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

gulp.task('default', ['scripts', 'non-bundle-js', 'templates', 'less', 'minify-css', 'fonts']);
