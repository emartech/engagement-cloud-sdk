(function (exports) {
  const state = {
    msgId: 1
  }

  const meActions = [ // todo order matters!
    {
      name: 'buttonClicked',
      attr: 'me-button-clicked',
      exec: function (buttonId, element, cb) {
        submitToPlatform('buttonClicked', { buttonId }, cb)
      }
    },
    {
      name: 'requestPushPermission',
      attr: 'me-request-push-permission',
      exec: function (params, element, cb) {
        submitToPlatform('requestPushPermission', {}, cb)
      }
    },
    {
      name: 'triggerAppEvent',
      attr: 'me-trigger-app-event',
      exec: function (params, element, cb) {
        submitToPlatform('triggerAppEvent', { name: params.type, payload: params.payload }, cb)
      },
      fnWrap: function (type, payload, cb) {
        this.exec({ type, payload }, undefined, cb)
      }
    },
    {
      name: 'triggerMEEvent',
      attr: 'me-trigger-event',
      exec: function (params, element, cb) {
        submitToPlatform('triggerMEEvent', { name: params.type, payload: params.payload }, cb)
      },
      fnWrap: function (type, payload, cb) {
        this.exec({ type, payload }, undefined, cb)
      }
    },
    {
      name: 'openExternalLink',
      attr: 'href',
      exec: function (url, element, cb) {
        if (url.slice(0, 4) !== 'http') {
          reportError(url + ' is an invalid URL')
          return cb()
        }
        submitToPlatform('openExternalLink', { url }, cb)
      }
    },
    {
      name: 'makeNetworkRequest',
      attr: 'me-make-network-request',
      exec: function (params, element, cb) {
        submitToPlatform('makeNetworkRequest', { request: params }, cb)
      },
      fnWrap: function (url, method, headers, payload, cb) {
        this.exec({ url, method, headers, payload }, undefined, cb)
      }
    },
    {
      name: 'close',
      attr: 'me-close',
      exec: function (enabled, element, cb) {
        if (enabled === false) (cb || noop)()
        else submitToPlatform('close', {}, cb)
      }
    },
    {
      name: 'gotoStep',
      attr: 'me-goto-step',
      exec: function (stepId, element, cb) {
        const hiddenClass = 'ems-hidden'
        const activeStepElement = document.querySelector('[me-step]:not(.' + hiddenClass + ')')
        const nextStepElement = document.querySelector('[me-step="' + stepId + '"]')

        activeStepElement
          ? activeStepElement.classList.add(hiddenClass)
          : reportError('Cannot find active step')
        nextStepElement
          ? nextStepElement.classList.remove(hiddenClass)
          : reportError('Cannot find next step with name "' + stepId + '"')

        cb()
      }
    },
    {
      name: 'copyToClipboard',
      attr: 'me-copy-to-clipboard',
      exec: function (text, element, cb) {
        if (typeof text === 'string' && text.length > 0) {
          submitToPlatform('copyToClipboard', { text }, cb)
          element.classList.add('ems-inapp-button--copied-to-clipboard')
        } else {
          reportError('parameter is not a string')
          return cb()
        }
      }
    }
  ]

  const eventCallbacks = []

  const meiam = {
    state,
    handleResponse: function (response) {
      if (eventCallbacks[response.id]) {
        eventCallbacks[response.id](response)
        delete eventCallbacks[response.id]
      }
    },
    send: function (method, message) {
      if (window.webkit) {
        window.webkit.messageHandlers[method].postMessage(message)
      } else if (window.Android) {
        window.Android[method](JSON.stringify(message))
      } else if (window.EMSInappWebBridge) {
        window.EMSInappWebBridge[method](JSON.stringify(message))
      }
    }
  }

  function submitToPlatform (method, message, cb) {
    message.id = (state.msgId++).toString()
    eventCallbacks[message.id] = cb || noop

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
        action.exec(payload, self, noop)
      } catch (e) {
        reportError(e)
      }
    })
  }

  function reportError (msg) {
    console.error(msg)
  }

  function noop () {}

  // proxy all actions on MEIAM
  meActions.forEach(function (meAction) {
    meiam[meAction.name] = meAction.fnWrap ? meAction.fnWrap.bind(meAction) : meAction.exec
  })

  exports.MEIAM = meiam

   if (window.EMSInappWebBridge) {
    const attrs = meActions.map(function (action) { return action.attr })
    document.querySelectorAll('[' + attrs.join('],[') + ']').forEach(function (el) {
         el.addEventListener('click', onClick.bind(el))
       })
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
  exports.onload = function () {
    // polyfill for NodeList.forEach
    // https://developer.mozilla.org/en-US/docs/Web/API/NodeList/forEach
    // https://emarsys.jira.com/browse/SUITEDEV-17354
    if (window.NodeList && !NodeList.prototype.forEach) {
      NodeList.prototype.forEach = Array.prototype.forEach
    }

    const attrs = meActions.map(function (action) { return action.attr })
    document.querySelectorAll('[' + attrs.join('],[') + ']').forEach(function (el) {
      el.addEventListener('click', onClick.bind(el))
    })

    // initialize steps
    document.querySelectorAll('[me-step]').forEach(function (el, idx) {
      if (idx > 0) {
        el.classList.add('ems-hidden')
      }
    })
  }
})(this)
