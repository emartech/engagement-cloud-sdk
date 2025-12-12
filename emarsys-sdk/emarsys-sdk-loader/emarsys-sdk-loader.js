(function (global) {
    let sdkCore = null
    let calls = []

    const currentSource = document.currentScript.src
    const baseUrl = currentSource.substring(0, currentSource.lastIndexOf('/'));
    const sdkUrl = `${baseUrl}/emarsys-sdk-html.js`

    global.Emarsys = {
        setup: createApiSegment('setup', ['enableTracking', 'disableTracking', 'isEnabled']),
        config: createApiSegment('config', [
            'getApplicationCode',
            'getClientId',
            'getLanguageCode',
            'getApplicationVersion',
            'getSdkVersion',
            'getCurrentSdkState',
            'changeApplicationCode',
            'setLanguage',
            'resetLanguage',
            'getNotificationSettings']),
        contact: createApiSegment('contact', ['link', 'linkAuthenticated', 'unlink']),
        event: createApiSegment('event', ['track']),
        push: createApiSegment('push', ['registerPushToken', 'clearPushToken', 'getPushToken']),
        deepLink: createApiSegment('deepLink', ['track']),
        inApp: createApiSegment('inApp', ['pause', 'resume', 'isPaused']),
        events: createApiSegment('events', ['once', 'on', 'off', 'removeAllListeners']),

        registerEventListener: createApiSegment(null, 'registerEventListener'),
    }

    const scriptTag = document.createElement('script');
    scriptTag.src = sdkUrl;
    scriptTag.async = true;
    scriptTag.onload = async function () {
        sdkCore = global["emarsys-sdk"].Emarsys.getInstance();
        global.emarsysSdkLoaded = true
        await replayCalls()
    }
    scriptTag.onerror = function (error) {
        console.error('SDK loading failed. Error: ', error);
    };

    document.head.appendChild(scriptTag);

    function createApiMethods(apiSegment, methodName) {
        return async function () {
            let args = Array.prototype.slice.call(arguments);
            if (global.emarsysSdkLoaded && sdkCore && calls.length === 0) {
                const {target, apiMethod} = getTargetAndMethod(apiSegment, methodName)
                return apiMethod.apply(target, args);
            } else {
                calls.push({
                    apiSegment: apiSegment,
                    method: methodName,
                    args: args,
                    timestamp: Date.now()
                });
                return Promise.resolve();
            }
        };
    }

    function createApiSegment(apiSegment, methods) {
        let gatherer = {};
        for (let i = 0; i < methods.length; i++) {
            gatherer[methods[i]] = createApiMethods(apiSegment, methods[i]);
        }
        return gatherer;
    }

    async function replayCalls() {
        while (calls.length > 0) {
            const call = calls.shift();
            try {
                const {target, apiMethod} = getTargetAndMethod(call.apiSegment, call.method)
                await apiMethod.apply(target, call.args);
            } catch (error) {
                console.error('Error replaying call:', call, error);
            }
        }
    }

    function getTargetAndMethod(apiSegment, methodName) {
        const target = apiSegment ? sdkCore[apiSegment] : sdkCore;
        const method = target[methodName];
        if (typeof method === 'function') {
            return {target: apiSegment ? sdkCore[apiSegment] : sdkCore, apiMethod: method};
        } else {
            console.error('No such method found: ', apiSegment + '.' + methodName);
            return undefined;
        }
    }

})(window);
