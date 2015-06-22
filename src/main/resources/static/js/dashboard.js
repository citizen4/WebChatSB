var dashboardApp = angular.module('dashboardApp', ['angular-hal']);

dashboardApp.controller('DashboardCtrl', function ($log, $timeout, $scope) {

   $scope.footerMsg = {msg: null, success: null, error: null, promise: null};

   $scope.showFooterMsg = function (m) {
      $scope.footerMsg.msg = m.msg || null;
      $scope.footerMsg.success = m.success || null;
      $scope.footerMsg.error = m.error || null;

      if ($scope.footerMsg.promise) {
         $timeout.cancel($scope.footerMsg.promise);
         $scope.footerMsg.promise = null;
      }

      if (m.msg) {
         return;
      }

      $scope.footerMsg.promise = $timeout(function () {
         $scope.footerMsg = {msg: null, success: null, error: null, promise: null};
      }, $scope.footerMsg.error ? 6000 : 2000, true);
   }

});

dashboardApp.controller('AccountCtrl', function ($log, $window, $filter, $timeout, $scope, halClient) {

   var emptyAccount = {username: null, firstName: null, lastName: null, email: null, password: null, roles: null};
   var edited = {changed:false, row:null, value:null, promise:null};

   function showErrorMsg(action,error) {
      var errMsg = action + ": FAILED! " + error.status + " " + error.statusText;
      if (error.data && error.data.errors) {
         errMsg += "\n" + JSON.stringify(error.data.errors);
      }
      console.error(errMsg);
      $scope.showFooterMsg({error: errMsg});
   }

   function accountPage(pageNumber) {
      var action = "Getting page "+pageNumber;

      if (!!edited.promise) {
         $timeout.cancel(edited.promise);
         edited.promise = null;
      }

      $scope.serviceResource.$get('accounts', {'page': pageNumber, 'size': 5, sort: 'created'})
          .then(function (resource) {
             $scope.page = resource.page;
             return resource.$get('accounts');
          }, function(error) {
             showErrorMsg(action,error);
          })
          .then(function (resource) {
             $scope.accountResource = resource;
             $scope.accounts = angular.copy(resource);
          });
   }

   $scope.editorHandler = function (event, row) {
      var elem = event.currentTarget;
      var type = event.type;

      if (type == "focus") {
         if (!!edited.promise) {
            $timeout.cancel(edited.promise);
            edited.promise = null;
         }

         if (row != edited.row && edited.changed) {
            edited.changed = false;
            $scope.updateAccount(edited.row,$scope.accounts[edited.row]);
         }

         edited.value = elem.value;
         edited.row = row;
         return;
      }

      if (type == "blur") {
         edited.changed = edited.changed || elem.value !== edited.value;
         if (edited.changed) {
            edited.promise = $timeout(function () {
               edited.changed = false;
               $scope.updateAccount(row,$scope.accounts[row]);
            }, 333, true);
         }
      }
   };

   // PUT/PATCH Bug in Spring Data Rest using Hibernate: DATAREST-441
   $scope.updateAccount = function (row, data) {
      var action = "Account update";
      $scope.showFooterMsg({msg:action+"..."});
      $scope.accountResource[row].$patch('self', {}, data)
          .then(function (resource) {
             $scope.showFooterMsg({success:action+": SUCCESSFUL!"});
          }, function (error) {
             showErrorMsg(action,error);
          });
   };

   $scope.createAccount = function (account) {
      var action = "Account creation";
      account.roles = "USER";
      $scope.showFooterMsg({msg: action});
      $scope.serviceResource.$post('accounts', {}, account)
          .then(function (resource) {
             $scope.account = angular.copy(emptyAccount);
             $scope.showFooterMsg({success:action+": SUCCESSFUL!"});
          }, function (error) {
             showErrorMsg(action,error);
          });
   };

   halClient.$get('/service').then(function (resource) {
      $scope.serviceResource = resource;
      accountPage(0);
   });

   $scope.loadAccountPage = function (pageNumber) {
      accountPage(pageNumber % $scope.page.totalPages);
   };

});
