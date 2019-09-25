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
		'$stateParams',
function($http, $scope, $filter, $window, $location, $cookies,$rootScope, userService, SpinnerService,$mdSidenav,AppService,$stateParams) {
		$scope.searchResults = [];
		$scope.filter = AppService.getCaseFilter();
		
		$scope.bookingDate1 = new Date(new Date().setDate(new Date().getDate()-6));
		$scope.bookingDate2 = new Date();
		$scope.aptDate1 = null;
		$scope.aptDate2 = null;
		$scope.today = new Date();
		$scope.minDate = new Date(2018,00,01);
		$scope.aptMaxDate = new Date(2025,00,31);
		$scope.selected = [];
		$scope.user = null;
		$scope.doctor = null;
		$scope.vendor = null;
		$scope.crown = null;
		$scope.activeFilter = 1;
		$scope.ShowCrownDetails = function(c) {
			$scope.crown = c;
			$('#crownDetails').modal('show');
		}
		
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
			});
		};
		
		
		if($scope.filter === undefined){
			$scope.filter = {};	
			$scope.filter['bookingDate1'] = $filter('date')(new Date(new Date().setDate(new Date().getDate()-6)), 'dd-MM-yyyy');
			$scope.filter['bookingDate2'] = $filter('date')(new Date(), 'dd-MM-yyyy');
			$scope.filter['status'] = "ALL";
			}
			else{
				$scope.bookingDate1 = $scope.formatFilterDate($scope.filter['bookingDate1']);
				$scope.bookingDate2 = $scope.formatFilterDate($scope.filter['bookingDate2']);
				$scope.aptDate1 = $scope.formatFilterDate($scope.filter['aptDate1']);
				$scope.aptDate2 = $scope.formatFilterDate($scope.filter['aptDate2']);
				$scope.dlvDate1 = $scope.formatFilterDate($scope.filter['dlvDate1']);
				$scope.dlvDate2 = $scope.formatFilterDate($scope.filter['dlvDate2']);
			}
		
		
		$scope.hasPermission = function(permission){
			var roles = userService.get().roles;
			if(permission != "" && roles != undefined){
				return roles.indexOf(permission) != -1;
			}
			return false;
		}
		
		$scope.isActive = function (viewLocation) { 
	        return $location.path().includes(viewLocation);
	    }
		
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
			}).catch(function(data) {
				SpinnerService.endSpinner(modal);
				$scope.addAlert('warning', 'Please Try Again !!!');
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
				}).catch(function(data) {
					SpinnerService.endSpinner(modal);
					$scope.addAlert('warning', 'Please Try Again !!!');
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
			}).catch(function(data) {
				$scope.addAlert('warning', 'Please Try Again !!!');
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
		
		$scope.onDateChange = function(key,date){
			
			$scope.filter[key] = $filter('date')(date, 'dd-MM-yyyy');	
			$scope.getCaseHistory();
		}
		
		$scope.onFilterChange = function(key,value){
			if(value != undefined){
			$scope.filter[key] = value;	
			$scope.getCaseHistory();
			}
		}
		
		$scope.resetFilter = function(key){
			delete $scope.filter[key];
			if(key === 'createdBy'){
				$scope.user = null;
			}
			else if(key === 'doctor'){
				$scope.doctor = null;
			}
			else if(key === 'vender'){
				$scope.vendor = null;
			}
			$scope.getCaseHistory();
		}
		
		$scope.resetBookingDateFilter = function(){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			$scope.getCaseHistory();
		}
		$scope.resetAptDateFilter = function(){
			$scope.aptDate1 = null;
			$scope.aptDate2 = null;
			delete $scope.filter['aptDate1'];
			delete $scope.filter['aptDate2'];
			$scope.getCaseHistory();
		}
		$scope.resetDlvDateFilter = function(){
			$scope.dlvDate1 = null;
			$scope.dlvDate2 = null;
			delete $scope.filter['dlvDate1'];
			delete $scope.filter['dlvDate2'];
			$scope.getCaseHistory();
		}
		
		$scope.applyFilter = function() {
			$scope.getCaseHistory();
		};
		
		$scope.editCase = function(opdNo,date) {
			var modal = SpinnerService.startSpinner();	
			$http.get(weburl + "/case/"+opdNo+"/"+date).success(
					function(data, status) {
						if(data.success){
							AppService.setCaseData(data.data);
							AppService.setCaseFilter($scope.filter);
							$location.path('/editCase');
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
		else if($location.path() === '/todayCases'){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			var todayDate = new Date();
			$scope.aptDate1 = todayDate;
			$scope.aptDate2 = todayDate;
			$scope.filter['aptDate1'] = $filter('date')(todayDate, 'dd-MM-yyyy');
			$scope.filter['aptDate2'] = $scope.filter['aptDate1'];
			$scope.getCaseHistory();
		}
		else if($stateParams.searchTxt){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			if($stateParams.searchTxt.match(/\d+/g)== null){
				$scope.filter['patient'] = $stateParams.searchTxt;
			}
			else{
				$scope.filter['opdNo'] = $stateParams.searchTxt
			}
			$scope.getCaseHistory();	
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