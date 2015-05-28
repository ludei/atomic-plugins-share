#Atomic Plugins Share

This repo contains Share APIs designed using the [Atomic Plugins](#about-atomic-plugins) paradigm. The API is available in JavaScript and we plan to add more languagues in the future:
 
  * [JavaScript API for Cordova or Cocoon based Apps](#javascript-api)

You can contribute and help to create more awesome plugins.

##About Atomic Plugins

Atomic Plugins provide an elegant and minimalist API and are designed with portability in mind from the beginning. Framework dependencies are avoided by design so the plugins can run on any platform and can be integrated with any app framework or game engine. 

#Provided APIs

* [JavaScript API](#javascript-api)
  * [API Reference](#api-reference)
  * [Introduction](#introduction)
  * [Setup your project](#setup-your-project)
  * [Example](#example-1)

##JavaScript API:

###API Reference

See [API Documentation](http://ludei.github.io/atomic-plugins-share/dist/doc/js/Cocoon.Share.html)

###Introduction 

Cocoon.Share class provides an easy to use API that can be used with different OSs.

###Setup your project

Releases are deployed to Cordova Plugin Registry. You only have to install the desired plugins using Cordova CLI, CocoonJS CLI or Ludei's Cocoon.io Cloud Server.

    cordova plugin add com.ludei.share;

The following JavaScript file is included automatically:

[`cocoon_share.js`](src/js/cocoon_share.js)

###Example

```javascript
	Cocoon.Share.share('Mortimer test', url, function(activity, completed, error){
		alert(JSON.stringify(arguments));
	});
```

#License

Mozilla Public License, version 2.0

Copyright (c) 2015 Ludei 

See [`MPL 2.0 License`](LICENSE)
