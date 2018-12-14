/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var app = angular.module('myApp', []);
app.controller('mainCtrl', function($scope, $http, $httpParamSerializerJQLike) {

    $scope.baseUrl = "http://localhost:8080/Homework1-web/webresources/filesystem/";
    $scope.path = "*";
    $scope.previousPath = "";
    $scope.pathWithSlash = $scope.path.replace("*", "/");
    $scope.directories = [];
    $scope.files = [];
    $scope.inputs = {};


    /* === DIRECTORIES REST === */

    $scope.createDirectory = function(directoryName) {
        $http.post($scope.baseUrl + "directories", $httpParamSerializerJQLike({path: $scope.path + "*" + directoryName}), {headers:{'Content-Type': 'application/x-www-form-urlencoded'}}).then(function(resp) {
          $scope.getDirectories($scope.path);
          console.log(resp);
      });
    };
    
    $scope.deleteDirectory = function(directoryName) {
        $http.delete($scope.baseUrl + "directories/" + $scope.path + directoryName).then(function(resp) {
            $scope.getDirectories($scope.path);
            console.log(resp);
      });
    };
    
    $scope.getDirectories = function(path) {
        $http.get($scope.baseUrl + "directories/" + path).then(function(resp) {
            $scope.directories = resp.data.directories;
            $scope.previousPath = $scope.path;
            $scope.path = path;
            $scope.pathWithSlash = $scope.path.replace(/[(*)]/g, "/");
            console.log(resp);
      });
    };
  
  
    /* === FILES REST === */

    $scope.deleteFile = function(data) {
        $http.delete($scope.baseUrl + "files/" + data).then(function(resp) {
          console.log(resp);
      });
    };
    
    $scope.getFiles = function(path) {
        $http.get($scope.baseUrl + "files/" + path).then(function(resp) {
            $scope.files = resp.data.files;
            console.log(resp);
      });
    };
    
    $scope.downloadFile = function(data) {
        $http.get($scope.baseUrl + "files/download/" + data).then(function(resp) {
          console.log(resp);
      });
    };
    
    $scope.updateFile = function(data) {
        $http.post($scope.baseUrl + "files", {file: data.file, destination: data.path}).then(function(resp) {
          console.log(resp);
      });
    };
   
    $scope.createFile = function(data) {
        $http.put($scope.baseUrl + "files", {file: data.file, destination: data.path}).then(function(resp) {
          console.log(resp);
      });
    };

    $scope.getDirectories($scope.path);
    $scope.getFiles($scope.path);








    var formdata = new FormData();
    $scope.getTheFiles = function (files) {
        formdata = new FormData();
        formdata.append("file", files[0]);
    };

    // NOW UPLOAD THE FILES.
    $scope.uploadFiles = function () {
        formdata.append("destination", $scope.path);
        $http.post($scope.baseUrl + "files", formdata, {headers:{'Content-Type': 'multipart/form-data'}}).then(function(resp) {
          console.log(resp);
        });
    };
});

app.directive('ngFiles', ['$parse', function ($parse) {
    function fn_link(scope, element, attrs) {
        var onChange = $parse(attrs.ngFiles);
        element.on('change', function (event) {
            onChange(scope, { $files: event.target.files });
        });
    };

    return {
        link: fn_link
    };
} ]);
