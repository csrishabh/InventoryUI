
app.controller('loginController', [ '$http' ,'$scope', '$filter' , '$window' ,'$location','$cookies','$rootScope','userService','SpinnerService' ,'AppService',
	function($http , $scope , $filter , $window ,$location, $cookies,$rootScope,userService,SpinnerService,AppService){
	
	var config = {
            headers : {
                'Content-Type': 'application/json;',
                'Authorization': 'Bearer'
            }
        };
	

	$scope.login = function(user){
		var modal = SpinnerService.startSpinner();
		$http.post(weburl+"/login",user,config).success(function(data, status, headers, config){
			$http.defaults.headers.common.Authorization = headers(['authorization']);
			$cookies.put("access_token", headers(['authorization']));
			$scope.getUser();
			SpinnerService.endSpinner(modal);
			
		}).error(function(data, status) {
			if(status == 401){
				$scope.addAlert('warning', 'Wrong Username & Password');
			}
			else{
				$scope.addAlert('warning', 'Please try again');
			}
			SpinnerService.endSpinner(modal);
		});
		
	}
	
	$scope.getUser = function(){
		
		$http.get(weburl+"/username").success(function(data){	
			$rootScope.name = data.fullname;
			$rootScope.userId = data.username;
			userService.set(data);
			$cookies.put("user", JSON.stringify(data));
			if($scope.hasPermission('VENDOR')){
				$location.path('/caseHistory');
			}
			else if($scope.hasPermission('USER_CASE')){
				
				AppService.getLateCaseCount().success(function(data){
					$rootScope.lateCaseCount = data.data.count;
				});
			$location.path('/caseHistory');
			}
			else{
				$location.path('/product');
			}
		});
	}
	
	$scope.hasPermission = function(permission){
		var roles = userService.get().roles;
		if(permission != "" && roles != undefined){
			return roles.indexOf(permission) != -1;
		}
		return false;
	}
	
	$scope.alerts = [
	               ];

	               $scope.addAlert = function(type,messege) {
	                 $scope.alerts.push({type: type, msg: messege});
	               };

	               $scope.closeAlert = function(index) {
	                 $scope.alerts.splice(index, 1);
	               };
	
	
	}]);