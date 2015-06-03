
angular.module('openspecimen')
  .config(function($translatePartialLoaderProvider, $stateProvider) {
    $translatePartialLoaderProvider.addPart('custom-modules/sgh');

    $stateProvider
      .state('bulk-printing', {
        url: '/bulk-printing',
        templateUrl: 'custom-modules/sgh/biospecimen/bulk-printing.html',
        parent: 'signed-in',
        controller: 'sgh.CpBulkPrintingCtrl'
      });
  });
