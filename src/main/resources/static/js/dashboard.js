var dashboardApp = angular.module('dashboardApp', ['angular-hal']);

dashboardApp.controller('AccountCtrl', function ($scope, halClient) {

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

   halClient.$get('/service').then(function (resource) {
      $scope.serviceResource = resource;
      accountPage(0);
   });

   $scope.loadAccountPage = function (pageNumber) {
      accountPage(pageNumber % $scope.page.totalPages);
   };

});
