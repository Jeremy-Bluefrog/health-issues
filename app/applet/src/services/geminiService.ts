import { AiAnalysisResult, InitialSymptomInput, TriageLevel } from '../types';
import { ClinicalKnowledgeEngine } from './clinicalKnowledge';

export class GeminiService {
  private static readonly MODEL = 'gemini-1.5-flash';
  private static readonly BASE_URL = 'https://generativelanguage.googleapis.com/v1beta/models/';

  static async analyze(
    input: InitialSymptomInput,
    history: Array<{ speaker: string; text: string }>,
    isFinal: boolean = false,
    round: number = 1,
    customApiKey?: string
  ): Promise<AiAnalysisResult> {
    const apiKey = (
      customApiKey ||
      (typeof import.meta !== 'undefined' && import.meta.env?.VITE_GEMINI_API_KEY) ||
      process.env.GEMINI_API_KEY ||
      ''
    ).trim();

    if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
      console.warn('Gemini API key is not configured, running smart Clinical Knowledge Engine');
      return ClinicalKnowledgeEngine.analyze(input, history, isFinal, round);
    }

    try {
      const systemInstruction = `你是一位專業、嚴謹且富同理心的醫療分診諮詢 AI 助手（非執業醫師）。請使用繁體中文（台灣醫學用語），為使用者輸入的症狀進行臨床邏輯分診與深度追問。

【任務要求】
1. 評估嚴重程度分級 (triageLevel)：只能是 "EMERGENCY" (急診紅燈)、"PROMPT" (儘速就醫橙燈)、"ROUTINE_CLINIC" (常規門診黃燈)、"SELF_CARE" (自我照護綠燈)。
2. 摘要 (summary)：以溫和、專業口吻簡述患者目前狀況與初步研判。
3. 潛在可能病因 (possibleCauses)：列出 2~4 項鑑別診斷探討，含名稱、可能性(如高/中/待排除)、說明、關鍵表現。
4. ${isFinal || round >= 3 ? '使用者已進入總結，followUpQuestions 提供 0 題或留空。' : '【重點：AI繼續追問】：請針對尚未明朗的臨床鑑別關鍵，提出 1 到 3 個精確的追問問題 (followUpQuestions)，每一題必須提供 3~4 個具體常見選項 (options) 供使用者點擊快速回答，並說明 purpose (追問目的)。'}
5. 建議就醫科別 (recommendedDepartments)：1~2 個科別及理由。
6. 紅旗警告 (redFlagWarnings)：若出現哪些警訊必須立即撥打119或衝急診。
7. 居家照護與舒緩建議 (homeCareAdvice)：安全、溫和的照護指引及「切勿做的事」(如未確診前忌盲目服用止痛藥掩蓋腹膜炎)。
8. 輸出格式：必須為標準合法的 JSON 字串，不得包含 markdown 標籤以外的多餘非 JSON 內容。

JSON結構規範：
{
  "triageLevel": "EMERGENCY" | "PROMPT" | "ROUTINE_CLINIC" | "SELF_CARE",
  "summary": "...",
  "possibleCauses": [
    {
      "name": "...",
      "likelihood": "...",
      "explanation": "...",
      "keySigns": ["...", "..."]
    }
  ],
  "followUpQuestions": [
    {
      "id": "q1",
      "question": "...",
      "purpose": "...",
      "options": ["...", "...", "..."]
    }
  ],
  "recommendedDepartments": [
    {
      "name": "...",
      "reason": "...",
      "urgency": "..."
    }
  ],
  "redFlagWarnings": ["...", "..."],
  "homeCareAdvice": ["...", "..."],
  "disclaimer": "本 AI 分析僅供就醫分診參考，無法取代醫師臨床診斷與治療。若症狀持續或劇烈惡化，請立即就醫。"
}`;

      const promptParts = [
        `【病患主訴基本資料】`,
        `- 症狀描述：${input.description}`,
        input.duration ? `- 持續時間：${input.duration}` : '',
        input.painLevel > 0 ? `- 疼痛程度：${input.painLevel} / 10` : '',
        `- 年齡族群：${input.ageGroup}`,
        input.chronicConditions ? `- 病史/用藥：${input.chronicConditions}` : '',
        history.length > 0 ? `\n【前後互動問答與追問記錄】\n` + history.map(h => `[${h.speaker}]: ${h.text}`).join('\n') : '',
        isFinal ? `\n【請產出最終完整綜合評估分析報告】` : `\n【請進行分診評估並提出精準的後續追問問題】`
      ].filter(Boolean).join('\n');

      const response = await fetch(`${this.BASE_URL}${this.MODEL}:generateContent?key=${apiKey}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ parts: [{ text: promptParts }] }],
          systemInstruction: { parts: [{ text: systemInstruction }] },
          generationConfig: {
            responseMimeType: 'application/json',
            temperature: 0.3,
          },
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error(`Gemini API error: ${response.status} ${response.statusText}`, errorText);
        // Fallback to clinical knowledge engine so the app never breaks
        return ClinicalKnowledgeEngine.analyze(input, history, isFinal, round);
      }

      const data = await response.json();
      const rawText = data?.candidates?.[0]?.content?.parts?.[0]?.text;
      if (!rawText) {
        return ClinicalKnowledgeEngine.analyze(input, history, isFinal, round);
      }

      const cleanJson = rawText.replace(/```json/g, '').replace(/```/g, '').trim();
      const parsed = JSON.parse(cleanJson);

      return {
        triageLevel: parsed.triageLevel || 'ROUTINE_CLINIC',
        summary: parsed.summary || '已收到您的症狀描述。',
        possibleCauses: Array.isArray(parsed.possibleCauses) ? parsed.possibleCauses : [],
        followUpQuestions: Array.isArray(parsed.followUpQuestions) ? parsed.followUpQuestions : [],
        recommendedDepartments: Array.isArray(parsed.recommendedDepartments) ? parsed.recommendedDepartments : [],
        redFlagWarnings: Array.isArray(parsed.redFlagWarnings) ? parsed.redFlagWarnings : [],
        homeCareAdvice: Array.isArray(parsed.homeCareAdvice) ? parsed.homeCareAdvice : [],
        disclaimer: parsed.disclaimer || '本 AI 評估為健康衛教參考，若症狀劇烈請儘速就醫。',
        isComprehensive: isFinal,
        inquiryRound: round,
      };
    } catch (err) {
      console.error('Failed to parse Gemini response, fallback to Clinical Engine:', err);
      return ClinicalKnowledgeEngine.analyze(input, history, isFinal, round);
    }
  }
}
