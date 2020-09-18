app.controller('inventoryController', [ '$http' ,'$scope','$filter','$q','$interval','userService' ,'SpinnerService','FileSaver','$stateParams', 
	function($http , $scope ,$filter,$q,$interval,userService,SpinnerService,FileSaver,$stateParams){
	
	$scope.products = {};
	$scope.product = [];
	
    $scope.getCurrentInventory = function (searchTxt){
    var modal = SpinnerService.startSpinner();
    if(searchTxt != ''){
    	var url = weburl+"/products?name="+searchTxt;
    }
    else{
	var url = weburl+"/products";
    }
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
    
    $scope.openUpdateProductModel = function(prd){
		$scope.product = JSON.parse(JSON.stringify(prd));
		$('#addNewProductModal').modal('show');
	}
    
    $scope.updateProduct = function(product){
		
		if( product.name == undefined || product.name == ""){
			$scope.addAlert('warning', 'Please enter name first');
		}
		else if(product.unit == undefined || product.unit == ""){
			$scope.addAlert('warning', 'Please select unit first');
		}
		else if(product.alert == undefined || product.alert <= 0){
			$scope.addAlert('warning', 'Please Enter alert Quantity');
		}
		else{
			$http.post(weburl+"/update/product",product).success(function(data,status){
				$scope.addAlert('success', data.msg[0]);
				$scope.product = {};
				$('#addNewProductModal').modal('hide');
			},function myError(response) {
				$scope.addAlert('warning', data.msg[0]);
		    });	
		}
	}
    
    
    $scope.updateProductStatus = function(product){
		
		if( product.name == undefined || product.name == ""){
			$scope.addAlert('warning', 'Please enter name first');
		}
		else if(product.unit == undefined || product.unit == ""){
			$scope.addAlert('warning', 'Please select unit first');
		}
		else if(product.alert == undefined || product.alert <= 0){
			$scope.addAlert('warning', 'Please Enter alert Quantity');
		}
		else{
			$http.post(weburl+"/update/product/status",product).success(function(data,status){
				$scope.addAlert('success', data.msg[0]);
				$scope.product = {};
				$('#addNewProductModal').modal('hide');
			},function myError(response) {
				$scope.addAlert('warning', data.msg[0]);
		    });	
		}
	}
    
    $scope.downloadReport = function(){
    	
    	var url = weburl+"/inventory";
		$http.get(url, { responseType: "arraybuffer" }).success(function(data){
			FileSaver.saveAs(new Blob([data],{type:"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}), "Inventory.xlsx");
		});
    }
    if($stateParams.searchTxt){
    	$scope.getCurrentInventory($stateParams.searchTxt);
    }
    else{
    $scope.getCurrentInventory('');
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