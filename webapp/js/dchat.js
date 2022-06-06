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
var flowersdict = {};
var fruitsdict = {};
var colorsdict = {};

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
    populateStaticDicts();
    readName();
    launchWebSocket();
}

function readName() {
    uname = randName()
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

function randName(){
    var randFlower = Math.round(Math.random() * 13).toString();
    var randFruit = Math.round(Math.random() * 82).toString();
    var randColor = Math.round(Math.random() * 8).toString();
    return colorsdict[randColor]+ "_" + flowersdict[randFlower] + "_" + fruitsdict[randFruit];
}

function populateStaticDicts(){
    colorsdict = JSON.parse('{"0":"red", "1":"green", "2":"blue","3":"yellow","4":"white","5":"purple","6":"violet","7":"indigo"}');
    flowersdict = JSON.parse('{"0":"rose","1":"lily","2":"jasmine","3":"snapdragon","4":"orchid","5":"cherry","6":"blossom","7":"magnolia","8":"poppy","9":"sunflower","10":"hibiscus","11":"tulip","12":"daisy","13":"daffodils"}');
    fruitsdict = JSON.parse('{"0":"apple","1":"apricot","2":"avocado","3":"banana","4":"bilberry","5":"blackberry","6":"blackcurrant","7":"blueberry","8":"boysenberry","9":"currant","10":"cherry","11":"cherimoya","12":"cloudberry","13":"coconut","14":"cranberry","15":"cucumber","16":"damson","17":"date","18":"dragonfruit","19":"durian","20":"elderberry","21":"feijoa","22":"fig","23":"gooseberry","24":"grape","25":"raisin","26":"grapefruit","27":"guava","28":"honeyberry","29":"huckleberry","30":"jabuticaba","31":"jackfruit","32":"jambul","33":"jujube","34":"kiwano","35":"kiwifruit","36":"kumquat","37":"lemon","38":"lime","39":"loquat","40":"longan","41":"lychee","42":"mango","43":"mangosteen","44":"marionberry","45":"melon","46":"cantaloupe","47":"honeydew","48":"watermelon","49":"miracle fruit","50":"mulberry","51":"nectarine","52":"nance","53":"olive","54":"orange","55":"clementine","56":"mandarine","57":"tangerine","58":"papaya","59":"passionfruit","60":"peach","61":"pear","62":"persimmon","63":"physalis","64":"plantain","65":"plum","66":"prune","67":"pineapple","68":"plumcot","69":"pomegranate","70":"pomelo","71":"quince","72":"raspberry","73":"salmonberry","74":"rambutan","75":"redcurrant","76":"salak","77":"satsuma","78":"soursop","79":"strawberry","80":"tamarillo","81":"tamarind","82":"yuzu"}');
}

