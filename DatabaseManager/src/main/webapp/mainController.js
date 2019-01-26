/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var app = angular.module('myApp', []);
app.controller('mainCtrl', function($scope, $http, $httpParamSerializerJQLike) {

    $scope.replicasNumber = 0;
    $scope.queryOutput = null;
    $scope.inputs = {doc: {}};
    $scope.bLoading = false;
    $scope.baseUrl = "http://localhost:43637/DatabaseManager-1.0-SNAPSHOT/webresources/mongodb/";


    /* === COLLECTION REST === */

    $scope.dropCollections = function() {
        $scope.bLoading = true;
        $http.delete($scope.baseUrl + "collections/drop").then(function(resp) {
            document.getElementById("output").innerHTML = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
            $scope.queryOutput = resp.data;
            console.log(resp);
        }).finally (function(){
            $scope.bLoading = false;
        });            
    };
    
    $scope.insertDocument = function(collectionName, directory, cycle, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state) {
        if(collectionName != null) {
            $scope.bLoading = true;
            $http.post($scope.baseUrl + "collections/write", $httpParamSerializerJQLike({collection_name: collectionName, directory: directory, cycle: cycle, mean_add: meanAdd, mean_download: meanDownload, stddev_add: stdDevAdd, stddev_download: stdDevDownload, state: state, timestamp: moment().format("YYYY/MM/DD - HH:mm:ss")}), {headers:{'Content-Type': 'application/x-www-form-urlencoded'}}).then(function(resp) {
                document.getElementById("output").innerHTML = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
                $scope.queryOutput = resp.data;
                console.log(resp);
            }).finally (function(){
                $scope.bLoading = false;
            }); 
        }
    };
    
    $scope.readCollection = function(collectionName) {
        if(collectionName != null) {
            $scope.bLoading = true;
            $http.get($scope.baseUrl + "collections/" + collectionName).then(function(resp) {
                document.getElementById("output").innerHTML = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
                $scope.queryOutput = resp.data;
                console.log(resp);
            }).finally (function(){
                $scope.bLoading = false;
            });            
        }
    };
    
    $scope.readLastCommittedDocumentFromCollection = function(collectionName) {
        if(collectionName != null) {
            $scope.bLoading = true;
            $http.get($scope.baseUrl + "collections/" + collectionName + "/lastCommittedDocument").then(function(resp) {
                document.getElementById("output").innerHTML = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
                $scope.queryOutput = resp.data;
                console.log(resp);
            }).finally (function(){
                $scope.bLoading = false;
            }); 
        }
    };
	
	$scope.getReplicasNumber = function() {
        $scope.bLoading = true;
        $http.get($scope.baseUrl + "replicas").then(function(resp) {
            $scope.replicasNumber = resp.data.replicas;
            console.log(resp);
        }).finally (function(){
            $scope.bLoading = false;
        });            
    };

    /* Others */
    function syntaxHighlight(json) {
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            var cls = 'number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'key';
                } else {
                    cls = 'string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'boolean';
            } else if (/null/.test(match)) {
                cls = 'null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    }
	
	$scope.getReplicasNumber();
    
});