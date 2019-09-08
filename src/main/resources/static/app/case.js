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
		function($http, $scope, $filter, $window, $location, $cookies,
				$rootScope, userService, SpinnerService,$mdDialog,AppService) {
			
			$scope.isCaseEdit = false;
			$scope.isOpdFound = false;
			$scope.Case = {};
			$scope.Case.crown={};
			$scope.Case.crown.details= [];
			$scope.person = {};
			$scope.Case = AppService.getCaseData();
			$scope.actions = ['BOOKED','INPROCESS','TRIAL','DELIVERD','INSERTION_DONE', 'REPEAT'];
			$scope.searchResults = [];
			$scope.isShowHistory = false;
			$scope.crown = null;
			$scope.getUser = function() {

				$http.get(weburl + "/username").success(function(data) {
					$rootScope.name = data.fullname;
					userService.set(data);
					$cookies.put("user", JSON.stringify(data));
				});
			}
			
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
				$('#addCrownModel').modal('show');
			}
			
			$scope.addCrowninCase = function(crown) {
				var c = {};
				c.type = crown.type;
				c.crownNo = crown.crownNo;
				c.shade = crown.shade;
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
							$scope.isCaseEdit = false;
							AppService.getLateCaseCount().success(function(data){
								$rootScope.lateCaseCount = data.data.count;
							});
							$location.path('/caseHistory');
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

			$scope.onBookingDateChange = function(date) {
				$scope.Case.appointmentDate = $scope.addWeekDays(date, 4);
				$scope.Case.deliveredDate = $scope.addWeekDays(date, 3);
			};

			$scope.addWeekDays = function(date, days) {
				var result = new Date(date.getTime());
				for (var i = 0; i < days; i++) {
					result = $scope.addOneDay(result);
					if (result.getDay() === 6 || result.getDay() === 0)
						i--;
				}
				;
				return result;
			};
			
			$scope.checkOpdNoExist = function(opdNo) {
				var modal = SpinnerService.startSpinner();	
				$http.get(weburl + "/patient/"+opdNo).success(
						function(data, status) {
							if(data.success){
								$scope.Case.patient = data.data;
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
				var date = $filter('date')($scope.Case.bookingDate._d, 'dd-MM-yyyy');
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
			
			if($scope.Case != undefined){
				$scope.isCaseEdit = true;
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