//const ws = new WebSocket("ws://localhost:7777/reversi_online/ws");
const ws = new WebSocket("ws://linyaohua.ddns.net:7777/reversi_online/ws");
const url_getGamePage = "/reversi_online/pages/gamePage.html";
const url_getLobbyPage = "/reversi_online/pages/lobbyPage.html";
const url_getWatchPage = "/reversi_online/pages/gamePage.html"; // Watch 跟 Game 的頁面是一樣的，有需要在分開。

const url_getLobbyData = "/reversi_online/lobbyData";
const url_getGameData = "/reversi_online/gameData";

const url_queue = "/reversi_online/queue";
const url_cancelQueue = "/reversi_online/cancelQueue";

var queue = true; // 於 lobby.js 中用於切換排隊/取消排隊的變數。
var nick_name = null;

let variableForGameMove_gameId = ""; // 用於落子功能。

// Vue
const { createApp, ref } = Vue;
let vue_gameAPP = null;
let vue_gameAPP_MountSwitch = false;
let vue_lobbyPageAPP = null;
var vue_lobbyPageAPP_MountSwitch = false;

// ===== 心跳機制 ↓ =====
// 每隔 50 秒發送一次心跳訊息
const HEARTBEAT_INTERVAL = 50 * 1000; // 毫秒
let heartbeatTimer = null;

// 連線成功後開始心跳
ws.addEventListener("open", function () {
    // 啟動心跳機制
    heartbeatTimer = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
            const heartbeatMessage = JSON.stringify({ type: "ping" });
            ws.send(heartbeatMessage);
            // console.log("Heartbeat sent:", heartbeatMessage);
        }
    }, HEARTBEAT_INTERVAL);

    // console.log("WebSocket 心跳機制啟動");
});

// 關閉連線時清除心跳
ws.addEventListener("close", function () {
    clearInterval(heartbeatTimer);
    // console.log("WebSocket 心跳機制關閉");
});
// ===== 心跳機制 ↑ =====


ws.onopen = () => {
    // console.log("Connected to WebSocket server.");
};

ws.onmessage = (event) => {
    const dataObj = JSON.parse(event.data);
    // console.log("dataObj.type:", dataObj.type);
    // console.log("dataObj.message:", dataObj.message);

    switch (dataObj.type) {
        case "redirect_page:lobbyPage":
            // console.log("Switch Case:1");
            redirect_page_lobbyPage();
            break;
        case "redirect_page:gamePage":
            // console.log("Switch Case:2");
            redirect_page_gamePage(dataObj.message);
            break;
        case "render_data:LobbyData":
            // console.log("Switch Case:3");
            render_data_LobbyData(dataObj.message);
            break;
        case "render_data:GameData":
            // console.log("Switch Case:4");
            render_data_GameData(dataObj.message);
            break;
        case "render_data:ChatData":
            // console.log("Switch Case:5");
            render_data_ChatData(dataObj.message);
            break;
        default:
            // console.log("無效 Switch");
    }
};

ws.onclose = () => {
    // console.log("Disconnected from WebSocket server.");
};

function render_data_LobbyData(message){
    // console.log("要渲染待機室頁面的數據為：", message);
    // console.log("在線人數：", message.online_players);

    if(vue_lobbyPageAPP_MountSwitch == true){
        // 建立後掛載 Vue 物件
        lobbyNamespace.createMyApp(message);
        vue_lobbyPageAPP = vue_lobby.mount('#lobbyPage');

        // 關閉 vueMount，避免重複掛載。
        vue_lobbyPageAPP_MountSwitch = false;
    }

    vue_lobbyPageAPP.renderLobbyData(message);
}

function render_data_GameData(message){
    // console.log("要渲染對局頁面的數據為：", message);
    // console.log("message.game_id:", message.game_id)
    variableForGameMove_gameId = message.game_id;

    if(vue_gameAPP_MountSwitch == true){
        // 建立後掛載 Vue 物件
        gameNamespace.createMyApp(message);
        vue_gameAPP = vue_game.mount('#game');

        // 關閉 vueMount，避免重複掛載。
        vue_gameAPP_MountSwitch = false;
    }
    vue_gameAPP.renderGameData(message);
}

function redirect_page_lobbyPage(){
    // 使用 fetch API 獲取 HTML 内容
    fetch(url_getLobbyPage)
    .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(html => {
        // 將獲取到的 HTML 插入到頁面的元素中
        document.getElementById('replace').innerHTML = html;

        queue = true; // 每當頁面回到「待機室」時，重置為 true，避免排隊/取消排隊的邏輯順序錯誤。
        // console.log("加入待機室頁面區塊，queue 為: " + queue);

        // 切換掛載 vue 物件為 true。
        vue_lobbyPageAPP_MountSwitch = true;

        // 獲取 nick_name
        nick_name = getCookieValue("nick_name");
        document.getElementById("nickName").innerHTML = "嗨! " + nick_name;
        document.getElementById("chatNickname").innerHTML = nick_name + ":";

        // 使用 fetch API 發送「獲取所需數據」的請求，但後端不想應，由 WebSocket 來處理。
        fetch(url_getLobbyData)
        .then(response => {
            if (!response.ok) {
                // 若 HTTP 狀態碼不是 2xx，可以拋錯處理
                throw new Error('Request failed with status ' + response.status);
            }
        });

    })
    .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
    });
}

function redirect_page_gamePage(message){
    // 使用 fetch API 獲取 HTML 内容
    fetch(url_getGamePage)
    .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.text();
    })
    .then(html => {
        // 切換掛載 vue 物件為 true。
        vue_gameAPP_MountSwitch = true;

        // 將獲取到的 HTML 插入到頁面的元素中
        document.getElementById('replace').innerHTML = html;

        // 使用 fetch API 發送「獲取所需數據」的請求，但後端不響應，由 WebSocket 來處理。
        const messageObj = JSON.parse(message); // 有嵌套物件，所以要再轉換一次。
        fetch(url_getGameData + "/" + messageObj.reconnect)
        .then(response => {
            if (!response.ok) {
                // 若 HTTP 狀態碼不是 2xx，可以拋錯處理
                throw new Error('Request failed with status ' + response.status);
            }
        });
    })
    .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
    });
}

function render_data_ChatData(message){
    // console.log("發送聊天訊息的玩家：", message.player);
    // console.log("聊天訊息：", message.content);

    // 將獲取到的聊天訊息插入到 chatWindows 元素中。
    document.getElementById('chatWindows').innerHTML += '<span class="d-flex font_3 justify-content-left align-items-center" style="margin: 0px 10px 0px 10px;">' + message.player + '：' + message.content + '</span>';
}

function sendButton(){
    // 將聊天訊息元素的 id 於這裏綁定，避免待機室頁面載入完成前，lobby.js 找不到該元素。
    const input = document.getElementById("chatInput");
    const message = input.value.trim();

    // 獲取 nick_name
    nick_name = getCookieValue("nick_name");
    // console.log("nick_name: " + nick_name);

    if (message !== "") {
        // 發送訊息給 WebSocket
        ws.send(JSON.stringify({
        type: "chat_message",
        message: {
            player: nick_name,
            content: message
        }
        }));

        input.value = ""; // 清空輸入框
    }

    // 按 Enter 發送
    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            sendButton();
        }
    });
}