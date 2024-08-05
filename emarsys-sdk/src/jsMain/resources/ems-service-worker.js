self.addEventListener('push', function (event) {
    event.waitUntil((async () => {
        return self.clients
        .matchAll({includeUncontrolled: true})
        .then(all => all.map(client => {
            client.postMessage({
            type: "emsPush",
            message: event.data.json()
        })}));
    })());
});
