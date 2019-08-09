app.controller('caseHistoryController', [
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
function($http, $scope, $filter, $window, $location, $cookies,$rootScope, userService, SpinnerService,$mdSidenav,AppService) {
		$scope.searchResults = [];
		$scope.filter = AppService.getCaseFilter();
		if($scope.filter === undefined){
		$scope.filter = {};	
		$scope.filter['bookingDate1'] = $filter('date')(new Date(new Date().setDate(1)), 'dd-MM-yyyy');
		$scope.filter['bookingDate2'] = $filter('date')(new Date(), 'dd-MM-yyyy');
		}
		$scope.bookingDate1 = new Date(new Date().setDate(1));
		$scope.bookingDate2 = new Date();
		$scope.today = new Date();
		$scope.minDate = new Date(2019, 01, 01);
		$scope.selected = [];
		
		$scope.isChecked = function() {
		    return (($scope.selected.length === $scope.searchResults.length) && ($scope.searchResults.length > 0)) ;
		};
		
		$scope.toggleAll = function() {
		    if ($scope.selected.length === $scope.searchResults.length) {
		      $scope.selected = [];
		    } else if ($scope.selected.length === 0 || $scope.selected.length > 0) {
		      $scope.selected = $scope.searchResults.slice(0);
		    }
		};
		
		$scope.exists = function (item, list) {
		    return list.indexOf(item) > -1;
		};
		
		$scope.toggle = function (item, list) {
		    var idx = list.indexOf(item);
		    if (idx > -1) {
		      list.splice(idx, 1);
		    }
		    else {
		      list.push(item);
		    }
		  };
	  
		$scope.getCaseHistory = function(){
		var modal = SpinnerService.startSpinner();	
		
		var url = weburl + "/get/cases"+$scope.getFilterString();
			return $http({
				url : url,
				method : 'GET'
			}).then(function(data) {
				$scope.searchResults = data.data;
				$scope.selected = [];
				SpinnerService.endSpinner(modal);
			});
		}
		
		$scope.getLateCases = function(){
			var modal = SpinnerService.startSpinner();	
			var url = weburl + "/get/lateCase";
				return $http({
					url : url,
					method : 'GET'
				}).then(function(data) {
					$scope.searchResults = data.data.data;
					$scope.selected = [];
					SpinnerService.endSpinner(modal);
				});
			}
		
		$scope.getPerson = function(searchStr, type) {
			if (!searchStr) {
				var searchStrEncoded = "";
			} else {
				var searchStrEncoded = escape(searchStr);
			}
			var url = weburl + "/person/" + searchStrEncoded + "/" + type;
			return $http({
				url : url,
				method : 'GET'
			}).then(function(data) {
				return data.data;
			});
		};
		
		$scope.getFilterString = function(){
			var filterString = "?";
			angular.forEach($scope.filter,function(value,key){
				filterString = filterString + key + "=" +value + "&";
			});
			return filterString;
		}
		
		$scope.openFilterNav = function() {
		    $mdSidenav('left').toggle();
		};
		
		$scope.onDateChange = function(key,date){
			
			$scope.filter[key] = $filter('date')(date, 'dd-MM-yyyy');	
			$scope.getCaseHistory();
		}
		
		$scope.applyFilter = function() {
			$scope.getCaseHistory();
		};
		
		$scope.editCase = function(opdNo) {
			var modal = SpinnerService.startSpinner();	
			$http.get(weburl + "/case/"+opdNo).success(
					function(data, status) {
						if(data.success){
							AppService.setCaseData(data.data);
							AppService.setCaseFilter($scope.filter);
							$location.path('/case');
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
		
		$scope.changestatus = function() {
			 var cases = {};
			 $scope.selected.forEach(function (item, index) {
				 cases[item.id] = item.action;
			 });
			 
			 var modal = SpinnerService.startSpinner();
				$http.post(weburl + "/proceed/cases", cases).success(
						function(data, status) {
							if(data.success){
							$scope.addAlert('success', data.msg[0]);
							angular.forEach($scope.selected,function(value){
								var idx = $scope.searchResults.indexOf(value);
								$scope.searchResults.splice(idx, 1);
							});
							}
							else{
								angular.forEach($scope.selected,function(value){
									if(data.data[value.id] === undefined){
									    var idx = $scope.searchResults.indexOf(value);
									    $scope.searchResults.splice(idx, 1);
									}
								});
								$scope.addAlert('warning', data.msg[0]);
							}
							SpinnerService.endSpinner(modal);
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
				});
			 
		};
		
		if($location.path() === '/lateCases'){
		$scope.getLateCases();
		}
		else{
		$scope.getCaseHistory();
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