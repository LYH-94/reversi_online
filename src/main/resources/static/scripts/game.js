// 設置命名空間
var gameNamespace = gameNamespace || {};

var vue_game = null;

const url_backToLooby = "/reversi_online/backToLooby";

let position_array = "";
let position_arr = Array.from({ length: 8 }, () => Array(8).fill(' '));

function backToLooby(){
    // 使用 fetch API 獲取 HTML 内容
    fetch(url_backToLooby)
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

        // 切換回待機室 HTML 區塊，需重新掛載 vue 物件。
        vue_lobbyPageAPP_MountSwitch = true;

        document.getElementById('queueOrNot_button').innerHTML = "加入排隊";

        // 獲取 nick_name
        nick_name = getCookieValue("nick_name");
        document.getElementById("nickName").innerHTML = "嗨! " + nick_name;
        document.getElementById("chatNickname").innerHTML = nick_name + ":";

        // 使用 fetch API 發送「獲取所需數據」的請求，但後端不響應，由 WebSocket 來處理。
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

function game_move(position){
    // console.log(position);
    // console.log(typeof position);

    const message = JSON.stringify({
            type: "game_move",
            message: {
                game_id: variableForGameMove_gameId,
                position: position
            }
        });
    ws.send(message);
}

// Vue
gameNamespace.createMyApp = function(data) {
    vue_game = createApp({
        setup() {
    		let white_player = ref(data.white);
    		let black_player = ref(data.black);
    		let current_move = ref(data.current_move);

    		function renderGameData(gameData){
    		    white_player.value = gameData.white;
                black_player.value = gameData.black;
                current_move.value = gameData.current_move;
                settingBoard(gameData);
    		}

    		function settingBoard(gameData){
    		    // 獲取棋盤上每個位置的元素
                getBoardPositionElement();

                // 清空棋盤
                clearBoard();

                // 放上棋子
                placePieces(gameData);
            }

            return {
                white_player,
                black_player,
                current_move,
                renderGameData
            };
        }
    });
};

function getBoardPositionElement(){
    // 獲取棋盤上每個位置的元素
    for (let i = 0; i < 8; ++i) {
        for (let j = 0; j < 8; ++j) {
            position_arr[i][j] = document.getElementById("xy-" + i + j);
        }
    }
}

function clearBoard(){
    for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
            position_arr[i][j].innerHTML = '';
        }
    }
}

function placePieces(gameData){
    // 獲取 nick_name
    nick_name = getCookieValue("nick_name");

    //※(1=白、2=黑、3=黑子允許的位置、4=白子允許的位置)
    for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
            if(gameData.board_status[i][j] == 1){ // 白
                position_arr[i][j].innerHTML = '<img src="/reversi_online/images/white.svg">';
            }else if(gameData.board_status[i][j] == 2){ // 黑
                position_arr[i][j].innerHTML = '<img src="/reversi_online/images/black.svg">';
            }else if(nick_name == gameData.black && gameData.board_status[i][j] == 3){ // 允許的位置
                position_arr[i][j].innerHTML = '<img src="/reversi_online/images/allowPosition.svg">';
            }else if(nick_name == gameData.white && gameData.board_status[i][j] == 4){ // 允許的位置
                position_arr[i][j].innerHTML = '<img src="/reversi_online/images/allowPosition.svg">';
            }
        }
    }
}

function getCookieValue(name) {
    const cookies = document.cookie.split('; ');
    for (let cookie of cookies) {
        const [key, value] = cookie.split('=');
        if (key === name) {
            return decodeURIComponent(value);
        }
    }
    return null;
}