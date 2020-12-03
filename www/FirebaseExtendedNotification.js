var exec = require('cordova/exec');
module.exports = {
  saveRefreshToken: function(serverUrl, refreshToken, success, error) {
    console.log('saveRefreshToken called');
    exec(success, error, 'FirebaseExtendedNotification', 'saveRefreshToken', [
      serverUrl, refreshToken
    ]);
  },
  getLastNotificationTappedData: function(success, error){
    exec(success, error, 'FirebaseExtendedNotification', 'getLastNotificationTappedData', []);
  },
  closeNotification: function(notificationId, success, error){
    exec(success, error, 'FirebaseExtendedNotification', 'closeNotification', [notificationId]);
  },
  notificationExists: function(notificationId, success, error){
    exec(function(answer){ success(answer === 1); }, error,
      'FirebaseExtendedNotification', 'notificationExists', [notificationId]);
  },
  showNotification: function(dataToReturn, notificationOptions, success, error){
    exec(function(answer){ success(answer === 1); }, error,
      'FirebaseExtendedNotification', 'showNotification', [dataToReturn, notificationOptions]);
  },
  logEvent: function(name, params, success, error) {
    exec(success, error, 'FirebaseExtendedNotification', 'logEvent', [name, params]);
  },
  getReferrer: function(success, error) {
    exec(success, error, 'FirebaseExtendedNotification', 'getReferrer', []);
  },
  triggerReview: function(success, error) {
    exec(success, error, 'FirebaseExtendedNotification', 'triggerReview', []);
  },
};
