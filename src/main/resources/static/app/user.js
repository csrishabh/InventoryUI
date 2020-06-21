app.controller('userController', [
		'$http',
		'$scope',
		'$filter',
		'$window',
		'$location',
		'$cookies',
		'$rootScope',
		'userService',
		'SpinnerService',
		'$mdDialog',
		'AppService',
		'$stateParams',
		function($http, $scope, $filter, $window, $location, $cookies,
				$rootScope, userService, SpinnerService,$mdDialog,AppService,$stateParams) {
			
			$scope.isUserEdit = false;
			$scope.user = {};
			
			$scope.hasPermission = function(permission){
				var roles = userService.get().roles;
				if(permission != "" && roles != undefined){
					return roles.indexOf(permission) != -1;
				}
				return false;
			}

			
			$scope.getUser = function(userId){
				var modal = SpinnerService.startSpinner();	
				$http.get(weburl + "/get/user?username="+userId).success(
						function(data, status) {
							$scope.user = data[0];
							SpinnerService.endSpinner(modal);
							$scope.isUserEdit = true;
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
					$location.path('/consignment');
				});
			}
			
			
			$scope.saveUser = function(user) {
				var modal = SpinnerService.startSpinner();
				$http.post(weburl + "/create/user", user).success(
						function(data, status) {
							if(data.success){
							$scope.initilizeNewUser();
							$scope.addAlert('success', data.msg[0]);
							}
							else{
								$scope.addAlert('warning', data.msg[0]);
							}
							SpinnerService.endSpinner(modal);
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
				});
			};
			
			$scope.updateUser = function(user) {
				var modal = SpinnerService.startSpinner();
				$http.post(weburl + "/update/user", user).success(
						function(data, status) {
							if(data.success){
							$scope.initilizeNewUser();
							$scope.isUserEdit = false;
							$scope.addAlert('success', data.msg[0]);
							}
							else{
								$scope.addAlert('warning', data.msg[0]);
							}
							SpinnerService.endSpinner(modal);
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
				});
			};
			
		
			$scope.initilizeNewUser = function() {
				$scope.user= {};
			};
			
			
			if($stateParams.userId != undefined){
				$scope.getUser($stateParams.userId)
			}
			else{
				$scope.initilizeNewUser()
			}
			
			$scope.alerts = [];

			$scope.addAlert = function(type, messege) {
				$scope.alerts.push({
					type : type,
					msg : messege
				});
			};

			$scope.closeAlert = function(index) {
				$scope.alerts.splice(index, 1);
			};

		} ]);