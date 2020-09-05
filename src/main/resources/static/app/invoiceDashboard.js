app.controller('invoiceDashboardController', [
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
		$scope.selected = [];
		$scope.activeFilter = 1;
		$scope.invoice = {};
		$scope.challan={};
		$scope.currentPage = 0;
		$scope.pageSize = 15;
		$scope.currInvoice={};
		
		$scope.formatFilterDate = function(resp) {
			if(resp == undefined || resp == ''){
				return null;
			}
			var date = resp.split("-");
			var response = date[1] + "-" + date[0] + "-" + date[2];
			return new Date(response);
		};
		
		$scope.openInvoiceHistoryModel = function(){
			$('#invoiceHistoryModel').modal('show');
		}
		
		$scope.openChallanListModel = function(){
			$('#challanModal').modal('show');
		}
		
		$scope.openCreateInvoiceModel = function(){
			$scope.invoice.createdDate = new Date();
			$('#createInvoiceModel').modal('show');
		}
		
		$scope.closeInvoiceModel = function(){
			$('#createInvoiceModel').modal('hide');
			$scope.initlizeInvoice();
		}
		
		$scope.initlizeInvoice = function(){
			$scope.invoice = {};
		}
		
		$scope.closeChallanModel = function(){
			$('#addChallanModel').modal('hide');
			$scope.initlizeChallan();
		}
		
		$scope.initlizeChallan = function(){
			$scope.challan={};
		}
	
		$scope.openAddChallanModel = function(invoice){
			$scope.challan.invoiceNo = 	invoice.refId;
			$('#addChallanModel').modal('show');
		}
		
		$scope.openInvoiceUpdateModel = function(invoice){
			$scope.currInvoice.refId = 	invoice.refId;
			$scope.currInvoice.refNo = 	invoice.refNo;
			$scope.currInvoice.finalValue = invoice.finalValue;
			$scope.currInvoice.createdDate = invoice.createdDate;
			$scope.currInvoice.amount = invoice.amount;
			$('#updateInvoiceModel').modal('show');
		}
		
		
		$scope.updateInvoice = function(currInvoice,newAmount){
			var modal = SpinnerService.startSpinner();
			currInvoice.adjustment = (newAmount*100) - (currInvoice.amount*100);
			$http.post(weburl + "/update/invoice", currInvoice).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$('#updateInvoiceModel').modal('hide');
						$scope.getInvoiceDashboard();
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
		
		$scope.createInvoice = function(issuer, amount){
			var modal = SpinnerService.startSpinner();
			$scope.invoice.amount = amount*100;
			$scope.invoice.issuer = issuer.username;
			$http.post(weburl + "/add/invoice", $scope.invoice).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$scope.closeInvoiceModel();
						$scope.getInvoiceDashboard();
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
		
		$scope.createChallan = function(amount){
			var modal = SpinnerService.startSpinner();
			$scope.challan.amount = amount*100;
			$http.post(weburl + "/add/challan", $scope.challan).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$scope.closeChallanModel();
						$scope.getInvoiceDashboard();
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
		
		$scope.cancelInvoice = function(refId){
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/cancel/invoice", refId).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$scope.getInvoiceDashboard();
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
		
		$scope.cancelChallan = function(refId){
			var modal = SpinnerService.startSpinner();
			$http.post(weburl + "/cancel/challan", refId).success(
					function(data, status) {
						if(data.success){
						$scope.addAlert('success', data.msg[0]);
						$('#challanModal').modal('hide');
						$scope.getInvoiceDashboard();
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
		
		$scope.updateIssuer = function(issuer){
			if(issuer){
				$scope.invoice.issuer = issuer.username;
			}
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
			$scope.filter['status'] = 'DUE';
			$scope.filter['type'] ='false';
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
			$scope.getInvoiceDashboard();
		}
		
		$scope.setCurrentPage = function(pageNo){
			$scope.currentPage = pageNo;
			$scope.filter['pageNo'] = $scope.currentPage;
			$scope.filter['pageSize'] = $scope.pageSize;
		}
		
		
		$scope.getInvoiceDashboard = function(){
		var modal = SpinnerService.startSpinner();	
		
		var url = weburl + "/get/invoice"+$scope.getFilterString();
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
			$scope.getInvoiceDashboard();
		}
		
		$scope.onFilterChange = function(key,value){
			if(value != undefined){
			$scope.filter[key] = value;
			$scope.setCurrentPage(0);
			$scope.getInvoiceDashboard();
			}
		}
		
		$scope.resetFilter = function(key){
			delete $scope.filter[key];
			if(key === 'issuer'){
				$scope.issuer = null;
			}
			else if(key === 'createdBy'){
				$scope.createdBy = null;
			}
			$scope.setCurrentPage(0);
			$scope.getInvoiceDashboard();
		}
		
		$scope.resetCreateDateFilter = function(){
			$scope.createDate1 = null;
			$scope.createDate2 = null;
			delete $scope.filter['createdDate1'];
			delete $scope.filter['createdDate2'];
			$scope.setCurrentPage(0);
			$scope.getInvoiceDashboard();
		}
		
		$scope.applyFilter = function() {
			$scope.setCurrentPage(0);
			$scope.getInvoiceDashboard();
		};
		
		
		$scope.getInvoiceHistory = function(manifests){
			var modal = SpinnerService.startSpinner();	
			var url = weburl + "/get/invoice/"+manifests.toString();
			$http.get(url).success(
					function(data, status) {
						if(data.success){
							$scope.invoiceHistory = data.data;
							SpinnerService.endSpinner(modal);
							$scope.openInvoiceHistoryModel();
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
		
		$scope.getChallanList = function(manifests){
			var modal = SpinnerService.startSpinner();	
			var url = weburl + "/get/challan/"+manifests.toString();
			$http.get(url).success(
					function(data, status) {
						if(data.success){
							$scope.challanList = data.data;
							SpinnerService.endSpinner(modal);
							$scope.openChallanListModel();
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
		
		
		if($stateParams.searchTxt){
			$scope.bookingDate1 = null;
			$scope.bookingDate2 = null;
			delete $scope.filter['bookingDate1'];
			delete $scope.filter['bookingDate2'];
			$scope.filter['biltyNo'] = $stateParams.searchTxt
			$scope.setCurrentPage(0);
			$scope.getInvoiceDashboard();
		}
		else{
			$scope.setCurrentPage(0);
			$scope.getInvoiceDashboard();
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