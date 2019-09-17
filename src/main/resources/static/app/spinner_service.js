'use strict';

app.factory('SpinnerService', ['$uibModal', '$rootScope', function($uibModal, $rootScope) {

	var countF = 0;
	return {
	   startSpinner : function() {
		   var loaderGif = '<div class="loader-spinner"><img class="loader-spinner-img" alt="loader" src="/icons/loader.gif"></div>';
		   if (countF == 0) {
			   countF++;
			   return $uibModal.open({
			      animation : true,
			      template : loaderGif,
			      backdrop : 'static',
			      keyboard : false,
			   });
		   } else {
			   return null;
		   }
	   },
	   endSpinner : function(modal) {
		   if (modal) {
			   countF--;
			   modal.dismiss('cancel');
		   }
	   }
	};
}]);
