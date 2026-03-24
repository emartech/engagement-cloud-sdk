const {describe, it, mock} = require("node:test");
const assert = require("node:assert");
const {pathToFileURL} = require("node:url");
const path = require("node:path");
const {JSDOM, ResourceLoader} = require("jsdom");

const TEST_APP_CODE = "INS-P01-APP-ABCDE";

describe("engagement-cloud-sdk-loader", function () {
    it("should register Engagement Cloud API on window", async () => {
        const {window} = await createTestEnvironment();

        assert.notEqual(window.EngagementCloud, undefined);

        window.close();
    });
    it("should start the download of the Engagement Cloud SDK by adding a new script tag to the head", async () => {
        const {loader, window} = await createTestEnvironment();

        assert.strictEqual(
            window.document.head.querySelectorAll("script").length,
            2,
        );

        assert.strictEqual(loader.requests.length, 2);
        assert.strictEqual(
            loader.requests[1].url.includes("engagement-cloud-sdk.js"),
            true,
        );

        window.close();
    });
    it("should create all api segments on the EngagementCloud object", async () => {
        const apiSegments = [
            "setup",
            "config",
            "contact",
            "event",
            "push",
            "deepLink",
            "events",
            "embeddedMessaging",
        ];
        const {window} = await createTestEnvironment();

        apiSegments.forEach((segment) => {
            assert.equal(window.EngagementCloud[segment] !== undefined, true);
        });

        window.close();
    });
    it("should gather calls before SDK is loaded and replay them to EngagementCloud after loading is done", async () => {
        const {window, mockDisable} = await createTestEnvironment();

        const disablePromise = window.EngagementCloud.setup.disable();
        triggerSDKLoadedEvent(window);
        await disablePromise;

        assert.equal(mockDisable.mock.callCount(), 1);
        window.close();
    });
    it("should gather and replay calls with their parameters", async () => {
        const testPushToken = "testPushToken";
        const {window, mockSubscribe} = await createTestEnvironment();

        const subscribePromise = window.EngagementCloud.push.subscribe(testPushToken);
        triggerSDKLoadedEvent(window);
        await subscribePromise;

        assert.equal(mockSubscribe.mock.callCount(), 1);
        assert.equal(
            mockSubscribe.mock.calls[0].arguments[0],
            testPushToken,
        );

        window.close();
    });
    it("should return the values produced by replayed calls using promises", async () => {
        const {window, mockGetApplicationCode} = await createTestEnvironment();

        const appCodePromise = window.EngagementCloud.config.getApplicationCode();
        triggerSDKLoadedEvent(window);
        const appCode = await appCodePromise;

        assert.equal(mockGetApplicationCode.mock.callCount(), 1);
        assert.equal(
            appCode,
            TEST_APP_CODE,
        );

        window.close();
    });
    it("should reject when a replayed call rejects", async () => {
        const {window, mockGetApplicationCode} = await createTestEnvironment();
        const testError = new Error("test error");
        mockGetApplicationCode.mock.mockImplementation(async () => {
            throw testError;
        });

        const appCodePromise = window.EngagementCloud.config.getApplicationCode();
        // Prevent unhandled rejection before assert.rejects attaches its handler
        appCodePromise.catch(() => {
        });
        triggerSDKLoadedEvent(window);

        await assert.rejects(appCodePromise, (err) => {
            assert.strictEqual(err, testError);
            return true;
        });

        assert.equal(mockGetApplicationCode.mock.callCount(), 1);

        window.close();
    });
    it("should include calls in the replay that arrive during the replay progress", async (testContext) => {
        const testPushToken = "testPushToken";
        const testEvent = "testEvent";
        const {window, mockSubscribe, mockDisable, mockTrackCustomEvent} =
            await createTestEnvironment();
        const spyGathererDisable = testContext.mock.method(
            window.EngagementCloud.setup,
            "disable",
        );
        const spyGathererSubscribe = testContext.mock.method(
            window.EngagementCloud.push,
            "subscribe",
        );
        const spyGathererTrackCustomEvent = testContext.mock.method(
            window.EngagementCloud.event,
            "track",
        );

        const disablePromise = window.EngagementCloud.setup.disable();
        const subscribePromise = window.EngagementCloud.push.subscribe()
        triggerSDKLoadedEvent(window);
        const trackPromise = window.EngagementCloud.event.track(testEvent);
        await disablePromise;
        await subscribePromise;
        await trackPromise;

        assert.equal(spyGathererDisable.mock.callCount(), 1);
        assert.equal(mockDisable.mock.callCount(), 1);

        assert.equal(spyGathererSubscribe.mock.callCount(), 1);
        assert.equal(mockSubscribe.mock.callCount(), 1);

        assert.equal(spyGathererTrackCustomEvent.mock.callCount(), 1);
        assert.equal(mockTrackCustomEvent.mock.callCount(), 1);
        assert.equal(mockTrackCustomEvent.mock.calls[0].arguments[0], testEvent);

        window.close();
    });

    it("should delegate call to EngagementCloud after the sdk is loaded", async () => {
        const {window, mockDisable} = await createTestEnvironment();

        triggerSDKLoadedEvent(window);

        await window.EngagementCloud.setup.disable();

        assert.equal(mockDisable.mock.callCount(), 1);

        window.close();
    });
});

