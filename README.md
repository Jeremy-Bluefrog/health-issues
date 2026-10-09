# 症狀AI隨身問 (Symptom AI Triage & Inquiry)

輸入不舒服的症狀，運用智慧 AI 深入臨床分析，並透過多輪互動式追問釐清關鍵病徵，提供分診燈號、可能成因、就醫科別建議與急症危急指引。

---

## 📱 透過 GitHub 自動打包建立 APK (GitHub Actions)

本專案已配置完整的 **GitHub Actions CI/CD 自動構建工作流**（位於 `.github/workflows/build-apk.yml`），只需將程式碼推送至 GitHub，即可自動編譯並下載 Android APK！

### 步驟 1：推送專案至 GitHub
1. 在 AI Studio 點擊右上角設定選單中的 **「Push to GitHub」** 或 **「Export as ZIP」** 上傳至您的 GitHub 儲存庫。
2. 儲存庫分支包含 `main` 或 `master`。

### 步驟 2：設定 API Key（可選）
若希望 APK 打包時預先內嵌 Gemini API 金鑰：
1. 進入 GitHub 儲存庫頁面，點選 **「Settings」** ➔ **「Secrets and variables」** ➔ **「Actions」**。
2. 點選 **「New repository secret」**。
3. 名稱填寫：`GEMINI_API_KEY`，內容填入您的 Google AI Studio Gemini API Key。
*(若未設定，系統亦內建智慧臨床知識庫與離線分診邏輯，並可在 App 介面中自訂輸入)*

### 步驟 3：觸發並下載 APK
1. 在 GitHub 儲存庫頁面上方，點選 **「Actions」** 標籤頁。
2. 在左側工作流列表中點選 **「Build Android APK」**。
3. 點選右側 **「Run workflow」** 按鈕手動觸發（或每次推送程式碼至 `main` 分支時會自動觸發）。
4. 構建完成後（約需 2~4 分鐘），點擊進入該次運行的工作流詳情。
5. 在頁面底部的 **Artifacts (產出檔案)** 區塊中，點擊 **`SymptomAI-Debug-APK`** 即可下載安裝檔！

---

## 🛠 本地開發與手動編譯

### Android (Android Studio / Gradle)
- **環境需求**：JDK 21、Android SDK 36
- **編譯指令**：
  ```bash
  ./gradlew :app:assembleDebug
  ```
- **APK 輸出路徑**：
  `app/build/outputs/apk/debug/app-debug.apk`

### Web 網頁版 (Vite + React)
- **啟動開發伺服器**：
  ```bash
  npm run dev
  ```
- **網頁打包**：
  ```bash
  npm run build
  ```

---

## 🌟 核心特色
- **臨床智慧追問**：針對潛在疾病盲點（痛感性質、轉移部位、發作時程、反彈痛等）主動追問。
- **M3 簡潔美學**：依據 Material Design 3 規範，介面俐落專注無多餘雜訊。
- **雙端支援**：同時具備原生 Android Jetpack Compose 與跨平台 Web 即時體驗。
