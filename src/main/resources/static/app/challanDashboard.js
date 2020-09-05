app.controller('challanDashboardController', [
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
		$scope.createDate1 = new Date(new Date().setDate(new Date().getDate()-6));
		$scope.createDate2 = new Date();
		$scope.today = new Date();
		$scope.minDate = new Date(2018,00,01);
		$scope.aptMaxDate = new Date(2025,00,31);
		$scope.activeFilter = 1;
		$scope.currentPage = 0;
		$scope.pageSize = 15;
		
		$scope.formatFilterDate = function(resp) {
			if(resp == undefined || resp == ''){
				return null;
			}
			var date = resp.split("-");
			var response = date[1] + "-" + date[0] + "-" + date[2];
			return new Date(response);
		};
				
		$scope.getUser = function(searchStr, type) {
			if (!searchStr) {
				var searchStrEncoded = "";
			} else {
				var searchStrEncoded = escape(searchStr);
			}
			var url = weburl + "/user/" + searchStrEncoded + "/" + type;
			return $http({
				url : url,
				method : 'GET'
			}).then(function(data) {
				return data.data;
			}).catch(function(data) {
				$scope.addAlert('warning', 'Please Try Again !!!');
            });
		};
		
		if($scope.filter === undefined){
			$scope.filter = {};	
			$scope.filter['createdDate1'] = $filter('date')(new Date(new Date().setDate(new Date().getDate()-6)), 'dd-MM-yyyy');
			$scope.filter['createdDate2'] = $filter('date')(new Date(), 'dd-MM-yyyy');
			$scope.filter['isCancelled'] = 'false';
			$scope.filter['type'] ='ALL';
		}
		
		
		$scope.hasPermission = function(permission){
			var roles = userService.get().roles;
			if(permission != "" && roles != undefined){
				return roles.indexOf(permission) != -1;
			}
			return false;
		}
		
		$scope.updatePageNo = function(pageNo){
			$scope.setCurrentPage($scope.currentPage + pageNo);
			$scope.getChallanDashboard();
		}
		
		$scope.setCurrentPage = function(pageNo){
			$scope.currentPage = pageNo;
			$scope.filter['pageNo'] = $scope.currentPage;
			$scope.filter['pageSize'] = $scope.pageSize;
		}
		
		
		$scope.getChallanDashboard = function(){
		var modal = SpinnerService.startSpinner();	
		
		var url = weburl + "/get/challan"+$scope.getFilterString();
		$http.get(url).success(
				function(data, status) {
					if(data.success){
						$scope.searchResults = data.data;
						$scope.selected = [];
					}
					else{
						$scope.addAlert('warning', data.msg[0]);
					}
					SpinnerService.endSpinner(modal);
				}).error(function(data, status) {
			$scope.addAlert('warning', 'Please Try Again !!!');
			SpinnerService.endSpinner(modal);
		});	
		}
		
		
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
		
		$scope.onDateChange = function(key,date){
			
			$scope.filter[key] = $filter('date')(date, 'dd-MM-yyyy');
			$scope.setCurrentPage(0);
			$scope.getChallanDashboard();
		}
		
		$scope.onFilterChange = function(key,value){
			if(value != undefined){
			$scope.filter[key] = value;
			$scope.setCurrentPage(0);
			$scope.getChallanDashboard();
			}
		}
		
		$scope.resetFilter = function(key){
			delete $scope.filter[key];
			if(key === 'createdBy'){
				$scope.createdBy = null;
			}
			$scope.setCurrentPage(0);
			$scope.getChallanDashboard();
		}
		
		$scope.resetCreateDateFilter = function(){
			$scope.createDate1 = null;
			$scope.createDate2 = null;
			delete $scope.filter['createdDate1'];
			delete $scope.filter['createdDate2'];
			$scope.setCurrentPage(0);
			$scope.getChallanDashboard();
		}
		
		$scope.applyFilter = function() {
			$scope.setCurrentPage(0);
			$scope.getChallanDashboard();
		};
		
		$scope.setCurrentPage(0);
		$scope.getChallanDashboard();
		
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