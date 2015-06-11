(function() {

    var Cocoon = window.Cocoon;
    if (!Cocoon && window.cordova && typeof require !== 'undefined') {
        Cocoon = cordova.require('com.ludei.cocoon.common.Cocoon');
    }

    /**
    * @fileOverview
    <h2>About Atomic Plugins</h2>
    <p>Atomic Plugins provide an elegant and minimalist API and are designed with portability in mind from the beginning. Framework dependencies are avoided by design so the plugins can run on any platform and can be integrated with any app framework or game engine.
    <br/> <p>You can contribute and help to create more awesome plugins. </p>
    <h2>Atomic Plugins Share</h2>
    <p>This <a src="https://github.com/ludei/atomic-plugins-share">repository</a> contains a native Share API designed using the Atomic Plugins paradigm. The API is already available in many languagues and we plan to add more in the future.</p>
    <h3>Setup your project</h3>
    <p>Releases are deployed to Cordova Plugin Registry. 
    You only have to install the desired plugins using Cordova CLI, CocoonJS CLI or Cocoon.io Cloud Server.</p>
    <ul>
    <code>cordova plugin add com.ludei.share;</code><br/>
    </ul>
    <p>The following JavaScript files will be included in your html project by default during installation:</p>
    <ul>
    <li><a href="https://github.com/ludei/atomic-plugins-share/blob/master/src/js/cocoon_share.js">cocoon_share.js</a></li>
    <li><a href="https://github.com/ludei/cocoon-common/blob/master/src/js/cocoon.js">cocoon.js</a></li>
    </ul>
    <h3>Documentation</h3>
    <p>In this section you will find all the documentation you need for using this plugin in your Cordova project. 
    Select the specific namespace below to open the relevant documentation section:</p>
    <ul>
    <li><a href="http://ludei.github.io/cocoon-common/dist/doc/js/Cocoon.html">Cocoon</a></li>
    <li><a href="Cocoon.Share.html">Share</a></li>
    </ul>
    <h3>API Reference</h3>
    <p>For a complete project that tests all the features provided in the API run the following command:</p>
    <ul><code>gulp create-cordova</code></ul>
    <br/><p>We hope you find everything you need to get going here, but if you stumble on any problems with the docs or the plugins, 
    just drop us a line at our forum and we will do our best to help you out.</p>
    <h3>Tools</h3>
    <a href="http://support.ludei.com/hc/communities/public/topics"><img src="img/cocoon-tools-1.png" /></a>
    <a href="https://cocoon.io/doc"><img src="img/cocoon-tools-2.png" /></a>
    <a href="http://cocoon.io/"><img src="img/cocoon-tools-3.png" /></a>
    * @version 1.0
    */

    /**
     * Cocoon.Share class provides a multiplatform, easy to use native Share API. 
     * @namespace Cocoon.Share
     */
    Cocoon.define("Cocoon.Share", function(extension) {
        /**
         * Opens a given share native window to share some specific text content in
         * any system specific social sharing options. For example, Twitter, Facebook, SMS, Mail, ...
         * @memberOf Cocoon.Share
         * @function share
         * @property {object}  data               - The data to share
         * @property {string}  data.message       - The message that will be shared.
         * @property {string}  data.image         - The image that will be shared. It can be a URL or a base64 image.
         * @param callback {function} callback. The callback params called when share completed or dimissed. Params: activity, completed, error
         * @example
         * Cocoon.Share.share({
                message: "I have scored more points on Flappy Submarine!! Chooo choooo",
                image: "http://www.myserver.com/myimage.png"
            }, function(activity, completed, error){
                    console.log("Share " + completed ? 'Ok' : 'Failed');
            });
         */
        extension.share = function(data, callback) {
            callback = callback || function() {};
            Cocoon.exec('LDSharePlugin', 'share', [data], function(result) {
                callback(result[0], result[1], result[2]);
            }, function(data) {
                callback(result[0], result[1], result[2]);
            });
        };

        return extension;
    });

})();