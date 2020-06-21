app.controller('consignmentController', [
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
			
			$scope.isConsignmentEdit = false;
			$scope.consignment = {};
			$scope.today = new Date();
			$scope.consignee;
			$scope.consignor;
			
			$scope.hasPermission = function(permission){
				var roles = userService.get().roles;
				if(permission != "" && roles != undefined){
					return roles.indexOf(permission) != -1;
				}
				return false;
			}

			$scope.getPerson = function(searchStr, type) {
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
			
			$scope.setDestination = function(city){
				if(city){
					$scope.consignment.des = city;
				}
			}
			$scope.onConsigneeChnage = function(person){
				if(person){
					$scope.consignment.consignee = person.username;
				}
			}
			
			$scope.onConsignorChnage = function(person){
				if(person){
					$scope.consignment.consignor = person.username;
				}
			}
			
			
			$scope.showPaidByModel = function(c) {
				$('#paidByModal').modal('show');
			}
			
			$scope.closePaidByModel = function() {
				$('#paidByModal').modal('hide');
			}
			
			$scope.onBillingTypeChange = function() {
				var type = $scope.consignment.billingType;
				if(type=='PAID'){
					$scope.consignment.paidBy = "1";
				}
				else if(type=='TO_PAY'){
					$scope.consignment.paidBy = "2";
				}
				else{
					$scope.showPaidByModel();
				}
			}

			$scope.saveConsignment = function(con) {
				
				var modal = SpinnerService.startSpinner();
				var consignment = JSON.parse(JSON.stringify(con));
				consignment.weight = consignment.weight*1000;
				consignment.rate = consignment.rate*100;
				consignment.discount = consignment.discount*100;
				consignment.tax = consignment.tax*100;
				consignment.remark1 = consignment.remark1*100;
				consignment.remark2 = consignment.remark2*100;
				
				$http.post(weburl + "/add/consignment", consignment).success(
						function(data, status) {
							if(data.success){
							$scope.initilizeNewConsignment();
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
			
		
			$scope.initilizeNewConsignment = function() {
				$scope.consignment= {};
				$scope.consignment.bookingDate = new Date();
			};
			
			$scope.calculateTotal = function(){
				
				if($scope.consignment.unit == 'PIECES'){
					var total = ($scope.consignment.totalParcel * $scope.consignment.rate) + $scope.consignment.remark1 + $scope.consignment.remark2;
					var tax = ((total - $scope.consignment.discount) *  $scope.consignment.tax)/100;
					$scope.subtotal = total - $scope.consignment.discount;
					$scope.grandTotal= $scope.subtotal + tax
				}
				else if($scope.consignment.unit == 'KILOGRAM'){
					var total = ($scope.consignment.weight * $scope.consignment.rate) + $scope.consignment.remark1 + $scope.consignment.remark2 ;
					var tax = ((total - $scope.consignment.discount) *  $scope.consignment.tax)/100;
					$scope.subtotal = total - $scope.consignment.discount;
					$scope.grandTotal= $scope.subtotal + tax
				}
			}
			
			
			$scope.checkOpdNoExist = function(opdNo) {
				var modal = SpinnerService.startSpinner();	
				$http.get(weburl + "/patient/"+opdNo).success(
						function(data, status) {
							if(data.success){
								data.data.bookingDate = $scope.Case.bookingDate;
								data.data.appointmentDate = $scope.Case.appointmentDate;
								data.data.deliveredDate = $scope.Case.deliveredDate;
								$scope.Case = data.data;
								$scope.isOpdFound = true;
							}
							else{
								if(data.msg[0] == 'CASE NOT FOUND'){
									$scope.Case.patient = "";
									$scope.isOpdFound = false;
								}
								else{
									$scope.Case.opdNo = "";
									$scope.isOpdFound = false;
									$scope.addAlert('warning', 'Please Try Again !!!');
								}
							}
							SpinnerService.endSpinner(modal);
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
				});
			};
			
			
			if($stateParams.caseId != undefined && $stateParams.date != undefined ){
				$scope.getCase($stateParams.caseId,$stateParams.date)
			}
			else{
				$scope.initilizeNewConsignment()
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