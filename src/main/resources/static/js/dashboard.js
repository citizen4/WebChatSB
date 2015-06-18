var dashboardApp = angular.module('dashboardApp', ['angular-hal']);

dashboardApp.controller('AccountCtrl', function ($log, $window, $scope, halClient) {

   function showErrorMsg(error) {
      var errMsg = error.status + " " + error.statusText;
      if (error.data && error.data.errors) {
         errMsg += "\n" + JSON.stringify(error.data.errors, undefined, 2);
      }
      console.log(errMsg);
      $window.alert(errMsg);
   }

   function accountPage(pageNumber) {
      $scope.serviceResource.$get('accounts', {'page': pageNumber, 'size': 5, sort: null})
          .then(function (resource) {
             $scope.page = resource.page;
             return resource.$get('accounts');
          })
          .then(function (accounts) {
             $scope.accounts = accounts;
          });
   }

   // PATCH Bug in Spring Data Rest: DATAREST-441
   $scope.updateAccount = function (account, data) {
      account.$put('self', {}, data)
          .then(function (resource) {
             $log.info("updateAccount: " + resource);
          }, function (error) {
             $log.error("updateAccount: " + error);
          });
   };

   $scope.createAccount = function (account) {

      account.roles = "USER";

      $scope.serviceResource.$post('accounts', {}, account)
          .then(function (resource) {
             $log.info("createAccount: " + resource);
             $scope.account = {username:null,firstName:null,lastName:null,email:null,password:null,roles:null};
             accountPage($scope.page.totalPages - 1);
          }, function (error) {
             showErrorMsg(error);
          });
   };

   halClient.$get('/service').then(function (resource) {
      $scope.serviceResource = resource;
      $scope.account = {username:null,firstName:null,lastName:null,email:null,password:null,roles:null};
      accountPage(0);
   });

   $scope.loadAccountPage = function (pageNumber) {
      accountPage(pageNumber % $scope.page.totalPages);
   };

});
