const {describe, it, mock} = require('node:test');
const assert = require('node:assert');
const {pathToFileURL} = require('node:url');
const path = require('node:path');
const {JSDOM, ResourceLoader} = require('jsdom');

describe('engagement-cloud-sdk-loader', function () {
    it('should register Engagement Cloud API on window', async () => {
        const {window} = await createTestEnvironment();

        assert.notEqual(window.EngagementCloud, undefined);

        window.close()
    })

    it('should start the download of the Engagement Cloud SDK by adding a new script tag to the head', async () => {
            const {loader, window} = await createTestEnvironment();

            assert.strictEqual(window.document.head.querySelectorAll('script').length, 2);

            assert.strictEqual(loader.requests.length, 2);
            assert.strictEqual(loader.requests[1].url.includes("engagement-cloud-sdk-html.js"), true);

            window.close()
        }
    )
    it('should set engagementCloudSdkLoaded to true on window when sdk download finished', async () => {
            const {window} = await createTestEnvironment();

            assert.strictEqual(window.engagementCloudSdkLoaded, undefined);

            triggerSDKLoadedEvent(window)

            assert.strictEqual(window.engagementCloudSdkLoaded, true);

            window.close()
        }
    )
    it('should create all api segments on the EngagementCloud object', async () => {
            const apiSegments = ['setup', 'config', 'contact', 'event', 'push', 'deepLink', 'events', 'embeddedMessaging']
            const {window} = await createTestEnvironment();

            apiSegments.forEach((segment) => {
                assert.equal(window.EngagementCloud[segment] !== undefined, true);
            })

            window.close()
        }
    )
    it('should gather calls before SDK is loaded and replay them to EngagementCloud after loading is done', async () => {
            const {window, mockDisable} = await createTestEnvironment();

            await window.EngagementCloud.setup.disable()

            triggerSDKLoadedEvent(window)

            assert.equal(mockDisable.mock.callCount(), 1)

            window.close()
        }
    )
    it('should gather and replay calls with their parameters', async () => {
            const testPushToken = "testPushToken";
            const {window, mockRegisterPushToken} = await createTestEnvironment();

            await window.EngagementCloud.push.registerPushToken(testPushToken);

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
                mockDisable,
                mockTrackCustomEvent
            } = await createTestEnvironment();
            const spyGathererDisable = testContext.mock.method(window.EngagementCloud.setup, "disable")
            const spyGathererRegisterPushToken = testContext.mock.method(window.EngagementCloud.push, "registerPushToken")
            const spyGathererTrackCustomEvent = testContext.mock.method(window.EngagementCloud.event, "trackEvent")

            await window.EngagementCloud.setup.disable();
            await window.EngagementCloud.push.registerPushToken(testPushToken);

            triggerSDKLoadedEvent(window)

            await window.EngagementCloud.event.trackEvent(testEvent);

            assert.equal(spyGathererDisable.mock.callCount(), 1)
            assert.equal(mockDisable.mock.callCount(), 1)

            assert.equal(spyGathererRegisterPushToken.mock.callCount(), 1)
            assert.equal(mockRegisterPushToken.mock.callCount(), 1)

            assert.equal(spyGathererTrackCustomEvent.mock.callCount(), 1)
            assert.equal(mockTrackCustomEvent.mock.callCount(), 1)
            assert.equal(mockTrackCustomEvent.mock.calls[0].arguments[0], testEvent);

            window.close()
        }
    )
    it('should delegate call to EngagementCloud after the sdk is loaded', async () => {
            const {window, mockDisable} = await createTestEnvironment();

            triggerSDKLoadedEvent(window)

            await window.EngagementCloud.setup.disable()

            assert.equal(mockDisable.mock.callCount(), 1)

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
        if (url.includes('engagement-cloud-sdk-html.js')) {
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
    const mockDisable = mock.fn()
    const mockRegisterPushToken = mock.fn()
    const mockTrackCustomEvent = mock.fn()
    const baseURL = pathToFileURL(path.resolve('./')).href;
    const resourceLoader = new SpyResourceLoader()
    const loaderPath = path.resolve('./engagement-cloud-sdk/src/jsHtml/resources/engagement-cloud-sdk-loader.js');
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

    const engagementCloudHtml = {
        ready: Promise.resolve(),
        setup: {disable: mockDisable},
        push: {registerPushToken: mockRegisterPushToken},
        event: {trackEvent: mockTrackCustomEvent},
        getInstance: () => engagementCloudHtml
    }

    dom.window["engagement-cloud-sdk"] = {
        EngagementCloud: engagementCloudHtml,
    };

    return {
        loader: resourceLoader,
        window: dom.window,
        mockDisable: mockDisable,
        mockRegisterPushToken: mockRegisterPushToken,
        mockTrackCustomEvent: mockTrackCustomEvent
    };
}

