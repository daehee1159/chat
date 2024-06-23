<!doctype html>
<html lang="en">
<head>
    <title>Websocket ChatRoom</title>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="/webjars/bootstrap/4.3.1/dist/css/bootstrap.min.css">
    <style>
        [v-cloak] {
            display: none;
        }
    </style>
</head>
<body>
<div class="container" id="app" v-cloak>
    <div class="row">
        <div class="col-md-6">
            <h4>{{roomName}} <span class="badge badge-info badge-pill">{{userCount}}</span></h4>
        </div>
        <div class="col-md-6 text-right">
            <a class="btn btn-primary btn-sm" href="/logout">로그아웃</a>
            <a class="btn btn-info btn-sm" href="/chat/room">뒤로가기</a>
            <a class="btn btn-info btn-sm" href="/chat/room" @click.prevent="exitRoom()">채팅방 나가기</a>
        </div>
    </div>
    <div class="input-group">
        <div class="input-group-prepend">
            <label class="input-group-text">내용</label>
        </div>
        <input type="text" class="form-control" v-model="message" v-on:keypress.enter="sendMessage('TALK')">
        <div class="input-group-append">
            <button class="btn btn-primary" type="button" @click="sendMessage('TALK')">보내기</button>
        </div>
    </div>
    <ul class="list-group">
        <li class="list-group-item" v-for="message in messages">
            {{message.sender}} - {{message.message}}</a>
        </li>
    </ul>
</div>
<!-- JavaScript -->
<script src="/webjars/vue/2.5.16/dist/vue.min.js"></script>
<script src="/webjars/axios/0.21.1/dist/axios.min.js"></script>
<script src="/webjars/sockjs-client/1.5.1/sockjs.min.js"></script>
<script src="/webjars/stomp-websocket/2.3.4/stomp.min.js"></script>
<script>
	// websocket & stomp initialize
	var sock = new SockJS("/ws-stomp");
	var ws = Stomp.over(sock);
	// vue.js
	var vm = new Vue({
		el: '#app',
		data: {
			roomId: '',
			roomName: '',
            sender: '',
			message: '',
			messages: [],
			token: '',
			userCount: 0
		},
		created() {
			this.roomId = localStorage.getItem('wschat.roomId');
			this.roomName = localStorage.getItem('wschat.roomName');
			this.sender = localStorage.getItem('wschat.sender');
			var _this = this;
			ws.connect({"username":localStorage.getItem('wschat.sender')}, function(frame) {
				console.log("연결 성공");
				console.log(sock);
				ws.subscribe("/sub/chat/room/"+_this.roomId, function(message) {
					console.log("여긴됨?");
					var recv = JSON.parse(message.body);
					_this.recvMessage(recv);
					console.log("테스트");
					console.log(recv);
				}, {"username" : localStorage.getItem('wschat.sender')});
				console.log("여기는 구독정보");
				console.log(this.sender);
				console.log(ws.subscriptions);
				console.log(this.messages);
			}, function(error) {
				alert("서버 연결에 실패 하였습니다. 다시 접속해 주십시요.");
				location.href="/chat/room";
			})

			// axios.get('/chat/user').then(response => {
			// 	_this.token = response.data.token;
			// 	ws.connect({"token":_this.token}, function(frame) {
			// 		ws.subscribe("/sub/chat/room/"+_this.roomId, function(message) {
			// 			var recv = JSON.parse(message.body);
			// 			_this.recvMessage(recv);
			// 		});
			// 	}, function(error) {
			// 		alert("서버 연결에 실패 하였습니다. 다시 접속해 주십시요.");
			// 		location.href="/chat/room";
			// 	});
			// });
		},
		methods: {
			sendMessage: function(type) {
				const currentDateTime = new Date().toISOString().toString();
				ws.send("/pub/chat/message", {"token":this.token}, JSON.stringify({type:type, roomId:this.roomId, message:this.message, sender: this.sender, regDt: currentDateTime}));
				this.message = '';
			},
			recvMessage: function(recv) {
				console.log("여기는 recvMessage 메서드" + recv.sender);
				this.userCount = recv.userCount;
				this.messages.unshift({"type":recv.type,"sender":recv.sender,"message":recv.message, "regDt":recv.regDt})
			},
			exitRoom: function () {
				console.log("채팅방 나가기");
				console.log(this.roomId);
				console.log(localStorage.getItem('wschat.sender'));
				axios.post('/chat/room/exit', {
					roomId: this.roomId,
                    name: localStorage.getItem('wschat.sender')
                }).then(response => {
					console.log("서버 응답: ", response.data);
                }).catch(error => {
					console.error("에러 발생", error);
                })
            }
		}
	});
</script>
</body>
</html>
