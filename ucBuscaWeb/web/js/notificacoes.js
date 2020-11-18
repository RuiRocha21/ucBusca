var websocket = null;


window.onload = function() { // URI = ws://10.16.0.165:8080/WebSocket/ws
    connect('ws://localhost/ucBusca/ws/');
};

function connect(host) { // connect to the host websocket
    if ('WebSocket' in window)
        websocket = new WebSocket(host);
    else if ('MozWebSocket' in window)
        websocket = new MozWebSocket(host);
    else {
        writeToHistory('Get a real browser which supports WebSocket.');
        return;
    }

    websocket.onclose   = onClose;
    websocket.onmessage = onMessage;
    websocket.onerror   = onError;
}


function onClose(event) {
    websocket.close();
    console.log("closing socket");
    websocket.close();
}



function onMessage(message) { // print the received message
    var msg = message.data;
    if (isNaN(msg.charAt(0))){
        console.log(msg);
    }else{
        alert(msg);
    }

}

function onError(event) {
    websocket.close();
    console.log('WebSocket error (' + event.data + ').');
    websocket.close();
}

function writeToHistory(text) {
    var history = document.getElementById('notificacoes');
    var line = document.createElement('p');
    line.style.wordWrap = 'break-word';
    line.innerHTML = text;
    history.appendChild(line);
    history.scrollTop = history.scrollHeight;
}



