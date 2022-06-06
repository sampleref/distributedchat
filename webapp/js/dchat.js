var locationHostProtocol = window.location.protocol;
var wsProtocol = "ws";
if(locationHostProtocol == "https:"){
    wsProtocol = "wss";
}
var wsUri = wsProtocol + "://" + window.location.host + "/wsdchat";
var output;
var uname;
var pingReceived = true;
var websocket = null;

function watchConnection() {
    if (!pingReceived) {
        console.log("Ping timed out, closing websocket");
        closeWebSocket();
    }
    pingReceived = false;
    setTimeout(watchConnection, 15000);
}

function init() {
    output = document.getElementById("output");
    readName();
    launchWebSocket();
}

function readName() {
    uname = prompt("Please enter your name", "demouser");
    while (!uname || uname.trim() === "" || (uname.trim()).length === 0) {
        uname = prompt("Please enter your name", "demouser");
    }
    document.getElementById("uname").value = uname;
}

function closeWebSocket() {
    websocket.close();
}

function launchWebSocket() {
    if(websocket != null){
        writeToScreen('<span style="color: red;">ERROR **: ALREADY OPEN WEBSOCKET, Please disconnect and try again </span>');
        return;
    }
    uname = document.getElementById("uname").value;
    wsUriWithUName = wsUri + "?uname=" + uname;
    websocket = new WebSocket(wsUriWithUName);
    websocket.onopen = function (evt) {
        onOpen(evt)
    };
    websocket.onclose = function (evt) {
        onClose(evt)
    };
    websocket.onmessage = function (evt) {
        onMessage(evt)
    };
    websocket.onerror = function (evt) {
        onError(evt)
    };
}

function onOpen(evt) {
    writeToScreen("CONNECTED");
}

function onClose(evt) {
    websocket = null;
    writeToScreen("DISCONNECTED ");
}

function onMessage(evt) {
    var payload = JSON.parse(evt.data);
    switch (payload.typeHeader) {
        case "MESSAGE_LOG":
            if(payload.type == 'connection' && payload.data == 'ServerPing'){
                pingReceived = true;
            }
            if(payload.type == 'invaliduser'){
                writeToScreen('<span style="color: red;">ERROR **: ' + evt.data + '</span>');
            }
            if(payload.type == 'duplicateuser'){
                writeToScreen('<span style="color: red;">ERROR **: ' + evt.data + '</span>');
            }
            if(payload.type == 'invalidreceiver'){
                writeToScreen('<span style="color: red;">ERROR **: ' + evt.data + '</span>');
            }
            console.log(payload);
            break;
        case "MESSAGE_IGNORE":
            console.log(payload);
            break;
        case "MESSAGE_PAYLOAD":
            writeToScreen('<span style="color: blue;">Received Message <<: ' + evt.data + '</span>');
            break;
        case "MESSAGE_ACK":
            writeToScreen('<span style="color: green;">Received Ack !!: ' + evt.data + '</span>');
            break;
        default:
            console.error("Invalid message header " + payload.typeHeader);
    }

}

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR **: ' + evt.data + '</span>');
}

function doSend(message) {
    if(websocket == null){
        writeToScreen('<span style="color: red;">ERROR **: INVALID WEBSOCKET, Please connect and try again </span>');
        return;
    }
    websocket.send(message);
    writeToScreen('<span style="color: chartreuse;">SENT >>: ' + message + '</span>');
}

function sendMessagePayload(message) {
    var toAddress = document.getElementById("toaddress").value;
    messagePayload = {
        typeHeader : 'MESSAGE_PAYLOAD',
        uuid : randMillisString(),
        data : message,
        fromAddress : uname,
        toAddress : toAddress
    };
    doSend(JSON.stringify(messagePayload));
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    output.appendChild(pre);
}

function clearScreen() {
    while (output.firstChild) {
        output.removeChild(output.firstChild);
    }
}

window.addEventListener("load", init, false);
watchConnection();

function randMillisString(){
    var randStr = Math.random().toString(36).substring(2,7) + new Date().getTime();
    return randStr;
}
