// 設置命名空間
var lobbyNamespace = lobbyNamespace || {};

var vue_lobby = null;

function queueOrNot(){
    // console.log('進 if 前，queue:', queue);
    if(queue){
        queue = false;
        // console.log('進 if 後，queue:', queue);

        document.getElementById('queueOrNot_button').innerHTML = "排隊中";

        fetch(url_queue)
        .then(response => {
            if (!response.ok) {
              throw new Error('Network response was not ok');
            }
            return response.text();  // 將 response 轉成文字
        })
        .then(text => {
            console.log('收到的文字內容:', text);
        })
        .catch(error => {
            console.error('There has been a problem with your fetch operation:', error);
        });
    }else{
        queue = true;
        // console.log('進 if 後，queue:', queue);

        document.getElementById('queueOrNot_button').innerHTML = "加入排隊";

        fetch(url_cancelQueue)
        .then(response => {
            if (!response.ok) {
              throw new Error('Network response was not ok');
            }
            return response.text();  // 將 response 轉成文字
        })
        .then(text => {
            // console.log('收到的文字內容:', text);
        })
        .catch(error => {
            console.error('There has been a problem with your fetch operation:', error);
        });
    }
}

// Vue
lobbyNamespace.createMyApp = function(data) {
    vue_lobby = createApp({
        setup() {
    		let online_players = ref(data.online_players);
    		let lobby_players_list = ref(data.lobby_players_list);
    		let game_list = ref(data.game_list);

            function renderLobbyData(data){
    		    online_players.value = data.online_players;
    		    lobby_players_list.value = data.lobby_players_list;
    		    game_list.value = data.game_list;
    		}

    		function watch(url_watch){
    		    // console.log("url_getWatchPage:" + url_getWatchPage);
    		    // console.log("url_watch:" + url_watch);
                // 使用 fetch API 獲取 HTML 内容
                fetch(url_getWatchPage)
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
                    fetch(url_watch)
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

            return {
                online_players,
                lobby_players_list,
                game_list,
                renderLobbyData,
                watch
            };
        }
    });
};