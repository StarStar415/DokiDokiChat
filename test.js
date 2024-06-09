require('dotenv').config();
const express = require('express');
const axios = require('axios');
const admin = require('firebase-admin');
const app = express();

// 初始化Firebase Admin SDK
const serviceAccount = require('./dokidokichat-4cd64-firebase-adminsdk-dvrdp-a5d8eefdbc.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const url = `http://localhost:${process.env.PORT||3000}/decreaseFavor`;

// 檢查當前時間是否在晚上十點到早上九點之間
function isWithinNoPenaltyTime() {
    const now = new Date();
    const hours = now.getHours();
    return (hours > 22 || hours < 9);
}

// 定義減少好感度的API端點
app.post('/decreaseFavor', async (req, res) => {
    try {
        if (isWithinNoPenaltyTime()) {
            console.log('Within no penalty time. No favor will be decreased.');
            return res.send('Within no penalty time. No favor will be decreased.');
        }

        const usersRef = db.collection('user');
        const snapshot = await usersRef.get();
        const batch = db.batch();

        snapshot.forEach(doc => {
            const data = doc.data();
            if (Array.isArray(data.friends)) {
                const updatedFriends = data.friends.map(friend => {
                    const hasSentMsg = friend.hasSentMsg || false;

                    // 僅當未傳訊息時才扣除 favor
                    if (!hasSentMsg) {
                        return {
                            ...friend,
                            favor: Math.max((friend.favor || 0) - 1, 0),
                            hasSentMsg: false // 重置為未傳訊息
                        };
                    } else {
                        // 重置為未傳訊息
                        return {
                            ...friend,
                            hasSentMsg: false
                        };
                    }
                });

                batch.update(doc.ref, { friends: updatedFriends });
            }
        });

        await batch.commit();
        res.send('Favor decreased for all friends who did not send a message.');
    } catch (error) {
        console.error('Error decreasing Favor:', error);
        res.status(500).send('Error decreasing Favor');
    }
});

// 設置在整點觸發的函數
function setHourlyTimer() {
    // 計算到下一個整點的時間
    const now = new Date();
    const delay = (60 - now.getMinutes()) * 60 * 1000 - now.getSeconds() * 1000 - now.getMilliseconds();

    // 在下一個整點執行一次，然後每小時執行一次
    setTimeout(() => {
        sendPostRequest();
        setInterval(sendPostRequest, 3600000); // 每小時執行一次
    }, delay);
}

// 發送POST請求的函數
function sendPostRequest() {
    axios.post(url)
        .then(response => {
            console.log(`POST request sent successfully: ${response.status}`);
        })
        .catch(error => {
            console.error(`Error sending POST request: ${error}`);
        });
}

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
// 啟動定時器
setHourlyTimer();