/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var app = angular.module('myApp', []);
app.controller('mainCtrl', function($scope, $http, $httpParamSerializerJQLike) {

    $scope.baseUrl = "http://localhost:8080/Homework1-web/webresources/filesystem/";
    $scope.path = "*";
    $scope.pathWithSlash = $scope.path.replace("*", "/");
    $scope.directories = [];
    $scope.formData;
    $scope.files = [];
    $scope.inputs = {};


    /* === DIRECTORIES REST === */

    $scope.createDirectory = function(directoryName) {
        if(directoryName!=null && !$scope.directoryExists(directoryName)) {
            $http.post($scope.baseUrl + "directories", $httpParamSerializerJQLike({path: $scope.path + "*" + directoryName}), {headers:{'Content-Type': 'application/x-www-form-urlencoded'}}).then(function(resp) {
                $scope.getDirectories($scope.path);
                console.log(resp);
            });    
        }
    };
    
    $scope.deleteDirectory = function(directoryName) {
        $http.delete($scope.baseUrl + "directories/" + $scope.path + "*" + directoryName).then(function(resp) {
            $scope.getDirectories($scope.path);
            console.log(resp);
        });
    };
    
    $scope.getDirectories = function(path) {
        $http.get($scope.baseUrl + "directories/" + path).then(function(resp) {
            $scope.getFiles(path);
            $scope.directories = resp.data.directories;
            $scope.path = path;
            $scope.pathWithSlash = $scope.path.replace(/[(*)]/g, "/");
            console.log(resp);
        });
    };
  
  
    /* === FILES REST === */

    $scope.deleteFile = function(fileName) {
        $http.delete($scope.baseUrl + "files/" + ($scope.path=='*' ? $scope.path : ($scope.path + '*')) + fileName).then(function(resp) {
            $scope.getFiles($scope.path);
            console.log(resp);
        });
    };
    
    $scope.getFiles = function(path) {
        $http.get($scope.baseUrl + "files/" + path).then(function(resp) {
            $scope.files = resp.data.files;
            console.log(resp);
        });
    };
    
    $scope.downloadFile = function(fileName) {
        $http.get($scope.baseUrl + "files/download/" + $scope.path + fileName).then(function(resp) {
          console.log(resp);
        });
    };
    
    $scope.updateFile = function(data) {
        $http.post($scope.baseUrl + "files", {file: data.file, destination: data.path}).then(function(resp) {
          console.log(resp);
        });
    };
   
    // Gestione upload e update file
    $scope.getTheFiles = function (files) {
        $scope.formData = new FormData();
        $scope.formData.append("file", files[0]);
    };

    $scope.uploadFiles = function(){
        $scope.formData.append('destination', $scope.path)
        $http.post($scope.baseUrl + "files", $scope.formData, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
        .then(function(){
            $scope.getFiles($scope.path);
            $scope.formData = null;
        });
    }

    // Tasto per tornare indietro
    $scope.back = function(){
        if($scope.path == "*") return; //siamo nella root directory, non possiamo andare indietro
        
        var parts = $scope.path.split('*'); 
        $scope.path = "";
        for (var i = 0; i<parts.length -1; i++){
            $scope.path = $scope.path + parts[i] + "*"; //ricompongo il path senza inserire l'ultimo elemento
        }
        $scope.path = $scope.path == "*" ? "*" : $scope.path.substring(0, $scope.path.length-1);
        $scope.getDirectories($scope.path);
    };
    
    $scope.directoryExists = function(directoryName) {
        for(var i = 0; i<$scope.directories.length; i++) {
            if($scope.directories[i].name == directoryName)
                return true;
        }
        return false;
    }
    
    $scope.getDirectories($scope.path);
});

// direttiva per l'upload del file
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
