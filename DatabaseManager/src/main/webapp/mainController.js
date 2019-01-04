/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var app = angular.module('myApp', []);
app.controller('mainCtrl', function($scope, $http, $httpParamSerializerJQLike) {

    $scope.replicaNumber = 5;
    $scope.queryOutput = null;
    $scope.inputs = {doc: {}};
    //TODO: Da cambiare per dicoker
    $scope.baseUrl = "http://localhost:8080/DatabaseManager/webresources/mongodb";


    /* === DIRECTORIES REST === */
    $scope.readCollection = function(collectionName) {
        if(collectionName != null) {
            $http.get($scope.baseUrl + "collections/" + collectionName).then(function(resp) {
                $scope.queryOutput = resp.data.documents;
                console.log(resp);
            });            
        }
    };
    
    $scope.readLastCommittedDocumentFromCollection = function(collectionName) {
        
        if(collectionName != null) {
            $http.get($scope.baseUrl + "collections/" + collectionName + "/lastCommittedDocument").then(function(resp) {
                $scope.queryOutput = resp.data.documents;
                console.log(resp);
            });            
        }
    };

    $scope.insertDocument = function(collectionName, directory, cycle, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state) {
        if(collectionName != null) {
            $http.post($scope.baseUrl + "collections/commit", $httpParamSerializerJQLike({collection_name: collectionName, directory: directory, cycle: cycle, mean_add: meanAdd, mean_download: meanDownload, stddev_add: stdDevAdd, stddev_download: stdDevDownload, state: state, timestamp: moment().format("yyyy/MM/dd - HH:mm:ss")}), {headers:{'Content-Type': 'application/x-www-form-urlencoded'}}).then(function(resp) {
                $scope.getDirectories($scope.path);
                console.log(resp);
            });    
        }
    };
    
});
