# DokiDokiChat

心動聊(DokiDokiChat)是一款創新的應用程式，主旨是在通過量化和視覺話朋友之間的好感度，幫助使用者維持和提升他們與朋友的關係。在現今快節奏的社會中，我們往往會不小心忽略身邊重要的人。心動聊(DokiDokiChat)旨在提醒使用者這些關係的重要性，並鼓勵定期互動以促進更緊密的關係。

## 開發人員

- 01057132 柯欣辰
- 01057122 鄭旭佑

## Project Overview

在現代社會中，忙碌的生活節奏讓我們經常不經意地忽略了身邊那些重要的人。為了讓我們更加關注和珍惜這些寶貴的關係，我們開發了“心動聊”這款 app。

我們將“好感度”實體化，通過簡單的規則來量化朋友對你的好感度。每一次的聊天互動都會影響你們之間的好感度分數，提醒你與朋友保持聯繫的重要性。定期與朋友聊天，可以幫助你們維持和提升彼此的感情。

透過“心動聊”，你可以更清晰地看到哪些朋友可能因為你的忽視而疏遠，從而幫助你主動修復和加強這些關係。這樣，你將不會因為疏忽而感到遺憾，並且會更好地珍惜每一段友誼。

“心動聊”旨在讓每一位使用者感受到，身邊的每個朋友都是珍貴的。希望通過這款應用，大家能夠更加關注彼此，讓每一段感情都變得更加深厚和美好。

---

In modern society, the busy pace of life often leads us to unintentionally overlook the important people around us. To help us better focus on and cherish these valuable relationships, we developed the "DokiDokiChat" app.

We have made "affinity" tangible by using simple rules to quantify the goodwill your friends have towards you. Each interaction will affect the score of your relationship, reminding you of the importance of staying connected with friends. Regularly chatting with friends can help maintain and enhance your relationships.

Through "DokiDokiChat", you can clearly see which friends may have become distant due to your neglect, allowing you to proactively repair and strengthen these connections. This way, you won't feel regret from neglect, and you will better appreciate each friendship.

The goal of "DokiDokiChat" is to make every user realize that every friend around them is precious. We hope that through this app, everyone can pay more attention to each other, making every relationship deeper and more meaningful.

## Features

- **視覺化好感度**：量化並視覺話你與朋友之間的好感度。
- **定期互動**: 透過互動來增加朋友之間的好感度，注意如果長時間沒有傳訊息好感度將會減少。
- **互動聊天**：簡單易用的聊天介面，方便與朋友交流。
- **粉色主題**：使用粉色當專案的主要顏色，透過粉色與白色構成簡單的介面

## Tech Stack

- **Frontend**: Jetpack Compose (Android)
- **Backend**: Firebase Firestore, node.js

## Installation

1. **Clone the repository**

    ```bash
    git clone https://github.com/yourusername/DokiDokiChat.git
    cd DokiDokiChat
    ```

2. **Open the project in Android Studio**

   Open Android Studio, then go to `File -> Open` and select the project directory.

3. **Configure Firebase**

    - Go to the [Firebase Console](https://console.firebase.google.com/).
    - Create a new project or use an existing one.
    - Add an Android app to your project.
    - Follow the instructions to download the `google-services.json` file and place it in the `app/` directory of your Android project.
    - Connect your firebase project

4. **Run the app**

   在 Android Studio 中點擊 “Run” 按鈕或使用以下命令：
5. 
    ```bash
    ./gradlew installDebug
    ```

## Usage

1. **Sign Up / Login**

   用電子郵件和密碼註冊或登錄

2. **Add Friends**

   使用者可以通過搜索userID來加好友。

3. **Chat and Maintain Affinity**

   定期與朋友聊天以維持和增加好感度。


