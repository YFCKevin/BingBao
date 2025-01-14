    // 語音功能 microphone
    // 初始化 SpeechRecognition
    const recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
    recognition.lang = 'zh-TW'; // 設置語言為中文（台灣）
    recognition.continuous = true; // 開啟連續識別模式
    recognition.interimResults = true; // 啟用中間結果

    let isRecording = false; // 是否正在錄音
    let mergedText = ""; // 追蹤 textarea 的最新內容
    let userInput = ""; // 儲存手動輸入的文字
    let isManualInput = false; // 判斷是否為手動輸入

    // 更新 textarea 顯示內容
    function updateTextareaContent(content) {
        const textarea = document.getElementById("userText");
        textarea.value = content;
    }

    // 當語音輸入返回結果時
    recognition.onresult = function (event) {
        if (isManualInput) return; // 如果是手動輸入，不處理語音輸入

        let interimTranscript = ''; // 中間結果
        let finalTranscript = ''; // 最終結果

        for (let i = event.resultIndex; i < event.results.length; i++) {
            const result = event.results[i];
            if (result.isFinal) {
                finalTranscript += result[0].transcript;
            } else {
                interimTranscript += result[0].transcript;
            }
        }

        // 更新 mergedText 並顯示
        mergedText += finalTranscript; // 添加最終結果
        updateTextareaContent(mergedText + interimTranscript); // 加上中間結果
    };

    // 當錄音停止時
    recognition.onend = function () {
        console.log("錄音結束");
        isRecording = false;
        updateMicrophoneStatus(false);
    };

    // 錯誤處理
    recognition.onerror = function (event) {
        console.error("語音識別錯誤: ", event.error);
    };

    // 開始錄音
    function startRecording() {
        isRecording = true;
        recognition.start();
        updateMicrophoneStatus(true);
        console.log("開始錄音...");
    }

    // 停止錄音
    function stopRecording() {
        isRecording = false;
        recognition.stop();
        updateMicrophoneStatus(false);
        console.log("停止錄音...");
    }

    // 更新麥克風icon樣式
    function updateMicrophoneStatus(isActive) {
        const microphoneIcon = document.getElementById("microphone");
        if (isActive) {
            microphoneIcon.classList.add("recording");
        } else {
            microphoneIcon.classList.remove("recording");
        }
    }

    // 監聽麥克風按鈕點擊
    document.addEventListener("DOMContentLoaded", function() {
        const microphoneIcon = document.getElementById("microphone");

        if (microphoneIcon) {
            microphoneIcon.addEventListener("click", () => {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            });
        }
    });

    // 監聽 textarea 手動輸入
    document.addEventListener("DOMContentLoaded", function() {
        const userTextElement = document.getElementById("userText");

        if (userTextElement) {
            userTextElement.addEventListener("input", (event) => {
                if (isRecording) {
                    stopRecording(); // 停止錄音
                }

                isManualInput = true; // 標記為手動輸入
                mergedText = event.target.value; // 更新最新內容
                setTimeout(() => {
                    isManualInput = false; // 延遲解除標記，防止語音輸入干擾
                }, 300);
            });
        }
    });