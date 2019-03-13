app.controller('inventoryController', [ '$http' ,'$scope','$filter','$q','$interval','userService' ,'SpinnerService','FileSaver', function($http , $scope ,$filter,$q,$interval,userService,SpinnerService,FileSaver){
	
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
    
    $scope.downloadReport = function(){
    	
    	var url = weburl+"/inventory";
		$http.get(url, { responseType: "arraybuffer" }).success(function(data){
			FileSaver.saveAs(new Blob([data],{type:"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}), "Inventory.xlsx");
		});
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