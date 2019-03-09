
app.controller('loginController', [ '$http' ,'$scope', '$filter' , '$window' ,'$location','$cookies','$rootScope','userService','SpinnerService',function($http , $scope , $filter , $window ,$location, $cookies,$rootScope,userService,SpinnerService){
	
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
			$location.path('/product');	
			SpinnerService.endSpinner(modal);
		}).error(function(data, status) {
			if(status == 401){
				$scope.addAlert('warning', 'Wrong Username & Password');
				SpinnerService.endSpinner(modal);
			}
		});
		
	}
	
	$scope.getUser = function(){
		
		$http.get(weburl+"/username").success(function(data){	
			$rootScope.name = data.fullname;
			userService.set(data);
			$cookies.put("user", JSON.stringify(data));
		});
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