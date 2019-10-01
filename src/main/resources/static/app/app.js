var app = angular.module('store',['ui.bootstrap','ngAnimate','angular-loading-bar' ,'ui.router','ngSanitize','ui.select2', 'ui.select','ngCsv', 'ngCookies','ngMdBadge','ngAria','ngMaterial','ngFileSaver','ngMessages','ngMaterialDatePicker','ngRoute','ngPatternRestrict']);
  // set a custom templ
var weburl = "https://jdback.herokuapp.com";
//var weburl = "http://localhost:8080";
var UIUrl = "";

app.config(function($stateProvider, $urlRouterProvider ,$httpProvider,$locationProvider,$routeProvider) {
	$locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
	});
	
	$stateProvider
	.state('product',{
		url: '/product',
		templateUrl: UIUrl+'/product.html'
	})
	
	.state('audit',{
		url: '/audit',
		templateUrl: UIUrl+'/auditDetails.html'
	})
	
	.state('login',{
		url: '/login',
		templateUrl: UIUrl+'/login.html'
	})
	
	.state('inventory',{
		url: '/inventory',
		templateUrl: UIUrl+'/Inventory.html'
	})
	
	.state('inventory/view/searchTxt',{
		url: '/inventory/:view/:searchTxt',
		templateUrl: UIUrl+'/Inventory.html'
	})
	
	.state('transction',{
		url: '/transction',
		templateUrl: UIUrl+'/transction.html'
	})
	
	.state('case',{
		url: '/case',
		templateUrl: UIUrl+'/case.html'
	})
	
	.state('editcase',{
		url: '/editcase/:caseId/:date',
		templateUrl: UIUrl+'/case.html'
	})
	
	.state('caseHistory',{
		url: '/caseHistory',
		templateUrl: UIUrl+'/caseHistory.html'
	})
	
	.state('caseHistory/view',{
		url: '/caseHistory/:view',
		templateUrl: UIUrl+'/caseHistory.html'
	})
	
	.state('caseHistory/view/searchTxt',{
		url: '/caseHistory/:view/:searchTxt',
		templateUrl: UIUrl+'/caseHistory.html'
	})
	
	.state('searchCase',{
		url: '/searchCase/:searchTxt',
		templateUrl: UIUrl+'/caseHistory.html'
	})
	
	.state('lateCases',{
		url: '/lateCases',
		templateUrl: UIUrl+'/caseHistory.html'
	})
	.state('todayCases',{
		url: '/todayCases',
		templateUrl: UIUrl+'/caseHistory.html'
	});
	
	$httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
	
});


app.config(function($mdAriaProvider) {
	   // Globally disables all ARIA warnings.
	   $mdAriaProvider.disableWarnings();
});

app.directive('nextOnEnter', function () {
    return {
        restrict: 'A',
        link: function ($scope, selem, attrs) {
            selem.bind('keydown', function (e) {
                var code = e.keyCode || e.which;
                if (code === 13) {
                    e.preventDefault();
                    var pageElems = document.querySelectorAll('input, select, textarea'),
                        elem = e.originalEvent.srcElement
                        focusNext = false,
                        len = pageElems.length;
                    for (var i = 0; i < len; i++) {
                        var pe = pageElems[i];
                        if (focusNext) {
                            if (pe.style.display !== 'none') {
                                pe.focus();
                                break;
                            }
                        } else if (pe === elem) {
                            focusNext = true;
                        }
                    }
                }
            });
        }
    }
});

app.directive('dlEnterKey', function () {
	return function(scope, element, attrs) {

        element.bind("keydown keypress", function(event) {
            var keyCode = event.which || event.keyCode;

            // If enter key is pressed
            if (keyCode === 13) {
                scope.$apply(function() {
                        // Evaluate the expression
                    scope.$eval(attrs.dlEnterKey);
                });

                event.preventDefault();
            }
        });
    }
});


app.directive('input', ['$interval', function($interval) {
    return {
      restrict: 'E', // It restricts that the directive will only be a HTML element (tag name), not an attribute.
      link: function(scope, elm, attr) {
        if (attr.type === 'checkbox') {
          elm.on('keypress', function(event) {
            var keyCode = (event.keyCode ? event.keyCode : event.which);
            if (keyCode === 13) {
              event.preventDefault(); // only when enter key is pressed.
              elm.trigger('click');
              scope.$apply();
            }
          });
        }
      }
    };
  }
]);


app.controller('myctrl',['$location','$cookies','$rootScope','userService','$http' ,'AppService','$scope', function($location, $cookies, $rootScope,userService,$http,AppService,$scope){
	
	$scope.hasPermission = function(permission){
		var roles = userService.get().roles;
		if(permission != "" && roles != undefined){
			return roles.indexOf(permission) != -1;
		}
		return false;
	}
	
	if($cookies.get("access_token") != undefined && $cookies.get("access_token")!= ""){
		$http.defaults.headers.common.Authorization = $cookies.get("access_token");
		var user = JSON.parse($cookies.get("user"));
		$rootScope.name = user.fullname;
		$rootScope.userId = user.username;
		userService.set(user);
		if($scope.hasPermission('VENDOR')){
			$location.path('/caseHistory');
		}
		else if($scope.hasPermission('USER_CASE')){
			
			AppService.getLateCaseCount().success(function(data){
				$rootScope.lateCaseCount = data.data.count;
			});
			
			if(!$location.path().includes("editcase")){
			$location.path('/caseHistory');
			}
		}
		else{
			$location.path('/product');
		}
	}
	else{
		$location.path('/login');
	}
	
}]);	


