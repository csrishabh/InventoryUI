app.controller('inventoryController', [ '$http' ,'$scope','$filter','$q','$interval','userService' ,'SpinnerService', function($http , $scope ,$filter,$q,$interval,userService,SpinnerService){
	
	$scope.products = {};
	
    $scope.getCurrentInventory = function (){
    var modal = SpinnerService.startSpinner();		
	var url = weburl+"/products";
    $http.get(url).success(function(data){
    	SpinnerService.endSpinner(modal);
    	$scope.products = data;
	},function myError(response) {
		SpinnerService.endSpinner(modal);
		$scope.addAlert('warning', 'Please Try Again');
    });
  }  
    
    $scope.hasPermission = function(permission){
		var roles = userService.get().roles;
		if(permission != "" && roles != undefined){
			return roles.indexOf(permission) != -1;
		}
		return false;
	}
    
    $scope.getCurrentInventory();
    
    $scope.alerts = [
                   ];

  	               $scope.addAlert = function(type,messege) {
  	                 $scope.alerts.push({type: type, msg: messege});
  	               };

  	               $scope.closeAlert = function(index) {
  	                 $scope.alerts.splice(index, 1);
  	               };
		
	}]); 