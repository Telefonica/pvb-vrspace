var parentWindow = window.opener;

class Message {
    constructor(type, body) {
        this.type = type;
        this.body = body;
    }
}

function sendMessage (windowObj, payload) {
    if(windowObj) {
        console.log("at sendMessage");
        console.log(payload);
        windowObj.postMessage(payload, "*");
    }
}

window.addEventListener("message", (e) => {
    console.log("at listener");
    console.log(e);

    if (e.data.type === 'token' && typeof(e.data.body) === "string") {
        localStorage.setItem('accessToken', e.data.body);
        window.location.replace('/babylon/avatar-selection.html');
    }
});

setTimeout(function () {
    console.log("at timeout");
    sendMessage(parentWindow, new Message("token", "true"));
}, 1000);
