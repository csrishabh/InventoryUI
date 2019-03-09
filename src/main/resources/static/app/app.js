var app = angular.module('store',['ui.bootstrap','ngAnimate','angular-loading-bar' ,'ui.router','ngSanitize','ui.select2', 'ui.select','ngCsv', 'ngCookies','ngMdBadge','ngAria','ngMaterial']);
  // set a custom templ
var weburl = "https://spotliback.herokuapp.com";
//var weburl = "http://localhost:8080";
var UIUrl = "";

app.config(function($stateProvider, $urlRouterProvider ,$httpProvider) {
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
	
	$httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
	
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


app.controller('myctrl',['$location','$cookies','$rootScope','userService','$http', function($location, $cookies, $rootScope,userService,$http){
	
	if($cookies.get("access_token") != undefined && $cookies.get("access_token")!= ""){
		$http.defaults.headers.common.Authorization = $cookies.get("access_token");
		var user = JSON.parse($cookies.get("user"));
		$rootScope.name = user.fullname;
		userService.set(user);
		$location.path('/product');
	}
	else{
		$location.path('/login');
	}
	
}]);	


app.controller('headerController', function($location, $http, $rootScope ,$cookies,userService ,$scope){
	
	
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
});


/*$scope.generatePaymentReport = function(){
	var personId = $scope.person.selected.id;
		var type = 'ALL';
		if($scope.search.type != undefined){
		type = $scope.search.type;
		}
		var currDate = $filter('date')(new Date(), 'dd-MM-yyyy');
	
		var date1 = currDate;
		var date2 = currDate;
	
		if($scope.search.date1 != undefined){
		date1  = $filter('date')($scope.search.date1, 'dd-MM-yyyy');
		}
		if($scope.search.date2 != undefined){
			date2  = $filter('date')($scope.search.date2, 'dd-MM-yyyy');
		}
		
		if(date1 > date2){
		$scope.addAlert('warning', 'FROM date can not be less greater then TO date');
		}
		else{
			var url = weburl+"/rest/payment/report?"+"id="+ personId+ "&type=" + type +"&date1=" + date1 + "&date2=" + date2;
			$http.get(url, { responseType: "arraybuffer" }).success(function(data){
				saveAs(new Blob([data],{type:"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}), "PaymentSheet.xlsx");
			})
		}
}*/


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

