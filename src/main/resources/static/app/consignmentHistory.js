app.controller('consignmentHistoryController', [
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
		$scope.manifestDetails=[];
		$scope.bookingDate1 = new Date(new Date().setDate(new Date().getDate()-6));
		$scope.bookingDate2 = new Date();
		$scope.today = new Date();
		$scope.minDate = new Date(2018,00,01);
		$scope.aptMaxDate = new Date(2025,00,31);
		$scope.selected = [];
		$scope.consignor = null;
		$scope.consignee = null;
		$scope.activeFilter = 1;
		$scope.manifest = {};
		$scope.unitMapping = [];
		$scope.price = 0;
		$scope.company = null;
		$scope.destination = null;
		
		$scope.formatFilterDate = function(resp) {
			if(resp == undefined || resp == ''){
				return null;
			}
			var date = resp.split("-");
			var response = date[1] + "-" + date[0] + "-" + date[2];
			return new Date(response);
		};
		
		$scope.openManifestDetailModel = function(){
			$('#manifestDetailModel').modal('show');
		}
		
		$scope.openManifestModel = function(){
			if($scope.selected.size == 0)
			{
				$scope.addAlert('warning', 'Please Select Consignment !!!');
				return;
			}
			$('#createManifestModel').modal('show');
		}
		
		$scope.closeManifestModel = function(){
			$('#createManifestModel').modal('hide');
			$scope.initlizeManifest();
		}
		
		$scope.initlizeManifest = function(){
			$scope.manifest = {};
			$scope.unitMapping = [];
			$scope.price = 0;
			$scope.company = null;
			$scope.destination = null;
		}
		
		$scope.createManifest = function(){
			$scope.manifest.consignments=[];
			$scope.selected.forEach(function (consignment) {
				$scope.manifest.consignments.push(consignment.biltyNo); 
			});
			
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/add/manifest", $scope.manifest).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$scope.closeManifestModel();
						$scope.getConsignmentHistory();
						$scope.selected= [];
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
		
		$scope.deletedConsignment = function(biltyNo){
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/deleted/consignment", biltyNo).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$scope.getConsignmentHistory();
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
		
		$scope.updateCompany = function(company){
			if(company){
				$scope.manifest.company = company.username;
				$scope.getUnitMapping(company.id);
			}
		}
		
		$scope.updateDestination = function(city){
			if(city){
				$scope.manifest.des = city.value;
			}
		}
		
		$scope.updateUnit = function (unit){
			$scope.unitMapping.forEach(function (mapping) {
				   if(mapping.unit === unit){
					   $scope.manifest.unitComMappingVer = mapping.version;
					   $scope.price = mapping.price/100;
				   }
			});
		}
		
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
		
		$scope.getCommonData = function(searchStr, type) {
			if (!searchStr) {
				var searchStrEncoded = "";
			} else {
				var searchStrEncoded = escape(searchStr);
			}
			var url = weburl + "/commmonData/" + type + "/" + searchStrEncoded;
			return $http({
				url : url,
				method : 'GET'
			}).then(function(data) {
				return data.data;
			}).catch(function(data) {
				$scope.addAlert('warning', 'Please Try Again !!!');
            });
		};
		
		$scope.getUnitMapping = function(companyId) {
			if(companyId){
			//var modal = SpinnerService.startSpinner();	
			$http.get(weburl + "/unit/"+companyId).success(
					function(data, status) {
						if(data.success){
							$scope.unitMapping = data.data;
						}
						else{
							$scope.addAlert('warning', data.msg[0]);
						}
						//SpinnerService.endSpinner(modal);
					}).error(function(data, status) {
				$scope.addAlert('warning', 'Please Try Again !!!');
				//SpinnerService.endSpinner(modal);
			});
			}
		};
		
		$scope.isUnitMappingPresent = function(type) {
			var isFound = false;
			$scope.unitMapping.forEach(function (mapping) {
				   if(mapping.unit === type){
					   isFound = true;
				   }
			});
			return isFound;
		};
		
		if($scope.filter === undefined){
			$scope.filter = {};	
			$scope.filter['bookingDate1'] = $filter('date')(new Date(new Date().setDate(new Date().getDate()-6)), 'dd-MM-yyyy');
			$scope.filter['bookingDate2'] = $filter('date')(new Date(), 'dd-MM-yyyy');
			$scope.filter['status'] = 'false';
			$scope.filter['type'] ='false';
		}
		
		
		$scope.hasPermission = function(permission){
			var roles = userService.get().roles;
			if(permission != "" && roles != undefined){
				return roles.indexOf(permission) != -1;
			}
			return false;
		}
		
		
		$scope.isChecked = function() {
		    return (($scope.selected.length === $scope.searchResults.length) && ($scope.searchResults.length > 0)) ;
		};
		
		$scope.toggleAll = function() {
			var items = [];
	    	$scope.searchResults.forEach(function (item) {
				   if(!item.isDeliverd && !item.isDeleted){
					   items.push(item);
				   }
			});
		    if ($scope.selected.length === items.length) {
		      $scope.selected = [];
		    } else if ($scope.selected.length === 0 || $scope.selected.length > 0) {
		      $scope.selected = items.slice(0);
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
	  
		$scope.getConsignmentHistory = function(){
		var modal = SpinnerService.startSpinner();	
		
		var url = weburl + "/get/consignments"+$scope.getFilterString();
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
			$scope.getConsignmentHistory();
		}
		
		$scope.onFilterChange = function(key,value){
			if(value != undefined){
			$scope.filter[key] = value;	
			$scope.getConsignmentHistory();
			}
		}
		
		$scope.resetFilter = function(key){
			delete $scope.filter[key];
			
			if(key === 'consignor'){
				$scope.consignor = null;
			}
			else if(key === 'consignee'){
				$scope.consignee = null;
			}
			$scope.getConsignmentHistory();
		}
		
		$scope.resetBookingDateFilter = function(){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			$scope.getConsignmentHistory();
		}
		
		$scope.applyFilter = function() {
			$scope.getConsignmentHistory();
		};
		
		
		$scope.getManifestDetails = function(manifests){
			var modal = SpinnerService.startSpinner();	
			var url = weburl + "/get/manifest?refId="+manifests.toString();
				return $http({
					url : url,
					method : 'GET'
				}).then(function(data) {
					$scope.manifestDetails = data.data;
					SpinnerService.endSpinner(modal);
					$scope.openManifestDetailModel();
				}).catch(function(data) {
					SpinnerService.endSpinner(modal);
					$scope.addAlert('warning', 'Please Try Again !!!');
	            });
		}
		
		
		if($stateParams.searchTxt){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			$scope.filter['biltyNo'] = $stateParams.searchTxt
			$scope.getConsignmentHistory();	
		}
		else if($stateParams.view === 'todayCases'){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			var todayDate = new Date();
			$scope.aptDate1 = todayDate;
			$scope.aptDate2 = todayDate;
			$scope.filter['aptDate1'] = $filter('date')(todayDate, 'dd-MM-yyyy');
			$scope.filter['aptDate2'] = $scope.filter['aptDate1'];
			$scope.getConsignmentHistory();
		}
		else{
		$scope.getConsignmentHistory();
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