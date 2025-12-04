(function (global) {
    let sdkCore = null;
    let collectedCalls = [];

    global.Emarsys = {
            events: {},
            setup: {
                enableTracking: function (config) {
                    if (sdkCore) {
                        sdkCore.setup.enableTracking(config);
                    } else {
                        collectedCalls.push({api: 'setup', name: 'enableTracking', payload: config});
                    }
                }
            },
            config: {},
            contact: {},
            event: {},
            push: {},
            deepLink: {},
            inApp: {},
    }

    const scriptTag = document.createElement('script');
    scriptTag.src = "./emarsys-sdk.js";
    scriptTag.async = true;

    scriptTag.onload = function () {
        sdkCore = global["emarsys-sdk"].Emarsys
        window.alert("SDK loaded");
        collectedCalls.forEach(collectedCall => {
            //Re-emit events
            console.log(collectedCall);
        })
        collectedCalls = []
    }

    scriptTag.onerror = function (error) {
        console.error('SDK loading failed. Error: ', error);
    };

    document.head.appendChild(scriptTag);

})(window);