app.controller('headerController', function($location, $http, $rootScope ,$cookies,userService ,$scope, AppService){
	
	this.showConsignment = function(consignments){
		$location.path('/showBooking')
	}
	
	this.book = function(consignments){
		$location.path('/booking')
	}
	this.auditDetails = function(consignments){
		$scope.isCollapsed = true;
		$location.path('/audit')
	}
	this.transctionDetails = function(){
		$scope.isCollapsed = true;
		$location.path('/transction')
	}
	this.addCityVia = function(consignments){
		$location.path('/addCityVia')
	}
	this.addPerson = function(consignments){
		$location.path('/addPerson')
	}
	this.showPayment = function(consignments){
		$location.path('/showPayment')
	}
	this.showProduct = function(){
		$scope.isCollapsed = true;
		$location.path('/product')
	}
	this.login = function(){
		$location.path('/login')
	}
	this.bookCase = function(){
		AppService.clearCaseData();
		$location.path('/case')
	}
	this.ViewCaseHistory = function(){
		AppService.clearCaseFilter();
		$location.path('/caseHistory')
	}
	this.ViewLateCase = function(){
		$location.path('/lateCases')
	}
	this.ViewTodayCase = function(){
		$location.path('/caseHistory/todayCases')
	}
	this.showInventory = function(){
		$scope.isCollapsed = true;
		$location.path('/inventory')
	}
	this.logout = function(){
		$scope.isCollapsed = true;
		$http.defaults.headers.common.Authorization = "";
		$rootScope.name = ""
		userService.clear();
		$cookies.put("access_token","");
		$cookies.put("user", "");
		$location.path('/login')
	}
	this.hasPermission = function(permission){
		var roles = userService.get().roles;
		if(permission != "" && roles != undefined){
			return roles.indexOf(permission) != -1;
		}
		return false;
	}
	
	this.searchCase = function(opdNo){
		$location.path('/caseHistory/search/'+opdNo);
	}
	
	this.searchProduct = function(searchTxt){
		$location.path('/inventory/search/'+searchTxt);
	}
	
	this.openResetPassModel = function(){
		$('#resetPassword').modal('show');
	}
	
	this.resetPassword = function(currPass,newPass){
		var param = {};
		param["currPass"] = currPass;
		param["newPass"] = newPass;
		$http.post(weburl + "/password/reset", param).success(
				function(data, status) {
					if(data.success){
					$scope.addAlert('success', data.msg[0]);
					$('#resetPassword').modal('hide');
					}
					else{
						$scope.addAlert('warning', data.msg[0]);
					}
				}).error(function(data, status) {
			$scope.addAlert('warning', 'Please Try Again !!!');
		});
		
	}
	
	this.viewPendingCase = function(){
		$location.path('/caseHistory/pending')
	}
	
	this.viewNotifyCase = function(){
	$location.path('/caseHistory/notify')
	}
	 
	$scope.alerts = [];

	$scope.addAlert = function(type, messege) {
		$scope.alerts.push({
			type : type,
			msg : messege
		});
	};

	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	
	this.isActive = function (viewLocation) { 
        return viewLocation === $location.path();
    }
	
});

app.run(function($rootScope){
	  $rootScope._ = _;
});

app.factory('userService', function() {
	 var savedData = {}
	 function set(data) {
	   savedData = data;
	 }
	 function get() {
	  return savedData;
	 }
	 function clear() {
	 savedData = {};
	 }

	 return {
	  set: set,
	  get: get,
	  clear: clear
	 } 
});


app.factory('AppService', ['$rootScope', '$http', '$q', 'SpinnerService', function($rootScope, $http, $q, SpinnerService) {
	var caseData = undefined;
	var caseFilter = undefined;
	
	function setCaseData(data) {
		caseData = data;
	}
	function getCaseData() {
		  return caseData;
	}
	function clearCaseData() {
		caseData = undefined;
    }
	
	function setCaseFilter(data) {
		caseFilter = data;
	}
	function getCaseFilter() {
		  return caseFilter;
	}
	function clearCaseFilter() {
		caseFilter = undefined;
    }
	
	return{
		getLateCaseCount : function(){
			return $http.get(weburl + "/get/count/lateCase").success(function(data) {
				return data.data.count;
			});
		},
		
		setCaseData: setCaseData,
		getCaseData: getCaseData,
		clearCaseData:clearCaseData,
		setCaseFilter:setCaseFilter,
		getCaseFilter:getCaseFilter,
		clearCaseFilter:clearCaseFilter
	};
	
}]);

app.factory('myService', function() {
	 var savedData = {}
	 function set(data) {
	   savedData = data;
	 }
	 function get() {
	  return savedData;
	 }

	 return {
	  set: set,
	  get: get
	 }

	});

