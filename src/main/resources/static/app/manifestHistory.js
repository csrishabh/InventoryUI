app.controller('manifestHistoryController', [
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
		$scope.consignmentDetails = [];
		$scope.createdDate1 = new Date(new Date().setDate(new Date().getDate()-6));
		$scope.createdDate2 = new Date();
		$scope.today = new Date();
		$scope.minDate = new Date(2018,00,01);
		$scope.company = null;
		$scope.activeFilter = 1;
		$scope.currentPage = 0;
		$scope.pageSize = 15;
		$scope.consignmentId=null;
		
		$scope.formatFilterDate = function(resp) {
			if(resp == undefined || resp == ''){
				return null;
			}
			var date = resp.split("-");
			var response = date[1] + "-" + date[0] + "-" + date[2];
			return new Date(response);
		};
		
		$scope.setCurrentPage = function(pageNo){
			$scope.currentPage = pageNo;
			$scope.filter['pageNo'] = $scope.currentPage;
			$scope.filter['pageSize'] = $scope.pageSize;
		}
		
		$scope.updatePageNo = function(pageNo){
			$scope.setCurrentPage($scope.currentPage + pageNo);
			$scope.getManifestHistory();
		}
		
		$scope.openConsignmentModel = function(){
			$('#consignmentDetailModel').modal('show');
		}
		
		$scope.deleteManifest = function(refId){
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/delete/manifest", refId).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						SpinnerService.endSpinner(modal);
						$scope.getManifestHistory();
						}
						else{
							$scope.addAlert('warning', data.msg[0]);
							SpinnerService.endSpinner(modal);
						}	
					}).error(function(data, status) {
				$scope.addAlert('warning', 'Please Try Again !!!');
				SpinnerService.endSpinner(modal);
			});	
		}
		
		$scope.deleteConsignmentFromManifest = function(idx,refId,biltyNo){
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/delete/manifest/consignment/"+biltyNo, refId).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$scope.consignmentDetails.splice(idx, 1);
						if($scope.consignmentDetails.length == 0){
							$('#consignmentDetailModel').modal('hide');
							SpinnerService.endSpinner(modal);
							$scope.getManifestHistory();
							return;
						}
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
			$scope.filter['type'] ='false';
		}
		
		
		$scope.hasPermission = function(permission){
			var roles = userService.get().roles;
			if(permission != "" && roles != undefined){
				return roles.indexOf(permission) != -1;
			}
			return false;
		}
		
		
		$scope.getConsignmentDetails = function(refId,consignments){
			var modal = SpinnerService.startSpinner();	
			var url = weburl + "/get/consignments?biltyNo="+consignments.toString();
				return $http({
					url : url,
					method : 'GET'
				}).then(function(data) {
					$scope.consignmentDetails = data.data;
					SpinnerService.endSpinner(modal);
					$scope.consignmentId = refId;
					$scope.openConsignmentModel();
				}).catch(function(data) {
					SpinnerService.endSpinner(modal);
					$scope.addAlert('warning', 'Please Try Again !!!');
	            });
		}
		
		$scope.getManifestHistory = function(){
		var modal = SpinnerService.startSpinner();	
		var url = weburl + "/get/manifest"+$scope.getFilterString();
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
			$scope.getManifestHistory();
		}
		
		$scope.onFilterChange = function(key,value){
			if(value != undefined){
			$scope.filter[key] = value;	
			$scope.setCurrentPage(0);
			$scope.getManifestHistory();
			}
		}
		
		$scope.resetFilter = function(key){
			delete $scope.filter[key];
			if(key === 'company'){
				$scope.consignor = null;
			}
			$scope.setCurrentPage(0);
			$scope.getManifestHistory();
		}
		
		$scope.resetCreatedDateFilter = function(){
			$scope.createdDate1 = null;
			$scope.createdDate2 = null;
			delete $scope.filter['createdDate1'];
			delete $scope.filter['createdDate2'];
			$scope.setCurrentPage(0);
			$scope.getManifestHistory();
		}
		
		$scope.applyFilter = function() {
			$scope.setCurrentPage(0);
			$scope.getManifestHistory();
		};
		
		if($stateParams.searchTxt){
			$scope.createdDate1 = null;
			$scope.createdDate2 = null;
			delete $scope.filter['createdDate1'];
			delete $scope.filter['createdDate2'];
			$scope.filter['refId'] = $stateParams.searchTxt
			$scope.setCurrentPage(0);
			$scope.getManifestHistory();
		}
		else{
			$scope.setCurrentPage(0);
			$scope.getManifestHistory();
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