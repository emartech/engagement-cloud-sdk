package com.emarsys

internal object IframeBridgeV2 {
    const val IFRAME_BRIDGE_V2 = """<script>(function (exports) {
  /**
   * MessageChannel for iFrame communication
   * @type {MessagePort | null}
   */
  let messageChannel = null

  /**
   * Detect if running inside an iframe
   * If window.webkit or window.Android is present, we're in a WebView, not an iframe
   * note: If accessing window.top throws (sandboxed iframe), we're definitely in an iframe
   * @type {boolean}
   */
  let isIframe = false
  try {
    isIframe = (window.frameElement !== null || window.self !== window.top) && !window.webkit && !window.Android
  } catch (e) {
    isIframe = true
  }

  /**
  * NOTE: order matters for some actions (e.g. gotoStep and close)
  * because multiple attributes can be present on the same element
  * and they will be executed in the order defined here
  * e.g. gotoStep should be executed before close
  * to allow navigating to a step before closing the message
  */
  const meActions = [
    {
      name: 'InAppButtonClicked',
      attr: 'me-button-clicked',
      exec: function (buttonId, element) {
        submitToPlatform('inAppButtonClicked', { buttonId, reporting: JSON.stringify({ id: buttonId }) })
      }
    },
    {
      name: 'RequestPushPermission',
      attr: 'me-request-push-permission',
      exec: function (params, element) {
        submitToPlatform('RequestPushPermission', {})
      }
    },
    {
      name: 'MEAppEvent',
      attr: 'me-trigger-app-event',
      exec: function (params, element) {
        submitToPlatform('MEAppEvent', { name: params.type, payload: params.payload })
      },
      fnWrap: function (type, payload) {
        this.exec({ type, payload }, undefined)
      }
    },
    {
      name: 'MECustomEvent',
      attr: 'me-trigger-event',
      exec: function (params, element) {
        submitToPlatform('MECustomEvent', { name: params.type, payload: params.payload })
      },
      fnWrap: function (type, payload) {
        this.exec({ type, payload }, undefined)
      }
    },
    {
      name: 'OpenExternalUrl',
      attr: 'href',
      exec: function (url, element) {
        if (url.slice(0, 4) !== 'http') {
          reportError(url + ' is an invalid URL')
          return false
        }
        submitToPlatform('OpenExternalUrl', { url })
      }
    },
    {
      name: 'makeNetworkRequest',
      attr: 'me-make-network-request',
      exec: function (params, element) {
        submitToPlatform('makeNetworkRequest', { request: params })
      },
      fnWrap: function (url, method, headers, payload) {
        this.exec({ url, method, headers, payload }, undefined)
      }
    },
    {
      name: 'Dismiss',
      attr: 'me-close',
      exec: function (enabled, element) {
        if (enabled === false) return false
        else submitToPlatform('Dismiss', {})
      }
    },
    {
      name: 'gotoStep',
      attr: 'me-goto-step',
      exec: function (stepId, element) {
        const hiddenClass = 'ems-hidden'
        const activeStepElement = document.querySelector('[me-step]:not(.' + hiddenClass + ')')
        const nextStepElement = document.querySelector('[me-step="' + stepId + '"]')

        activeStepElement
          ? activeStepElement.classList.add(hiddenClass)
          : reportError('Cannot find active step')
        nextStepElement
          ? nextStepElement.classList.remove(hiddenClass)
          : reportError('Cannot find next step with name "' + stepId + '"')
      }
    },
    {
      name: 'CopyToClipboard',
      attr: 'me-copy-to-clipboard',
      exec: function (text, element) {
        if (typeof text === 'string' && text.length > 0) {
          submitToPlatform('copyToClipboard', { text })
          element.classList.add('ems-inapp-button--copied-to-clipboard')
        } else {
          reportError('parameter is not a string')
        }
      }
    }
  ]

  const meiam = {
    getSDKVersion () {
      const sdkVersionAttr = 'data-me-sdk-version'
      const element = document.querySelector('[' + sdkVersionAttr + ']')
      if (!element) {
        return undefined
      }
      const parsedVersion = element.getAttribute(sdkVersionAttr)
      return parsedVersion === 'ME-SDK-VERSION' ? undefined : parsedVersion
    },
    getCompatibilityVersion () {
      const compatibilityAttr = 'data-me-compatibility-version'
      const element = document.querySelector('[' + compatibilityAttr + ']')
      if (!element) {
        return undefined
      }
      return element.getAttribute(compatibilityAttr)
    },
    send: function (method, message) {
      if (window.webkit) {
        window.webkit.messageHandlers[method].postMessage(message)
        return
      }

      if (window.Android) {
        window.Android[method](JSON.stringify(message))
        return
      }

      // todo what if the messageChannel is not yet initialized?
      // should we queue messages until then and replay them once the port is available?
      if (messageChannel) {
        const sdkMessage = { ...message, type: method }
        messageChannel.postMessage(sdkMessage);
        return
      }
    }
  }

  // proxy all actions on MEIAM
  meActions.forEach(function (meAction) {
    meiam[meAction.name] = meAction.fnWrap ? meAction.fnWrap.bind(meAction) : meAction.exec
  })

  function submitToPlatform (method, message) {
    meiam.send(method, message)
  }

  function onClick (event) {
    const self = this

    const usedActions = meActions.filter(function (meAction) {
      return self.hasAttribute(meAction.attr)
    })

    event.preventDefault() // todo check if needed in every case or just for href (test with onclick js handler)

    usedActions.forEach(function (action) {
      let payload

      try {
        payload = JSON.parse(self.getAttribute(action.attr))
      } catch (e) {
        payload = self.getAttribute(action.attr)
      }

      try {
        action.exec(payload, self)
      } catch (e) {
        reportError(e)
      }
    })
  }

  function reportError (msg) {
    console.error(msg)
  }

  /**
   * Register event listeners on elements with ME attributes
   * checks if the element already has a listener to avoid multiple registrations
   */
  function registerEventListeners () {
    const attrs = meActions.map(function (action) { return action.attr })
    document.querySelectorAll('[' + attrs.join('],[') + ']').forEach(function (el) {
      if (!el._MEIAM_INIT) {
        el.addEventListener('click', onClick.bind(el))
        el._MEIAM_INIT = true
      }
    })
  }

  function initializeSteps () {
    document.querySelectorAll('[me-step]').forEach(function (el, idx) {
      if (idx > 0) {
        el.classList.add('ems-hidden')
      }
    })
  }

  /**
   * Initialize message channel for iFrame communication
   * Only runs if in an iframe context
   * Listens for 'INIT_MESSAGE_CHANNEL' message from parent to establish channel
   * and responds with 'connected' message once ready
   */
  function initIframeMessageChannel () {
    if (!isIframe) return

    window.addEventListener('message', (event) => {
      // Validate origin to prevent malicious messages
      // TODO: this will prevent cross-origin iframes from working
      if (event.origin !== window.location.origin) return

      if (event.data === 'INIT_MESSAGE_CHANNEL') {
        messageChannel = event.ports[0]
        messageChannel.postMessage('connected')

        startResizeReporting()
      }
    })
  }

  /**
   * Reports height changes to parent frame using ResizeObserver
   *
   * For same-origin iframes, directly resizes the iframe element
   * For cross-origin iframes, sends height updates to parent via messageChannel
   */
  function startResizeReporting () {
    if (!messageChannel) return

    reportHeight()

    const resizeObserver = new ResizeObserver(reportHeight)
    resizeObserver.observe(document.documentElement)
  }

  function reportHeight () {
    const height = Math.max(
      document.body.offsetHeight,
      document.documentElement.offsetHeight
    )

    if (window.frameElement) {
      // same-origin iframe can resize itself directly
      window.frameElement.style.height = height + 'px'
    } else {
      // cross-origin iframe reports height to parent
      messageChannel.postMessage({ type: 'resize', height })
    }
  }

  function executeInit () {
    // polyfill for NodeList.forEach
    // https://developer.mozilla.org/en-US/docs/Web/API/NodeList/forEach
    // https://emarsys.jira.com/browse/SUITEDEV-17354
    if (window.NodeList && !NodeList.prototype.forEach) {
      NodeList.prototype.forEach = Array.prototype.forEach
    }

    // Previously we used event delegation, where we registered a click event listener on the document
    // (git commit 7b5c4bf38c2992ea43bbeb91d88aeba0ca4ca3ac).
    // Unfortunately, that solution does not work on non-interactive elements like div/span on Ios
    // (https://developer.mozilla.org/en-US/docs/Web/Events/click#Safari_Mobile).
    // So now we directly register event listeners on elements that use our attributes.
    // This solution works if the dom has all the elements in place on load,
    // but will not work with dynamically created elements.
    // If we need to handle that case later, maybe we should revert to event delegation,
    // then we can generate a css rule for our attributes with `cursor: pointer` (just as mentioned on the link).
    registerEventListeners()
    initializeSteps()
    initIframeMessageChannel()
  }

  /**
   * Ensures SDK initialization happens only after the DOM is ready.
   * If DOM is already loaded, initializes immediately. (eg. script at end of <body>).
   * Otherwise, waits for DOMContentLoaded event. (eg. script in <head>)
   * This prevents errors when the script loads before DOM elements exist.
   */
  function initOnDomReadyState () {
    if (document.readyState === 'complete') {
      executeInit()
    } else {
      window.addEventListener('DOMContentLoaded', executeInit, { once: true })
    }
  }

  initOnDomReadyState()

  /**
   * Export to the global window scope
   * In WebViews, this allows the SDK to access the bridge via window.MEIAM
   * In iframes, this allows the parent frame to access the Bridge via iframe.contentWindow.MEIAM
   * In template this allows direct access to the Bridge eg.: <button onclick="MEIAM.gotoStep('step2')">Next</button>
   */
  exports.MEIAM = meiam
})(this)</script>"""

}