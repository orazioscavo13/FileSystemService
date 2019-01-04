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
    $scope.baseUrl = "http://localhost:43637/DatabaseManager-1.0-SNAPSHOT/webresources/mongodb/";


    /* === COLLECTION REST === */
    $scope.readCollection = function(collectionName) {
        if(collectionName != null) {
            $http.get($scope.baseUrl + "collections/" + collectionName).then(function(resp) {
                $scope.queryOutput = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
                console.log(resp);
            });            
        }
    };
    
    $scope.readLastCommittedDocumentFromCollection = function(collectionName) {
        
        if(collectionName != null) {
            $http.get($scope.baseUrl + "collections/" + collectionName + "/lastCommittedDocument").then(function(resp) {
                $scope.queryOutput = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
                console.log(resp);
            });            
        }
    };

    $scope.insertDocument = function(collectionName, directory, cycle, meanAdd, meanDownload, stdDevAdd, stdDevDownload, state) {
        if(collectionName != null) {
            $http.post($scope.baseUrl + "collections/write", $httpParamSerializerJQLike({collection_name: collectionName, directory: directory, cycle: cycle, mean_add: meanAdd, mean_download: meanDownload, stddev_add: stdDevAdd, stddev_download: stdDevDownload, state: state, timestamp: moment().format("yyyy/MM/dd - HH:mm:ss")}), {headers:{'Content-Type': 'application/x-www-form-urlencoded'}}).then(function(resp) {
                $scope.queryOutput = syntaxHighlight(JSON.stringify(resp.data, undefined, 4));
                console.log(resp);
            });    
        }
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
    
});