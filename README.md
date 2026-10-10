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

## 🚀 Android 開發者平台：修復與優化指南 (Android Developer Platform Best Practices)

依據 **Google AI 開發者平台與 Android 開發最佳實踐**，本專案已完成以下關鍵修復與優化：

### 1. Gemini API 模型多重容錯與備援機制 (Multi-Model Fallback Strategy)
- **問題防範**：針對模型版本迭代（如 `gemini-2.5-flash`、`gemini-2.0-flash`、`gemini-1.5-flash`、`gemini-1.5-pro`、`gemini-2.5-pro`），官方建議採用動態容錯重試鏈。
- **實作優化**：`GeminiService` 內建自動依序切換備援機制，當特定端點回傳 404 或逾時時，會自動嘗試下一候選模型，確保服務高可用性。

### 2. 雙軌離線臨床知識引擎 (Robust Offline Fallback)
- **穩定性提升**：若未設定 API Key 或網路異常，系統會無縫啟動內建的 **ClinicalKnowledgeEngine**，提供零延遲的離線分診與鑑別診斷指引，符合 Google Play 政策對穩定離線體驗的要求。

### 3. API Key 安全配置與動態切換
- **憑證管理**：支援從應用內設定面板（Settings）動態輸入或覆蓋 `GEMINI_API_KEY`，並透過環境變數安全注入，避免硬編碼資安風險。

### 4. 效能與程式碼分割優化 (Performance & Build Optimization)
- **Vite 產出最佳化**：已通過 `npm run build` 驗證，建置快速且穩定，模組化架構確保在行動裝置與網頁瀏覽器上皆能流暢運行。
