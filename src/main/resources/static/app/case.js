app.controller('caseController', [
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
			
			$scope.isCaseEdit = false;
			$scope.isOpdFound = false;
			$scope.crownMapping = [];
			$scope.crownDetail = {};
			$scope.Case = {};
			$scope.Case.crown={};
			$scope.Case.crown.details= [];
			$scope.person = {};
			$scope.Case = AppService.getCaseData();
			$scope.actions = ['BOOKED','INPROCESS','TRIAL','DELIVERD','INSERTION_DONE', 'REPEAT'];
			$scope.searchResults = [];
			$scope.isShowHistory = false;
			$scope.crown = null;
			$scope.today = new Date();
			$scope.getUser = function() {

				$http.get(weburl + "/username").success(function(data) {
					$rootScope.name = data.fullname;
					userService.set(data);
					$cookies.put("user", JSON.stringify(data));
				});
			}
			
			$scope.getCrownMapping = function(vendorId) {
				if(vendorId){
				var modal = SpinnerService.startSpinner();	
				$http.get(weburl + "/crown/"+vendorId).success(
						function(data, status) {
							if(data.success){
								$scope.crownMapping = data.data;
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
			};
			
			$scope.isCrownMappingPresent = function(type) {
				var isFound = false;
				$scope.crownMapping.forEach(function (mapping) {
					   if(mapping.crown === type){
						   isFound = true;
					   }
				});
				return isFound;
			};
			
			$scope.getCrownVersion = function(type) {
				var version = 0;
				$scope.crownMapping.forEach(function (mapping) {
					   if(mapping.crown === type){
						   version = mapping.version;
					   }
				});
				return version;
			};
			
			$scope.getCase = function(opdNo,date) {
				var modal = SpinnerService.startSpinner();	
				$http.get(weburl + "/case/"+opdNo+"/"+date).success(
						function(data, status) {
							if(data.success){
								$scope.isCaseEdit = true;
								$scope.Case = data.data;
								$scope.Case.remark = '';
							}
							else{
								$scope.addAlert('warning', data.msg[0]);
							}
							SpinnerService.endSpinner(modal);
							$scope.getCrownMapping($scope.Case.vender.id);
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
				});
			};
			
			$scope.ShowCrownDetails = function(c) {
				$scope.crown = c;
				$('#crownDetails').modal('show');
			}
			
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

			$scope.addNewPerson = function(name, type) {
				$scope.person.name = name;
				$scope.person.type = type;
				$('#addPersonModel').modal('show');
			}

			$scope.addCrown = function() {
				if($scope.crownMapping.length >0){
					$scope.crownDetail = {};
					$scope.crownDetail.type = $scope.crownMapping[0].crown;
					$('#addCrownModel').modal('show');
				}
			}
			
			$scope.addCrowninCase = function(crown) {
				var c = {};
				c.type = crown.type;
				c.crownNo = crown.crownNo;
				c.shade = crown.shade;
				c.version = $scope.getCrownVersion(crown.type);
				$scope.Case.crown.details[$scope.Case.crown.details.length] = c;
			}
			
			$scope.updateCrowns = function(crown)
			{
				 var idx = $scope.Case.crown.details.indexOf(crown);
				    if (idx > -1) {
				    	$scope.Case.crown.details.splice(idx, 1);
				    }
			}
			

			$scope.closePopUp = function() {
				$scope.person = {};
			}
			
			$scope.closeAddCrownModel = function() {
				$('#addCrownModel').modal('hide');
			}
			
			$scope.saveCase = function(Case) {
				var modal = SpinnerService.startSpinner();
				$http.post(weburl + "/add/case", Case).success(
						function(data, status) {
							if(data.success){
							$scope.initilizeNewCase();
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
			
			$scope.updateCase = function(Case){
				var modal = SpinnerService.startSpinner();
				$http.post(weburl + "/update/case", Case).success(
						function(data, status) {
							if(data.success){
							AppService.getLateCaseCount().success(function(data){
								$rootScope.lateCaseCount = data.data.count;
							});
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
			}

			$scope.savePerson = function(person) {
				var modal = SpinnerService.startSpinner();
				$http.post(weburl + "/add/person", person).success(
						function(data, status) {
							SpinnerService.endSpinner(modal);
							$scope.addAlert('success', 'Done');
							$scope.closePopUp();
							$('#addPersonModel').modal('hide');
						}).error(function(data, status) {
					$scope.addAlert('warning', 'Please Try Again !!!');
					SpinnerService.endSpinner(modal);
				});
			};

			$scope.initilizeNewCase = function() {
				$scope.Case = {};
				$scope.Case.crown={};
				$scope.Case.crown.details= [];
				$scope.Case.bookingDate = new Date();
				$scope.Case.appointmentDate = $scope.addWeekDays(
						$scope.Case.bookingDate, 4);
				$scope.Case.deliveredDate = $scope.addWeekDays(
						$scope.Case.bookingDate, 3);
			};

			$scope.addOneDay = function(date) {
				var result = new Date(date.getTime());
				result.setDate(result.getDate() + 1);
				return result;
			};
			
			$scope.subOneDay = function(date) {
				var result = new Date(date.getTime());
				result.setDate(result.getDate() - 1);
				return result;
			};

			$scope.onBookingDateChange = function(date) {
				$scope.Case.appointmentDate = $scope.addWeekDays(date, 4);
				$scope.Case.deliveredDate = $scope.addWeekDays(date, 3);
			};
			
			$scope.onAppointmentDateChange = function(date) {
				$scope.Case.deliveredDate = $scope.subWeekDays(date, 1);
			};

			$scope.addWeekDays = function(date, days) {
				var result = new Date(date.getTime());
				for (var i = 0; i < days; i++) {
					result = $scope.addOneDay(result);
					if (result.getDay() === 0)
						i--;
				}
				return result;
			};
			
			$scope.subWeekDays = function(date, days) {
				var result = new Date(date.getTime());
				for (var i = 0; i < days; i++) {
					result = $scope.subOneDay(result);
					if (result.getDay() === 0)
						i--;
				}
				return result;
			};
			
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
			
			$scope.getCaseHistory = function() {
				var modal = SpinnerService.startSpinner();
				var date = $filter('date')($scope.Case.bookingDate, 'dd-MM-yyyy');
				var opdNo = $scope.Case.opdNo;
				$http.get(weburl + "/case/history/"+opdNo+"/"+date).success(
						function(data, status) {
							if(data.success){
								$scope.searchResults = data.data;
								$scope.isShowHistory = true;
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
			
			if($stateParams.caseId != undefined && $stateParams.date != undefined ){
				$scope.getCase($stateParams.caseId,$stateParams.date)
			}
			else{
				$scope.initilizeNewCase()
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