app.controller('auditController', [ '$http' ,'$scope','$filter','$q','$interval','SpinnerService', function($http ,$scope ,$filter ,$q ,$interval ,SpinnerService){
	$scope.search = {};
	$scope.search.date1 = $scope.search.date2 = new Date();
	$scope.transctions = {};
	$scope.edit = false;
	document.getElementById('date').focus();
	var config = {
            headers : {
                'Content-Type': 'application/json;',
                'Authorization': 'Bearer'
            }
        };	
	var currDate = $filter('date')(new Date(), 'dd-MM-yyyy');
	var d = new Date();
	d.setDate(d.getDate() +1);
	var tomorrowDate = $filter('date')(d, 'dd-MM-yyyy');
	var url = weburl+"/audit/transction?&startDate="+currDate+"&endDate="+(tomorrowDate);
	var modal = SpinnerService.startSpinner();
    $http.get(url).success(function(data){	
    	$scope.transctions = data;
    	SpinnerService.endSpinner(modal);
		
	},function myError(response) {
		SpinnerService.endSpinner(modal);
		$scope.addAlert('warning', 'Please Try Again');
    });
    
    $scope.parseString = function(num) {return parseFloat(num)};
    
    $scope.audit = function(data) {
    
    	if(data[3] <=0){
    		$scope.addAlert('warning', 'Please Enter Amount');
    	}
    	else if(data[2]<=0){
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
    
    
    $scope.getAdtTransctions = function (){
    var date1 = $filter('date')($scope.search.date1, 'dd-MM-yyyy');
    var d = $scope.search.date2;
	d.setDate(d.getDate() +1);
	var date2 = $filter('date')(d, 'dd-MM-yyyy');
	if($scope.search.date1 > $scope.search.date2){
		$scope.addAlert('warning', 'FROM date can not be less greater then TO date');
	}
	else{
	var url = weburl+"/audit/transction?&startDate="+date1+"&endDate="+(date2);
	var modal = SpinnerService.startSpinner();
    $http.get(url).success(function(data){	
    	$scope.transctions = data;
    	SpinnerService.endSpinner(modal);
	});
	}
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