app.controller('transctionController', [ '$http' ,'$scope','$filter','$q','$interval','SpinnerService','userService','$mdSidenav','FileSaver', 
	function($http ,$scope ,$filter ,$q ,$interval ,SpinnerService,userService,$mdSidenav,FileSaver){
	$scope.search = {};
	$scope.search.date1 = $scope.search.date2 = new Date();
	$scope.transctions = {};
	$scope.transction = []
	$scope.today = new Date();
	$scope.minDate = new Date(2018,00,01);
	$scope.filter = {};	
	$scope.filter['startDate'] = $filter('date')(new Date(), 'dd-MM-yyyy');
	$scope.filter['endDate'] = $filter('date')(new Date(), 'dd-MM-yyyy');
	
	var config = {
            headers : {
                'Content-Type': 'application/json;'
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
		$scope.getTransctions();
	}
	
	$scope.onFilterChange = function(key,value){
		if(value != undefined){
		$scope.filter[key] = value;	
		$scope.getTransctions();
		}
	}
	
	$scope.resetDateFilter = function(){
		$scope.search.date1 = null;
		$scope.search.date2 = null;
		delete $scope.filter['startDate'];
		delete $scope.filter['endDate']
		$scope.getTransctions();
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
	
	$scope.applyFilter = function() {
		$scope.getTransctions();
	};
	
    $scope.getTransctions = function (){
	var url = weburl+"/transction"+$scope.getFilterString();
	var modal = SpinnerService.startSpinner();
    $http.get(url).success(function(data){	
    	$scope.transctions = data.data;
    	SpinnerService.endSpinner(modal);
	}).error(function(response,status){
		SpinnerService.endSpinner(modal);
		$scope.addAlert('warning', 'Please Try Again');
    });
  }  
    
    $scope.downloadReport = function(){
    	
    	var url = weburl+"/transction/download"+$scope.getFilterString();
		$http.get(url, { responseType: "arraybuffer" }).success(function(data){
			FileSaver.saveAs(new Blob([data],{type:"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}), "Transction.xlsx");
		});
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
    
    $scope.getTransctions();
    
    $scope.alerts = [
                   ];

  	               $scope.addAlert = function(type,messege) {
  	                 $scope.alerts.push({type: type, msg: messege});
  	               };

  	               $scope.closeAlert = function(index) {
  	                 $scope.alerts.splice(index, 1);
  	               };
	
	
	}]); 