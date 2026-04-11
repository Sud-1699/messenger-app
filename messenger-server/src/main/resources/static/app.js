const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8080/chat'
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/messages', (messages) => {
        showMessages(JSON.parse(messages.body));
    });
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
//    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#messages").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function send() {
    stompClient.publish({
        destination: "/app/send",
        body: JSON.stringify({
            "content": $("#message").val(),
            "sender": $("#userId").val(),
            "roomId": "room-1"
        })
    });

    $('#message').val("");
}

function showMessages(message) {
    $("#messages").append("<tr><td>" + message.sender + " -> " + message.content + "</td></tr>");
}

$(function () {
    connect();

    $("form").on('submit', (e) => e.preventDefault());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => send());
});