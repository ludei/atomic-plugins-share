(function() {

    var Cocoon = window.Cocoon;
    if (!Cocoon && window.cordova && typeof require !== 'undefined') {
        Cocoon = cordova.require('com.ludei.cocoon.common.Cocoon');
    }

    /**
    * Opens a given share native window to share some specific text content in 
    * any system specific social sharing options. For example, Twitter, Facebook, SMS, Mail, ...
    * @memberOf Cocoon.Share
    * @function share
    * @param text {string} text The text content that will be shared.
    * @param image {string} image The image that will be shared. It can be a URL or a base64 image.
    * @param callback {function} callback. The callback params called when share completed or dimissed. Params: activity, completed, error
    * @example
    * Cocoon.Social.share("I have scored more points on Flappy Submarine!! Chooo choooo");
    */
    Cocoon.define("Cocoon.Share", function(extension) {

        extension.share = function(text, image, callback) {
            callback = callback || function(){};
            Cocoon.exec('LDSharePlugin', 'share', [text, image], function(data){
                callback(data[0], data[1], data[2]);
            }, function(data) {
                callback(data[0], data[1], data[2]);
            });
        };

        return extension;
    });

})();