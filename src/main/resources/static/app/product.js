
app.controller('productController', [ '$http' ,'$scope', '$filter' , '$window','$cookies','$rootScope','userService','$log','SpinnerService' ,'$location', '$anchorScroll',function($http , $scope , $filter , $window , $cookies ,$rootScope, userService,$log,SpinnerService,$location,$anchorScroll){
	
	$scope.transction = {};
	$scope.transctions = [];
	$scope.products = [];
	$scope.product = {};
	$scope.date = new Date();
	$scope.showTransction = false;
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
	
	$scope.getAllProduct = function() {
	    var url = weburl+"/products";
	    return $http({
	        url: url,
	        method: 'GET'
	    }).then(function (data) {
	    	$scope.products = data.data;
	    });
	};
	
	$scope.next = function() {
		$scope.showTransction = true; 
	};
	
	$scope.back = function() {
		$scope.showTransction = false; 
	};
	
	$scope.getAllProduct();
	
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
		else{
			$http.post(weburl+"/addProduct",product,config).success(function(data){
				$scope.addAlert('success', 'Done');	
				$scope.product = {};
				if(isupdate){
					$('#addNewProductModal').modal('hide');
				}
			},function myError(response) {
		    
		    });	
		}
	}
	
	$scope.addNewProduct = function(name){
		$scope.product.name = name;
		$('#addNewProductModal').modal('show');
	}
	
	$scope.editProduct = function(product){
		$('#paymentDedtailsModal').modal('hide');
		$scope.product = product;
		$('#addNewProductModal').modal('show');
	}
	
	$scope.showPopUp = function(product){
			$scope.transction.date= new Date();
			$scope.product = product;
			$scope.searchText = null;
			$('#paymentDedtailsModal').modal('show');
			$('quantity').focus();
	}	
	
	$scope.closePopUp = function(){
		$scope.transction = {};	
		$scope.product = {};
	}
	
	$scope.addProduct = function(date, quantity, product){
		if(date > new Date()){
			$scope.addAlert('warning', 'Future date is not acceptable');
		}
		else if(quantity == undefined || quantity <= 0){
			$scope.addAlert('warning', 'Please enter quantity first');
		}
		else{
		var transction = {};	
		transction.type = "ADD"
		transction.productId = product.id
		transction.productName = product.name
		transction.date = date
		transction.quantity = quantity
		if(!$scope.isDuplicateTransction(transction, product)){
			$scope.transctions[$scope.transctions.length] = transction;
			$scope.updateProduct(transction,false);
		}
		$scope.closePopUp();
		if(product.quantity){
			product.quantity = 0
		}
		$('#paymentDedtailsModal').modal('hide');
		}
		
	}
	
	$scope.DispatchProduct = function(date, quantity, product){
		if(date > new Date()){
			$scope.addAlert('warning', 'Future date is not acceptable');
		}
		else if(quantity == undefined || quantity <= 0){
			$scope.addAlert('warning', 'Please enter quantity first');
		}
		else if(quantity > product.qtyAbl){
			$scope.addAlert('warning', 'Required quantity not available');
		}
		else{
		var transction = {};	
		transction.type = "DISPATCH"
		transction.productId = product.id
		transction.amount = 0
		transction.productName = product.name
		transction.date = date
		transction.quantity = quantity
		if(!$scope.isDuplicateTransction(transction, product)){
			$scope.transctions[$scope.transctions.length] = transction;
			$scope.updateProduct(transction,false);
		}		
		$scope.closePopUp();
		if(product.quantity){
			product.quantity = 0
		}
		$('#paymentDedtailsModal').modal('hide');
		}
		
	}
	
	
	/*$scope.isDuplicateTransction = function(transction, product) {
		for(i=0;i<$scope.transctions.length;i++) {
			var t = $scope.transctions[i];
			  if (t != undefined && t != null && t.productId == product.id && t.type == transction.type && t.date == transction.date) {
			        if(transction.type == 'DISPATCH' && (t.quantity + transction.quantity) > product.qtyAbl){
			        	$scope.addAlert('warning', 'Required quantity not available');
			        	return true;
			        }
			    	t.quantity = t.quantity + transction.quantity;
			    	return true;
			    }
		}
		return false;
    };*/
    
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
			    	t.quantity = t.quantity + transction.quantity;
			    	$scope.updateProduct(transction, false);
			    	return true;
			    }
		}
		return false;
    };
    
    $scope.updateProduct = function(transction , isDeleteTransction) {
    	
    	var product = $scope.products.find(function(element) { 
    		  return element.id == transction.productId; 
  		});
      	
      	if(transction.type == 'ADD'){
      		if(isDeleteTransction){
      		product.qtyAbl = product.qtyAbl - transction.quantity;
      		}
      		else{
      		product.qtyAbl = product.qtyAbl + transction.quantity;
      		}
      	}
      	else if(transction.type == 'DISPATCH'){
      		if(isDeleteTransction){
          		product.qtyAbl = product.qtyAbl + transction.quantity;
          		}
          		else{
          		product.qtyAbl = product.qtyAbl - transction.quantity;
          		}
        }	
    }
    
    $scope.saveTransctions = function(transctions) {
        if(transctions.length <= 0){
        	$scope.addAlert('warning', 'Nothing to submit yet !!!');
        	return;
        }
        var modal = SpinnerService.startSpinner();
    	$http.post(weburl+"/addTransctions",transctions,config).success(function(data,status){
				$scope.transctions = [];	
				SpinnerService.endSpinner(modal);
				$scope.addAlert('success', 'Done');
				$scope.getAllProduct();
				$scope.showTransction = false;
    	}).error(function(data, status) {
    		if(status == 406){
    			for(i=0;i<data.length;i++) {
    			var product = $scope.products.find(function(element) { 
    		    		  return element.id == data[i].productId; 
    		  		});
    			data[i].productName = product.name;
    			}
    			$scope.getAllProduct();
				$scope.addAlert('warning', 'Some items are not available');
				SpinnerService.endSpinner(modal);
				$scope.transctions = data;
    		}else{
    			SpinnerService.endSpinner(modal);	
    			$scope.addAlert('warning', 'Please Try Again !!!');
    		}
       });
    };
    
	$scope.setEdit = function(value) {
        
    	return !value;
    };
    
    $scope.gotoBottom = function() {
        // set the location.hash to the id of
        // the element you wish to scroll to.
        $location.hash('bottom');

        // call $anchorScroll()
        $anchorScroll();
      };
      
    $scope.deleteTransction = function(index) {
        
    	var transction  = $scope.transctions[index];
    	$scope.updateProduct(transction, true);
    	$scope.transctions.splice(index, 1);
    };
	
	$scope.alerts = [
	               ];

	               $scope.addAlert = function(type,messege) {
	                 $scope.alerts.push({type: type, msg: messege});
	               };

	               $scope.closeAlert = function(index) {
	                 $scope.alerts.splice(index, 1);
	               };
	
	
	}]);