$(document).ready(function(){
	var socket;
	$('#send_button').click(function(){
		var message = $('#send_text').val();
		console.log(message)
		if (!window.WebSocket) {
            return;
        }
        if (socket.readyState == WebSocket.OPEN) {
            socket.send(message);
        } else {
            alert("连接没有开启.");
        }
	});
	ws();
	function ws(){
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }
        if (window.WebSocket) {
            socket = new WebSocket("ws://localhost:8008/websocket");
            socket.onopen = function(event) {
                $('#response_text').val('连接开启!');
            };
            socket.onclose = function(event) {
                $('#response_text').val('连接关闭!');
            };
            socket.onmessage = function(event) {
            	console.log(event.data)
                $('#response_text').val($('#response_text').val() + '\n' + event.data);
            };
        } else {
            alert("你的浏览器不支持 WebSocket！");
        }
	}
});

