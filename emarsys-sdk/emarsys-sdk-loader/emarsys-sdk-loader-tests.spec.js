const {describe, it, mock} = require('node:test');
const assert = require('node:assert');
const {pathToFileURL} = require('node:url');
const path = require('node:path');
const {JSDOM, ResourceLoader} = require('jsdom');

describe('emarsys-sdk-loader', function () {
    it('should register Emarsys API on window', async () => {
        const {window} = await createTestEnvironment();

        assert.notEqual(window.Emarsys, undefined);

        window.close()
    })

    it('should start the download of the EmarsysSdk by adding a new script tag to the head', async () => {
            const {loader, window} = await createTestEnvironment();

            assert.strictEqual(window.document.head.querySelectorAll('script').length, 2);

            assert.strictEqual(loader.requests.length, 2);
            assert.strictEqual(loader.requests[1].url.includes("emarsys-sdk-html.js"), true);

            window.close()
        }
    )
    it('should set emarsysSdkLoaded to true on window when sdk download finished', async () => {
            const {window} = await createTestEnvironment();

            assert.strictEqual(window.emarsysSdkLoaded, undefined);

            triggerSDKLoadedEvent(window)

            assert.strictEqual(window.emarsysSdkLoaded, true);

            window.close()
        }
    )
    it('should create all api segments on the Emarsys object', async () => {
            const apiSegments = ['setup', 'config', 'contact', 'event', 'push', 'deepLink', 'inApp', 'events']
            const {window} = await createTestEnvironment();

            apiSegments.forEach((segment) => {
                assert.equal(window.Emarsys[segment] !== undefined, true);
            })

            window.close()
        }
    )
    it('should gather calls before SDK is loaded and replay them to Emarsys after loading is done', async () => {
            const {window, mockDisableTracking} = await createTestEnvironment();

            await window.Emarsys.setup.disableTracking()

            triggerSDKLoadedEvent(window)

            assert.equal(mockDisableTracking.mock.callCount(), 1)

            window.close()
        }
    )
    it('should gather and replay calls with their parameters', async () => {
            const testPushToken = "testPushToken";
            const {window, mockRegisterPushToken} = await createTestEnvironment();

            await window.Emarsys.push.registerPushToken(testPushToken);

            triggerSDKLoadedEvent(window)

            assert.equal(mockRegisterPushToken.mock.callCount(), 1)
            assert.equal(mockRegisterPushToken.mock.calls[0].arguments[0], testPushToken);

            window.close()
        }
    )
    it('should include calls in the replay that arrive during the replay progress', async (testContext) => {
            const testPushToken = "testPushToken";
            const testEvent = "testEvent"
            const {
                window,
                mockRegisterPushToken,
                mockDisableTracking,
                mockTrackCustomEvent
            } = await createTestEnvironment();
            const spyGathererDisableTracking = testContext.mock.method(window.Emarsys.setup, "disableTracking")
            const spyGathererRegisterPushToken = testContext.mock.method(window.Emarsys.push, "registerPushToken")
            const spyGathererTrackCustomEvent = testContext.mock.method(window.Emarsys.event, "trackEvent")

            await window.Emarsys.setup.disableTracking();
            await window.Emarsys.push.registerPushToken(testPushToken);

            triggerSDKLoadedEvent(window)

            await window.Emarsys.event.trackEvent(testEvent);

            assert.equal(spyGathererDisableTracking.mock.callCount(), 1)
            assert.equal(mockDisableTracking.mock.callCount(), 1)

            assert.equal(spyGathererRegisterPushToken.mock.callCount(), 1)
            assert.equal(mockRegisterPushToken.mock.callCount(), 1)

            assert.equal(spyGathererTrackCustomEvent.mock.callCount(), 1)
            assert.equal(mockTrackCustomEvent.mock.callCount(), 1)
            assert.equal(mockTrackCustomEvent.mock.calls[0].arguments[0], testEvent);

            window.close()
        }
    )
    it('should delegate call to Emarsys after the sdk is loaded', async () => {
            const {window, mockDisableTracking} = await createTestEnvironment();

            triggerSDKLoadedEvent(window)

            await window.Emarsys.setup.disableTracking()

            assert.equal(mockDisableTracking.mock.callCount(), 1)

            window.close()
        }
    )
})

class SpyResourceLoader extends ResourceLoader {
    constructor() {
        super();
        this.requests = [];
    }

    fetch(url, options) {
        if (url.includes('emarsys-sdk-html.js')) {
            this.requests.push({url, options});
            return Promise.resolve();
        }
        this.requests.push({url, options});
        return super.fetch(url, options);
    }
}

function triggerSDKLoadedEvent(window) {
    const script = window.document.head.querySelectorAll('script')[1];
    script.dispatchEvent(new window.Event("load"));
}

async function createTestEnvironment() {
    const mockDisableTracking = mock.fn()
    const mockRegisterPushToken = mock.fn()
    const mockTrackCustomEvent = mock.fn()
    const baseURL = pathToFileURL(path.resolve('./')).href;
    const resourceLoader = new SpyResourceLoader()
    const loaderPath = path.resolve('./emarsys-sdk/src/jsHtml/resources/emarsys-sdk-loader.js');
    const dom = new JSDOM(`<!DOCTYPE html><html lang="en"><head><script src="${loaderPath}"></script>
</head></html>`, {
        url: baseURL,
        runScripts: "dangerously",
        resources: resourceLoader,
    });


    await new Promise((resolve) => {
        if (dom.window.document.readyState === 'complete') {
            resolve();
        } else {
            dom.window.document.addEventListener('DOMContentLoaded', resolve);
        }
    });

    const emarsysHtml = {
        setup: {disableTracking: mockDisableTracking},
        push: {registerPushToken: mockRegisterPushToken},
        event: {trackEvent: mockTrackCustomEvent},
        getInstance: () => emarsysHtml
    }

    dom.window["emarsys-sdk"] = {
        Emarsys: emarsysHtml,
    };

    return {
        loader: resourceLoader,
        window: dom.window,
        mockDisableTracking: mockDisableTracking,
        mockRegisterPushToken: mockRegisterPushToken,
        mockTrackCustomEvent: mockTrackCustomEvent
    };
}

