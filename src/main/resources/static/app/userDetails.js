app.controller('userDetailsController', [
		'$http',
		'$scope',
		'$filter',
		'$window',
		'$location',
		'$cookies',
		'$rootScope',
		'userService',
		'SpinnerService',
		'$mdSidenav',
		'AppService',
		'$stateParams',
function($http, $scope, $filter, $window, $location, $cookies,$rootScope, userService, SpinnerService,$mdSidenav,AppService,$stateParams) {
		$scope.searchResults = [];
		$scope.activeFilter = 1;
		$scope.currentPage = 0;
		$scope.pageSize = 10;
		
		$scope.setCurrentPage = function(pageNo){
			$scope.currentPage = pageNo;
			$scope.filter['pageNo'] = $scope.currentPage;
			$scope.filter['pageSize'] = $scope.pageSize;
		}
		
		$scope.updatePageNo = function(pageNo){
			$scope.setCurrentPage($scope.currentPage + pageNo);
			$scope.getUserHistory();
		}
						
		if($scope.filter === undefined){
			$scope.filter = {};	
			$scope.filter['type'] ='ALL';
		}
		
		
		$scope.hasPermission = function(permission){
			var roles = userService.get().roles;
			if(permission != "" && roles != undefined){
				return roles.indexOf(permission) != -1;
			}
			return false;
		}
		
		$scope.addUser = function(){
			$location.path('/user');
		}
		
		$scope.getUserHistory = function(){
		var modal = SpinnerService.startSpinner();	
		var url = weburl + "/get/user"+$scope.getFilterString();
			return $http({
				url : url,
				method : 'GET'
			}).then(function(data) {
				$scope.searchResults = data.data;
				SpinnerService.endSpinner(modal);
			}).catch(function(data) {
				SpinnerService.endSpinner(modal);
				$scope.addAlert('warning', 'Please Try Again !!!');
            });
		}
		
		$scope.updateUser = function(user) {
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/update/user", user).success(
					function(data, status) {
						if(data.success){
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
		
		$scope.getFilterString = function(){
			var filterString = "?";
			angular.forEach($scope.filter,function(value,key){
				filterString = filterString + key + "=" +value + "&";
			});
			return filterString;
		}
		
		$scope.openFilterNav = function(filterNo) {
			$scope.activeFilter = filterNo;
		    $mdSidenav('left').toggle();
		};
		
		
		$scope.onFilterChange = function(key,value){
			if(value != undefined){
			$scope.filter[key] = value;	
			$scope.setCurrentPage(0);
			$scope.getUserHistory();
			}
		}
		
		$scope.resetFilter = function(key){
			delete $scope.filter[key];
			$scope.setCurrentPage(0);
			$scope.getUserHistory();
		}
		
		$scope.applyFilter = function() {
			$scope.setCurrentPage(0);
			$scope.getUserHistory();
		};
		
		
		$scope.setCurrentPage(0);
		$scope.getUserHistory()
		
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