class SpyResourceLoader extends ResourceLoader {
    constructor() {
        super();
        this.requests = [];
    }

    fetch(url, options) {
        if (url.includes("engagement-cloud-sdk.js")) {
            this.requests.push({url, options});
            const neverResolvingPromise = new Promise(() => {
            });
            if (typeof neverResolvingPromise.abort === "undefined") neverResolvingPromise.abort = function () {
            };
            return neverResolvingPromise
        }
        this.requests.push({url, options});
        return super.fetch(url, options);
    }
}

function triggerSDKLoadedEvent(window) {
    const script = window.document.head.querySelectorAll("script")[1];
    script.dispatchEvent(new window.Event("load"));
}

async function createTestEnvironment() {
    const mockDisable = mock.fn(() => Promise.resolve());
    const mockSubscribe = mock.fn(() => Promise.resolve());
    const mockTrackCustomEvent = mock.fn(() => Promise.resolve());
    const mockGetApplicationCode = mock.fn(() => Promise.resolve(TEST_APP_CODE));
    const mockInitializeSdk = mock.fn(() => Promise.resolve());
    const baseURL = pathToFileURL(path.resolve("./")).href;
    const resourceLoader = new SpyResourceLoader();
    const loaderPath = path.resolve(
        "./engagement-cloud-sdk/src/jsHtml/resources/engagement-cloud-sdk-loader.js",
    );
    const dom = new JSDOM(
        `<!DOCTYPE html><html lang="en"><head><script src="${loaderPath}"></script>
</head></html>`,
        {
            url: baseURL,
            runScripts: "dangerously",
            resources: resourceLoader,
        },
    );

    await new Promise((resolve) => {
        if (dom.window.document.readyState === "complete") {
            resolve();
        } else {
            dom.window.document.addEventListener("DOMContentLoaded", resolve);
        }
    });

    const engagementCloudHtml = {
        ready: Promise.resolve(),
        setup: {disable: mockDisable},
        push: {subscribe: mockSubscribe},
        event: {track: mockTrackCustomEvent},
        config: {getApplicationCode: mockGetApplicationCode},
        getInstance: () => engagementCloudHtml,
        initializeSdk: mockInitializeSdk,
    }

    dom.window["engagement-cloud-sdk"] = {
        EngagementCloud: engagementCloudHtml,
    };

    return {
        loader: resourceLoader,
        window: dom.window,
        mockDisable: mockDisable,
        mockSubscribe: mockSubscribe,
        mockTrackCustomEvent: mockTrackCustomEvent,
        mockGetApplicationCode: mockGetApplicationCode
    };
}
