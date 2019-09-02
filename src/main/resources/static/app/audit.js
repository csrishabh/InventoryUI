app.controller('auditController', [ '$http' ,'$scope','$filter','$q','$interval','SpinnerService','$mdSidenav', function($http ,$scope ,$filter ,$q ,$interval ,SpinnerService,$mdSidenav){
	$scope.search = {};
	$scope.search.date1 = $scope.search.date2 = new Date();
	$scope.transctions = {};
	$scope.edit = false;
	$scope.today = new Date();
	$scope.minDate = new Date(2018,00,01);
	$scope.filter = {};	
	$scope.filter['startDate'] = $filter('date')(new Date(), 'dd-MM-yyyy');
	$scope.filter['endDate'] = $filter('date')(new Date(), 'dd-MM-yyyy');
	
	var config = {
            headers : {
                'Content-Type': 'application/json;',
                'Authorization': 'Bearer'
            }
        };	
	
    $scope.openFilterNav = function() {
	    $mdSidenav('left').toggle();
	};
	
	$scope.getFilterString = function(){
		var filterString = "?";
		angular.forEach($scope.filter,function(value,key){
			filterString = filterString + key + "=" +value + "&";
		});
		return filterString;
	}
	
	$scope.onDateChange = function(key,date){
		
		$scope.filter[key] = $filter('date')(date, 'dd-MM-yyyy');	
		$scope.getAdtTransctions();
	}
	
	$scope.onFilterChange = function(key,value){
		if(value != undefined){
			$scope.filter[key] = value;	
			$scope.getAdtTransctions();
		}	
	}
	
	$scope.resetDateFilter = function(){
		$scope.search.date1 = null;
		$scope.search.date2 = null;
		delete $scope.filter['startDate'];
		delete $scope.filter['endDate']
		$scope.getAdtTransctions();
	}
    
    $scope.parseString = function(num) {return parseFloat(num)};
    
    $scope.audit = function(data) {
    
    	if(data[3] <=0){
    		$scope.addAlert('warning', 'Please Enter Amount');
    	}
    	else if(data[2]== undefined || data[2]<=0){
    		$scope.addAlert('warning', 'Please Enter Quantity');
    	}
    	else{
    	var modal = SpinnerService.startSpinner();	
    	var t = {};
    	t.id = data[6];
    	t.amount = data[3];
    	t.quantity= data[2];
    	
    	$http.post(weburl+"/audit",t).success(function(response, status, headers, config){
			var index = $scope.transctions.indexOf(data)
    		$scope.transctions.splice(index, 1);
			SpinnerService.endSpinner(modal);
			$scope.addAlert('success', 'Done');
		}).error(function(response,status){
			SpinnerService.endSpinner(modal);
			if(status == 406){
				$scope.addAlert('warning', 'Transction has been alredy audited');
			}
			else{
			$scope.addAlert('warning', 'Please Try Again');
			}
	    });
    }
   }
    
    
    $scope.setEdit = function(value) {
        
    	return !value;
    };
    
    $scope.deleteTransction = function(data) {
    	if(data[6] == undefined && data[6] === ""){
    		$scope.addAlert('warning', 'Please try again');
    	}
    	else{
    	var modal = SpinnerService.startSpinner();	
    	var t = {};
    	t.id = data[6];
    	$http.post(weburl+"/delTransction",t).success(function(response, status, headers, config){
    		SpinnerService.endSpinner(modal);
    		$scope.addAlert('success', 'Done');
			var index = $scope.transctions.indexOf(data)
    		$scope.transctions.splice(index, 1);  
		}).error(function(response,status){
			SpinnerService.endSpinner(modal);
			if(status == 406){
				$scope.addAlert('warning', 'Transction has been alredy audited');
			}
			else{
			$scope.addAlert('warning', 'Please Try Again');
			}
	    });
    }
    };
    
    $scope.applyFilter = function() {
		$scope.getAdtTransctions();
	};
 
    $scope.getAdtTransctions = function (){
	var url = weburl+"/audit/transction"+$scope.getFilterString();
	var modal = SpinnerService.startSpinner();
    $http.get(url).success(function(data){	
    	$scope.transctions = data;
    	SpinnerService.endSpinner(modal);
	}).error(function(response,status){
		SpinnerService.endSpinner(modal);
		$scope.addAlert('warning', 'Please Try Again');
    });
  } 
    
    $scope.getUser = function(searchStr, type) {
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
		});
	};
    
    $scope.getAdtTransctions();
    
    $scope.alerts = [
                   ];

  	               $scope.addAlert = function(type,messege) {
  	                 $scope.alerts.push({type: type, msg: messege});
  	               };

  	               $scope.closeAlert = function(index) {
  	                 $scope.alerts.splice(index, 1);
  	               };
	
	
	}]); 