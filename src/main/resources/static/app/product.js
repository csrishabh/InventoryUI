
app.controller('productController', [ '$http' ,'$scope', '$filter' , '$window','$cookies','$rootScope','userService','$log','SpinnerService' ,'$location', '$anchorScroll', 'pagerService','$q'
	,function($http , $scope , $filter , $window , $cookies ,$rootScope, userService,$log,SpinnerService,$location,$anchorScroll,PagerService ,$q){
	
	$scope.transction = {};
	$scope.transctions = [];
	$scope.products = [];
	$scope.product = {};
	$scope.date = new Date();
	$scope.showTransction = false;
	$scope.pager = {};
	$scope.searchResult = [];
	$scope.orderByField = "name";
	$scope.reverseSort = false;
	$scope.unit="";
	$scope.productName = "";
	$scope.units = ['BOOKED','INPROCESS','TRIAL','DELIVERD','COMPLETED'];
	var config = {
            headers : {
                'Content-Type': 'application/json;'
            }
        }
	$scope.getProduct = function(searchStr) {
	    if(!searchStr) {
	        var searchStrEncoded = "";
	    } else {
	        var searchStrEncoded = escape(searchStr);
	    }
	    var url = weburl+"/products/" + searchStrEncoded;
	    return $http({
	        url: url,
	        method: 'GET'
	    }).then(function (data) {
	        return data.data;
	    });
	};
	
	
	$scope.getProductById = function(id) {
		var modal = SpinnerService.startSpinner();
	    var url = weburl+"/product/"+(id);
	    var deferred = $q.defer();
	    $http({
	        url: url,
	        method: 'GET'
	    }).then(function (data) {
	    	SpinnerService.endSpinner(modal);
	    	deferred.resolve(data);
	    	return deferred.promise;
	    });
	};
	
	$scope.getAllProduct = function(pageNo) {
		var modal = SpinnerService.startSpinner();
		if($scope.productName == undefined){
			$scope.productName = '';
		}
		var url = weburl+"/searchProducts/"+(pageNo-1)+"?reverseSort="+$scope.reverseSort+"&orderBy="+$scope.orderByField+"&name="+$scope.productName;
	    return $http({
	        url: url,
	        method: 'GET'
	    }).then(function (data) {
	    	$scope.searchResult = data.data;
	    	$scope.products = $scope.searchResult.content;
	    	$scope.setPage(pageNo);
	    	SpinnerService.endSpinner(modal);
	    },function(data){
	    	$location.path('/login');
	    	SpinnerService.endSpinner(modal);
	    });
	};
	
	$scope.next = function() {
		
		var modal = SpinnerService.startSpinner();
		$http.get(weburl+"/getCart",config).success(function(data,status){	
			SpinnerService.endSpinner(modal);
			$scope.transctions = data;
				if(data.length >0 ){
					$scope.showTransction = true; 
				}
				else{
					$scope.addAlert('warning', 'Your cart is empty');
				}
			}).error(function(data, status) {
			$scope.transctions = [];
			SpinnerService.endSpinner(modal);
		});
	};
	
	$scope.back = function() {
		$scope.showTransction = false; 
	};
	
	$scope.changeSort = function(fieldName) {
		$scope.orderByField = fieldName;
		$scope.reverseSort = !$scope.reverseSort;
		$scope.getAllProduct(1);
	};
	
	$scope.getAllProduct(1);
	
	$scope.setPage = function(page) {
	
	if (page < 1 || page > $scope.pager.totalPages) {
		return;
	}
	$scope.pager = PagerService.GetPager($scope.searchResult.totalElements, page);
	}

	
	this.hasPermission = function(permission){
		var roles = userService.get().roles;
		if(permission != "" && roles != undefined){
			return roles.indexOf(permission) != -1;
		}
		return false;
	}

	$scope.saveProduct = function(product , isupdate){
		
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
			$http.post(weburl+"/addProduct",product,config).success(function(data,status){
				if(status == 208){
					$scope.addAlert('warning', 'This Product is Already Added');
				}
				else{
				$scope.addAlert('success', 'Done');	
				$scope.product = {};
				$('#addNewProductModal').modal('hide');
				}	
			},function myError(response) {
				$scope.addAlert('warning', 'Please Try Again');
		    });	
		}
	}
	
	$scope.addNewProduct = function(name){
		$scope.product.name = name;
		$scope.product.unit = 'PIECES';
		$('#addNewProductModal').modal('show');
	}
	
	$scope.editProduct = function(product){
		$('#paymentDedtailsModal').modal('hide');
		$scope.product = product;
		$('#addNewProductModal').modal('show');
	}
	
	$scope.showPopUp = function(product){
			$scope.transction.date= new Date();
			$scope.transction.quantity = 0;
			$scope.product = product;
			$scope.searchText = null;
			$('#paymentDedtailsModal').modal('show');
			$('quantity').focus();
	}	
	
	$scope.closePopUp = function(){
		$scope.transction = {};	
		$scope.product = {};
		$scope.unit= undefined;
	}
	
	$scope.addProduct = function(date, quantity, product,unit){
		if(date > new Date()){
			$scope.addAlert('warning', 'Future date is not acceptable');
		}
		else if(quantity == undefined || quantity <= 0){
			$scope.addAlert('warning', 'Please enter quantity first');
		}
		else if(unit == undefined){
			$scope.addAlert('warning', 'Please select unit first');
		}
		else{
		var transction = {};	
		transction.type = "ADD"
		transction.productId = product.id
		transction.productName = product.name
		transction.date = date
		if(unit == 'GRAM' || unit == 'MILILITER')
		{
			transction.quantity = quantity/1000;
		}else{
			transction.quantity = quantity;
		}
		var modal = SpinnerService.startSpinner();
		$http.post(weburl+"/carted",transction,config).success(function(data,status){	
			SpinnerService.endSpinner(modal);
			$scope.addAlert('success', 'Done');
			$scope.closePopUp();
			if(product.quantity){
				product.quantity = 0
			}
			$('#paymentDedtailsModal').modal('hide');
			}).error(function(data, status) {
			$scope.addAlert('warning', 'Please Try Again !!!');
			SpinnerService.endSpinner(modal);
		});
	}	
};
	
	$scope.DispatchProduct = function(date, quantity, product,unit){
		
		if(date > new Date()){
			$scope.addAlert('warning', 'Future date is not acceptable');
		}
		else if(quantity == undefined || quantity <= 0){
			$scope.addAlert('warning', 'Please enter quantity first');
		}
		else if(unit == undefined){
			$scope.addAlert('warning', 'Please select unit first');
		}
		else{
			var transction = {};	
			transction.type = "DISPATCH"
			transction.productId = product.id
			transction.amount = 0
			transction.date = date
			if(unit == 'GRAM' || unit == 'MILILITER')
			{
				transction.quantity = quantity/1000;
			}else{
				transction.quantity = quantity;
			}
			var modal = SpinnerService.startSpinner();
			$http.post(weburl+"/carted",transction,config).success(function(data,status){	
				SpinnerService.endSpinner(modal);
				$scope.addAlert('success', 'Done');
				$scope.closePopUp();
				if(product.quantity){
					product.quantity = 0
				}
				$('#paymentDedtailsModal').modal('hide');
				}).error(function(data, status) {
			if(status == 406){
				$scope.addAlert('warning', 'Required quantity not available');
				SpinnerService.endSpinner(modal);
			}
			});
		}	
	}

	
    $scope.isDuplicateTransction = function(transction, product) {
		for(i=0;i<$scope.transctions.length;i++) {
			var t = $scope.transctions[i];
			  if (t != undefined && t != null && t.productId == product.id) {
				  	
				  if(t.type != transction.type){
				  		$scope.addAlert('warning', 'You can either add or Dispatch a product');
				  		return true;
				  	}
			        if(transction.type == 'DISPATCH' && (transction.quantity) > product.qtyAbl){
			        	$scope.addAlert('warning', 'Required quantity not available');
			        	return true;
			        }
			        if(t.date.toDateString() != transction.date.toDateString()){
			        	continue;
			        }
			    	t.quantity =  $scope.round((t.quantity + transction.quantity),3);
			    	//$scope.updateProduct(transction, false);
			    	return true;
			    }
		}
		return false;
    };
    
    
    
    $scope.saveTransctions = function(transctions) {
        if(transctions.length <= 0){
        	$scope.addAlert('warning', 'Nothing to submit yet !!!');
        	return;
        }
        var modal = SpinnerService.startSpinner();
    	$http.post(weburl+"/dispatch/Cart",config).success(function(data,status){
				$scope.transctions = [];	
				SpinnerService.endSpinner(modal);
				$scope.addAlert('success', 'Done');
				$scope.showTransction = false;
    	}).error(function(data,status) {
    			if(status == 406){
    			$scope.addAlert('warning', 'Nothing to submit yet !!!');
    			}
    			else{
    			$scope.addAlert('warning', 'Please Try Again !!!');
    			}
    			SpinnerService.endSpinner(modal);
       });
    };
    
    
	$scope.setEdit = function(value) {
        
    	return !value;
    };
       
    $scope.updateTransction = function(index , isDeleted, checked) {
        
    	var transction  = $scope.transctions[index];
    	if(isDeleted){
    		transction.quantity	= 0;
    	}
    	else if(transction.quantity == undefined || transction.quantity <= 0){
			$scope.addAlert('warning', 'Please enter quantity first');
			return;
		}
    	var modal = SpinnerService.startSpinner();
    	 
     	$http.post(weburl+"/update/carted",transction,config).success(function(data,status){	
 				SpinnerService.endSpinner(modal);
 				$scope.addAlert('success', 'Done');
 				if(isDeleted){
 					$scope.transctions.splice(index, 1);
 		    	}
 				else{
 					$scope.transctions[index] = data
 				}
     	}).error(function(data, status) {
     		if(status == 406){
 				$scope.addAlert('warning', 'Quantity not available');
 				SpinnerService.endSpinner(modal);
     		}else{
     			SpinnerService.endSpinner(modal);	
     			$scope.addAlert('warning', 'Please Try Again !!!');
     		}
        });
     	
     	return !checked
    };
    
    $scope.round = function(value, decimals) {
    	  return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
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