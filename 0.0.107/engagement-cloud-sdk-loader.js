(function (global) {
  let sdkCore = null;
  let calls = [];
  let resolveLoaded;
  const sdkReady = new Promise((resolve) => (resolveLoaded = resolve));

  const currentSource = document.currentScript.src;
  const baseUrl = currentSource.substring(0, currentSource.lastIndexOf("/"));
  const sdkUrl = "https://emartech.github.io/engagement-cloud-sdk/latest/engagement-cloud-sdk.js";
//  const sdkUrl = `${baseUrl}/engagement-cloud-sdk-html.js`;

  global.EngagementCloud = {
    ready: sdkReady,
    setup: createApiSegment("setup", ["enable", "disable", "isEnabled", "setOnContactLinkingFailedCallback"]),
    config: createApiSegment("config", [
      "getApplicationCode",
      "getClientId",
      "getLanguageCode",
      "getApplicationVersion",
      "getSdkVersion",
      "getCurrentSdkState",
      "changeApplicationCode",
      "setLanguage",
      "resetLanguage",
      "getNotificationSettings",
    ]),
    contact: createApiSegment("contact", [
      "link",
      "linkAuthenticated",
      "unlink",
    ]),
    event: createApiSegment("event", ["trackNavigation", "trackEvent"]),
    push: createApiSegment("push", [
      "registerPushToken",
      "clearPushToken",
      "getPushToken",
    ]),
    deepLink: createApiSegment("deepLink", ["track"]),
    events: createApiSegment("events", [
      "once",
      "on",
      "off",
      "removeAllListeners",
    ]),
    embeddedMessaging: createApiSegment("embeddedMessaging", [
      "getCategories",
      "isUnreadFilterActive",
      "getActiveCategoryFilters",
      "filterUnreadOnly",
      "filterByCategories",
    ]),

    registerEventListener: createApiSegment(null, "registerEventListener"),
  };

  const scriptTag = document.createElement("script");
  scriptTag.src = sdkUrl;
  scriptTag.async = true;
  scriptTag.onload = async function () {
    sdkCore = global["engagement-cloud-sdk"].EngagementCloud.getInstance();
    global.engagementCloudSdkLoaded = true;
    resolveLoaded();
    await replayCalls();
  };
  scriptTag.onerror = function (error) {
    console.error("SDK loading failed. Error: ", error);
  };

  document.head.appendChild(scriptTag);

  function createApiMethods(apiSegment, methodName) {
    return async function () {
      let args = Array.prototype.slice.call(arguments);
      if (global.engagementCloudSdkLoaded && sdkCore && calls.length === 0) {
        const { target, apiMethod } = getTargetAndMethod(
          apiSegment,
          methodName,
        );
        return apiMethod.apply(target, args);
      } else {
        calls.push({
          apiSegment: apiSegment,
          method: methodName,
          args: args,
          timestamp: Date.now(),
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
        const { target, apiMethod } = getTargetAndMethod(
          call.apiSegment,
          call.method,
        );
        await apiMethod.apply(target, call.args);
      } catch (error) {
        console.error("Error replaying call:", call, error);
      }
    }
  }

  function getTargetAndMethod(apiSegment, methodName) {
    const target = apiSegment ? sdkCore[apiSegment] : sdkCore;
    const method = target[methodName];
    if (typeof method === "function") {
      return {
        target: apiSegment ? sdkCore[apiSegment] : sdkCore,
        apiMethod: method,
      };
    } else {
      console.error("No such method found: ", apiSegment + "." + methodName);
      return undefined;
    }
  }
})(window);
