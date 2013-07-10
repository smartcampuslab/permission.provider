angular.module('admin', [ 'ngResource']);

/**
 * Main layout controller
 * @param $scope
 */
function AdminController($scope, $resource) {
	// error message
	$scope.error = '';
	// info message
	$scope.info = '';

	$scope.currentView = 'approvals';
	$scope.activeView = function(view) {
		return view == $scope.currentView ? 'active' : '';
	};
	$scope.signOut = function() {
	    window.document.location = "./admin/logout";
	};
	
	// resource reference for the app API
	var ClientApprovals = $resource('admin/approvals/:clientId', {}, {
		query : { method : 'GET' },
		approve : {method : 'POST'}
	});

	/**
	 * Initialize the app: load list of the developer's apps and reset views
	 */
	var init = function() {
		ClientApprovals.query(function(response){
			if (response.responseCode == 'OK') {
				$scope.error = '';
				$scope.approvals = response.data;
			} else {
				$scope.error = 'Failed to load approval requests: '+response.errorMessage;
			}	
		});
	};
	init();
	
	$scope.approve = function(clientId) {
		var newClient = new ClientApprovals();
		newClient.$approve({clientId:clientId},function(response){
			if (response.responseCode == 'OK') {
				$scope.error = '';
				$scope.info = 'Approved successfully';
				$scope.approvals = response.data;
			} else {
				$scope.error = 'Failed to approve resource access: '+response.errorMessage;
			}	
		});
	};
}