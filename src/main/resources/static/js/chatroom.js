    function loadData_chatroom() {
      return {
        chatData: [],
        channel: "bingBao",
        memberId: "home",
        connectedMemberId: "",
        chatChannel: "",
        userText: "",
        isLoading: false,
        contextPath: "https://gurula.cc/chatbot/",

        init_chatroom() {
          let _this = this;

          let isChatboxOpen = false; // 預設關閉
          $("#chat-container").addClass("d-none");

          // 切換 chatbox
          function toggleChatbox() {
            $("#chat-container").toggleClass("d-none");
            isChatboxOpen = !isChatboxOpen;
          }

          // 綁定開啟/關閉按鈕事件
          $("#open-chat").on("click", toggleChatbox);
          $("#close-chat").on("click", toggleChatbox);

        },
        scrollToBottom (){
          let chatbox = $("#chatbox")[0];
          $("#chatbox").scrollTop(chatbox.scrollHeight);
        },
        connect(event) {
          let _this = this;

          if(this.connectedMemberId == "") {
            this.connectedMemberId = this.memberId;
            console.log(this.connectedMemberId);
          } else {
            return;
          }

          if(this.chatChannel == "") {
            $.ajax({
              url: this.contextPath + "ai/getChatChannel/" + this.channel + "/" + this.memberId,
              type: "get",
              dataType: "json",
              contentType: "application/json; charset=utf-8",
              success: function (response) {
                if (response.code == "C000"){
                  _this.chatChannel = response.data;
                  console.log(_this.chatChannel);
                  _this.loadChatHistory();
                  _this.initializeWebSocketConnection(event);
                } else {
                  return;
                }
              },
            });
          } else {
            this.initializeWebSocketConnection(event);
          }
          event.preventDefault();
        },
        initializeWebSocketConnection(event) {
          if (this.memberId) {
            var socket = new SockJS(this.contextPath + 'chatroom');
            stompClient = Stomp.over(socket);

            stompClient.heartbeat.outgoing = 3000; // 每 3 秒發送一次心跳
            stompClient.heartbeat.incoming = 3000; // 每 3 秒接收心跳

            stompClient.connect({}, (frame) => {
              console.log('連接成功: ' + frame);
              this.onConnected();
            }, (error) => {
              console.error('連接失敗: ' + error);
              alert('聊天室閒置過久，請重新整理畫面再次連線小助手！')
            });
          } else {
            popHint();
          }
        },
        onConnected (){
          // 訂閱
          stompClient.subscribe(`/${this.channel}/${this.memberId}/${this.chatChannel}`, this.onMessageReceived.bind(this));

          console.log("已成功訂閱聊天頻道");
        },
        sendMessage(event) {
          if (stompClient) {
            if (this.userText.trim() === '') return;

            this.chatData.push({ text: this.userText });
            this.isLoading = true;  // 發送後顯示 loading
            let chatMessage = {
              memberId: this.memberId,
              query: this.userText,
              chatChannel: this.chatChannel
            };

            stompClient.send("/ai/bingBao/chat", {}, JSON.stringify(chatMessage));
            this.userText = '';
          }
          event.preventDefault();
        },
        onMessageReceived(payload) {
          this.isLoading = false;  // 接收後隱藏 loading
          console.log("回覆訊息：" + payload.body)
          this.chatData.push({ text: marked.parse(payload.body) });
        },
        disconnect() {
          let _this = this;
          this.connectedMemberId = "";
          if (stompClient) {
            stompClient.disconnect(function() {
              console.log('斷開連接成功');
            });
          }
        },
        getCookie(name) {
          const value = `; ${document.cookie}`;
          const parts = value.split(`; ${name}=`);
          if (parts.length === 2) return decodeURIComponent(parts.pop().split(';').shift());
        },
        loadChatHistory() {
            let _this = this;
            let data = {
                projectName: 'BingBao',
                memberId: this.memberId,
                chatChannel: this.chatChannel
            };

            $.ajax({
                url: this.contextPath + "ai/chatHistory",
                type: "post",
                dataType: "json",
                data: JSON.stringify(data),
                contentType: "application/json; charset=utf-8",
                success: function (response) {
                    _this.chatData = response;
                    _this.chatData = _this.chatData.map(chat => {
                        return {
                            ...chat,
                            text: marked.parse(chat.text) // 將 Markdown 轉換為 HTML
                        };
                    });
                    console.log(_this.chatData);
                    _this.scrollToBottom();
                },
            });
        },
      }
    }