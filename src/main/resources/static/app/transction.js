app.controller('transctionController', [ '$http' ,'$scope','$filter','$q','$interval','SpinnerService','userService', function($http ,$scope ,$filter ,$q ,$interval ,SpinnerService,userService){
	$scope.search = {};
	$scope.search.date1 = $scope.search.date2 = new Date();
	$scope.transctions = {};
	$scope.transction = []
	
	document.getElementById('date').focus();
	var config = {
            headers : {
                'Content-Type': 'application/json;'
            }
        };	
	
	var currDate = $filter('date')(new Date(), 'dd-MM-yyyy');
	var d = new Date();
	d.setDate(d.getDate() +1);
	var tomorrowDate = $filter('date')(d, 'dd-MM-yyyy');
	var url = weburl+"/transction?&startDate="+currDate+"&endDate="+(tomorrowDate);
	var modal = SpinnerService.startSpinner();
    $http.get(url).success(function(data){	
    	$scope.transctions = data;
    	SpinnerService.endSpinner(modal);
		
	},function myError(response) {
		SpinnerService.endSpinner(modal);
		$scope.addAlert('warning', 'Please Try Again');
    });
    
    
    $scope.getTransctions = function (){
    var date1 = $filter('date')($scope.search.date1, 'dd-MM-yyyy');
    var d = $scope.search.date2;
	d.setDate(d.getDate() +1);
	var date2 = $filter('date')(d, 'dd-MM-yyyy');
	if($scope.search.date1 > $scope.search.date2){
		$scope.addAlert('warning', 'FROM date can not be less greater then TO date');
	}
	else{
	var url = weburl+"/transction?&startDate="+date1+"&endDate="+(date2);
	var modal = SpinnerService.startSpinner();
    $http.get(url).success(function(data){	
    	$scope.transctions = data;
    	SpinnerService.endSpinner(modal);
	});
	}
  }  
    
    $scope.hasPermission = function(permission){
		var roles = userService.get().roles;
		if(permission != "" && roles != undefined){
			return roles.indexOf(permission) != -1;
		}
		return false;
	}
    
    $scope.showRemarkPopUp = function(trns){
    	$scope.transction = trns;
		$('#addRemark').modal('show');
    }
    
    $scope.hideRemarkPopUp = function(){
    	$scope.transction = [];
		$('#addRemark').modal('hide');
    }
    
    $scope.revertTrns = function(trns){
    	if(trns.remark == undefined || trns.remark == ''){
    		$scope.addAlert('warning', 'Please enter reason for Revert !!!');
    		return;
    	}
    	var modal = SpinnerService.startSpinner();
    	$http.post(weburl+"/revert",trns,config).success(function(data,status){	
			SpinnerService.endSpinner(modal);
			var index = $scope.transctions.indexOf(trns)
    		$scope.transctions.splice(index, 1);
			$scope.addAlert('success', 'Done');
			}).error(function(data, status) {
			$scope.addAlert('warning', 'Please Try Again !!!');
			SpinnerService.endSpinner(modal);
		});
    	$scope.hideRemarkPopUp();
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