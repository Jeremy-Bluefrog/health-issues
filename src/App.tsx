import React, { useState, useEffect, useRef } from 'react';
import {
  AiAnalysisResult,
  ChatMessage,
  ConsultationHistoryItem,
  InitialSymptomInput,
} from './types';
import { GeminiService } from './services/geminiService';
import { FollowUpCard } from './components/FollowUpCard';
import { AnalysisDetailCard } from './components/AnalysisDetailCard';
import { RedFlagGuide } from './components/RedFlagGuide';
import { HistoryDrawer } from './components/HistoryDrawer';
import {
  Activity,
  AlertTriangle,
  ArrowRight,
  ChevronDown,
  ChevronUp,
  FileText,
  HeartPulse,
  History,
  Key,
  MessageSquare,
  Plus,
  RefreshCw,
  Send,
  ShieldCheck,
  Sliders,
  Sparkles,
  Stethoscope,
  User,
} from 'lucide-react';

const QUICK_TAGS = [
  '右下腹疼痛',
  '單側搏動頭痛畏光',
  '胸口重壓悶痛',
  '發燒畏寒肌肉酸痛',
  '喉嚨痛劇烈乾咳',
  '天旋地轉頭暈噁心',
  '胃酸倒流火燒心',
  '嘔吐腹瀉食慾差',
];

const DURATION_OPTS = ['今天開始', '1~2天', '3~5天', '超過一週'];
const AGE_GROUPS = ['兒童', '青年', '成人', '長者'];

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'consult' | 'history' | 'guide'>('consult');

  // Input State
  const [input, setInput] = useState<InitialSymptomInput>({
    description: '',
    duration: '',
    painLevel: 0,
    ageGroup: '成人 (18~64歲)',
    chronicConditions: '',
  });

  const [showAdvanced, setShowAdvanced] = useState(false);
  const [customApiKey, setCustomApiKey] = useState(() => localStorage.getItem('user_gemini_api_key') || '');
  const [selectedModel, setSelectedModel] = useState(() => localStorage.getItem('selected_gemini_model') || 'gemini-2.5-flash');
  const [showKeyModal, setShowKeyModal] = useState(false);

  // Session State
  const [isSessionActive, setIsSessionActive] = useState(false);
  const [sessionId, setSessionId] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [inquiryRound, setInquiryRound] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // History State
  const [history, setHistory] = useState<ConsultationHistoryItem[]>(() => {
    try {
      const saved = localStorage.getItem('consultation_history');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    try {
      localStorage.setItem('consultation_history', JSON.stringify(history));
    } catch (e) {
      console.error('Failed to save history', e);
    }
  }, [history]);

  useEffect(() => {
    if (isSessionActive) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, isAnalyzing, isSessionActive]);

  const handleStartConsultation = async () => {
    if (!input.description.trim()) {
      setErrorMessage('請先輸入您感到身體不適的症狀描述');
      return;
    }
    setErrorMessage(null);

    const newId = Date.now().toString();
    setSessionId(newId);
    setIsSessionActive(true);
    setInquiryRound(1);

    const initialUserMsg: ChatMessage = {
      id: 'msg_init',
      sender: 'USER',
      timestamp: Date.now(),
      text: `${input.description}${input.duration ? ` (持續：${input.duration})` : ''}${
        input.painLevel > 0 ? ` (疼痛：${input.painLevel}/10)` : ''
      }${input.chronicConditions ? ` (病史：${input.chronicConditions})` : ''}`,
    };

    setMessages([initialUserMsg]);
    setIsAnalyzing(true);

    try {
      const result = await GeminiService.analyze(input, [], false, 1, customApiKey, selectedModel);
      const aiMsg: ChatMessage = {
        id: `ai_${Date.now()}`,
        sender: 'AI',
        timestamp: Date.now(),
        text: result.summary,
        analysisResult: result,
      };

      setMessages((prev) => [...prev, aiMsg]);
      saveToHistory(newId, input.description, result);
    } catch (err) {
      setErrorMessage('分析過程發生問題，請重試');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleAnswerFollowUp = async (questionId: string, questionText: string, answer: string) => {
    if (!answer.trim() || isAnalyzing) return;

    const nextRound = inquiryRound + 1;
    setInquiryRound(nextRound);

    const userReplyMsg: ChatMessage = {
      id: `user_${Date.now()}`,
      sender: 'USER',
      timestamp: Date.now(),
      text: `【回答追問】${questionText} ➔ ${answer}`,
      answeredQuestionId: questionId,
      selectedOption: answer,
    };

    const updated = [...messages, userReplyMsg];
    setMessages(updated);
    setIsAnalyzing(true);

    try {
      const historyTuples = updated.map((m) => ({
        speaker: m.sender === 'USER' ? '病患' : 'AI分診',
        text: m.text,
      }));

      const result = await GeminiService.analyze(input, historyTuples, false, nextRound, customApiKey, selectedModel);
      const aiMsg: ChatMessage = {
        id: `ai_${Date.now()}`,
        sender: 'AI',
        timestamp: Date.now(),
        text: result.summary,
        analysisResult: result,
      };

      setMessages((prev) => [...prev, aiMsg]);
      saveToHistory(sessionId, input.description, result);
    } catch (err) {
      setErrorMessage('追問分析發生狀況，請重試');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleRequestFinalReport = async () => {
    if (isAnalyzing) return;
    const nextRound = inquiryRound + 1;
    setInquiryRound(nextRound);

    const userReqMsg: ChatMessage = {
      id: `user_final_${Date.now()}`,
      sender: 'USER',
      timestamp: Date.now(),
      text: '請為我產出【最終完整綜合評估報告】',
    };

    const updated = [...messages, userReqMsg];
    setMessages(updated);
    setIsAnalyzing(true);

    try {
      const historyTuples = updated.map((m) => ({
        speaker: m.sender === 'USER' ? '病患' : 'AI分診',
        text: m.text,
      }));

      const finalResult = await GeminiService.analyze(input, historyTuples, true, nextRound, customApiKey, selectedModel);
      const aiMsg: ChatMessage = {
        id: `ai_final_${Date.now()}`,
        sender: 'AI',
        timestamp: Date.now(),
        text: '已彙整所有症狀與問答細節，以下為【最終綜合分診評估報告】',
        analysisResult: finalResult,
      };

      setMessages((prev) => [...prev, aiMsg]);
      saveToHistory(sessionId, input.description, finalResult);
    } catch (err) {
      setErrorMessage('產出最終報告失敗，請重試');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleReset = () => {
    setIsSessionActive(false);
    setMessages([]);
    setInquiryRound(0);
    setErrorMessage(null);
    setInput({
      description: '',
      duration: '',
      painLevel: 0,
      ageGroup: '成人 (18~64歲)',
      chronicConditions: '',
    });
  };

  const saveToHistory = (sId: string, initialDesc: string, res: AiAnalysisResult) => {
    const item: ConsultationHistoryItem = {
      id: sId,
      createdAt: Date.now(),
      initialSymptom: initialDesc,
      triageLevel: res.triageLevel,
      summary: res.summary,
      causesSummary: res.possibleCauses?.map((c) => c.name).join('、') || '',
      deptSummary: res.recommendedDepartments?.map((d) => d.name).join('、') || '',
      fullAnalysis: res,
    };

    setHistory((prev) => {
      const filtered = prev.filter((h) => h.id !== sId);
      return [item, ...filtered];
    });
  };

  const handleDeleteHistory = (id: string) => {
    setHistory((prev) => prev.filter((h) => h.id !== id));
  };

  const handleClearHistory = () => {
    setHistory([]);
  };

  const getPainDescriptor = (val: number) => {
    if (val === 0) return '完全無痛';
    if (val <= 2) return '輕微隱痛';
    if (val <= 4) return '輕中度 (需分散注意)';
    if (val <= 6) return '中度疼痛 (影響日常工作)';
    if (val <= 8) return '劇烈疼痛 (難以入眠)';
    return '極度劇痛 (無法忍受)';
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#f2f5f6] text-[#191c1d]">
      {/* Material 3 Top App Bar */}
      <header className="sticky top-0 z-30 bg-[#f8fafb]/95 backdrop-blur-md border-b border-[#bfc8ca]/40 shadow-xs">
        <div className="max-w-4xl mx-auto px-4 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-full bg-[#cde7ec] text-[#006874] flex items-center justify-center shadow-xs">
              <Stethoscope className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center space-x-2">
                <h1 className="font-bold text-[#191c1d] text-base sm:text-lg tracking-tight">
                  症狀AI隨身問
                </h1>
                <span className="text-[11px] font-bold px-2.5 py-0.5 rounded-full bg-[#9eeffd] text-[#001f24]">
                  Material 3
                </span>
              </div>
              <p className="text-xs text-[#3f484a] hidden sm:block">
                智慧臨床分診 • 深度多輪追問 • 科別指引
              </p>
            </div>
          </div>

          {/* M3 Navigation Bar with Pill Indicators */}
          <nav className="flex items-center space-x-1 sm:space-x-1.5 p-1 bg-[#eceff0] rounded-full border border-[#bfc8ca]/30">
            <button
              onClick={() => setActiveTab('consult')}
              className={`h-9 px-3.5 sm:px-4 rounded-full text-xs font-bold transition-all flex items-center m3-ripple ${
                activeTab === 'consult'
                  ? 'bg-[#006874] text-white shadow-xs'
                  : 'text-[#3f484a] hover:bg-[#e0e3e4]'
              }`}
            >
              <MessageSquare className="w-3.5 h-3.5 mr-1.5" />
              症狀諮詢
              {isSessionActive && (
                <span className="ml-1.5 px-1.5 py-0.2 rounded-full bg-[#9eeffd] text-[#001f24] text-[10px]">
                  {inquiryRound}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('history')}
              className={`h-9 px-3.5 sm:px-4 rounded-full text-xs font-bold transition-all flex items-center m3-ripple ${
                activeTab === 'history'
                  ? 'bg-[#006874] text-white shadow-xs'
                  : 'text-[#3f484a] hover:bg-[#e0e3e4]'
              }`}
            >
              <History className="w-3.5 h-3.5 mr-1.5" />
              問診紀錄
              {history.length > 0 && (
                <span className="ml-1.5 px-1.5 py-0.2 rounded-full bg-[#cde7ec] text-[#001f24] text-[10px]">
                  {history.length}
                </span>
              )}
            </button>

            <button
              onClick={() => setActiveTab('guide')}
              className={`h-9 px-3.5 sm:px-4 rounded-full text-xs font-bold transition-all flex items-center m3-ripple ${
                activeTab === 'guide'
                  ? 'bg-[#ba1a1a] text-white shadow-xs'
                  : 'text-[#ba1a1a] hover:bg-[#ffdad6]/60'
              }`}
            >
              <HeartPulse className="w-3.5 h-3.5 mr-1.5" />
              急症指南
            </button>

            <button
              onClick={() => setShowKeyModal(true)}
              title="API 金鑰設定"
              className="w-9 h-9 rounded-full flex items-center justify-center text-[#3f484a] hover:bg-[#e0e3e4] transition-colors"
            >
              <Key className="w-4 h-4" />
            </button>
          </nav>
        </div>
      </header>

      {/* Main Content Container */}
      <main className="flex-1 max-w-4xl w-full mx-auto p-4 sm:p-6">
        {activeTab === 'guide' && <RedFlagGuide />}

        {activeTab === 'history' && (
          <HistoryDrawer
            history={history}
            onDelete={handleDeleteHistory}
            onClearAll={handleClearHistory}
            onClose={() => setActiveTab('consult')}
          />
        )}

        {activeTab === 'consult' && (
          <>
            {!isSessionActive ? (
              /* Sleek Minimalist Consultation Form */
              <div className="space-y-4 max-w-2xl mx-auto animate-fade-in">
                {/* Concise Header */}
                <div className="text-center pt-1 pb-1 space-y-1">
                  <h2 className="text-xl sm:text-2xl font-extrabold text-[#191c1d] tracking-tight">
                    您哪裡感到不舒服？
                  </h2>
                  <p className="text-xs sm:text-sm text-[#3f484a]">
                    輸入主要症狀，AI 即刻分析並主動提出關鍵追問
                  </p>
                  <div className="pt-1">
                    <button
                      type="button"
                      onClick={() => setShowKeyModal(true)}
                      className="inline-flex items-center text-xs font-medium text-[#006874] bg-[#cde7ec]/60 hover:bg-[#cde7ec] px-3 py-1 rounded-full transition-colors"
                    >
                      <Key className="w-3.5 h-3.5 mr-1" />
                      點此設定您的專屬 Gemini API Key（可選）
                    </button>
                  </div>
                </div>

                {/* Main Card */}
                <div className="bg-white rounded-[24px] border border-[#bfc8ca]/50 shadow-xs p-5 sm:p-7 space-y-4">
                  <div>
                    <textarea
                      value={input.description}
                      onChange={(e) => {
                        setInput({ ...input, description: e.target.value });
                        setErrorMessage(null);
                      }}
                      placeholder="簡述不適症狀（例如：右下腹持續悶痛、稍微發燒，走路震動時更痛...）"
                      rows={3}
                      className="w-full px-4 py-3 rounded-[16px] bg-[#f8fafb] border border-[#6f797a]/60 focus:outline-none focus:ring-2 focus:ring-[#006874] focus:border-transparent text-sm leading-relaxed text-[#191c1d] placeholder-[#6f797a] transition-all resize-none"
                    />
                    {errorMessage && (
                      <p className="text-xs text-[#ba1a1a] font-semibold mt-1 flex items-center">
                        <AlertTriangle className="w-3.5 h-3.5 mr-1" />
                        {errorMessage}
                      </p>
                    )}
                  </div>

                  {/* Clean Quick Chips */}
                  <div>
                    <div className="flex flex-wrap gap-1.5">
                      {QUICK_TAGS.map((tag, idx) => (
                        <button
                          key={idx}
                          type="button"
                          onClick={() => {
                            const cur = input.description.trim();
                            setInput({
                              ...input,
                              description: cur ? `${cur}、${tag}` : tag,
                            });
                          }}
                          className="px-3 py-1 rounded-full text-xs font-medium bg-[#f2f5f6] hover:bg-[#cde7ec] hover:text-[#001f24] border border-[#bfc8ca]/60 text-[#3f484a] transition-all active:scale-[0.98] m3-ripple"
                        >
                          + {tag}
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Collapsible Optional Factors */}
                  <div className="pt-1 border-t border-[#eceff0]">
                    <button
                      type="button"
                      onClick={() => setShowAdvanced(!showAdvanced)}
                      className="flex items-center justify-between w-full text-xs font-semibold text-[#006874] hover:text-[#00505a] py-1.5 transition-colors"
                    >
                      <span className="flex items-center">
                        <Sliders className="w-3.5 h-3.5 mr-1.5" />
                        更多線索：時長、疼痛分數、年齡（選填）
                      </span>
                      {showAdvanced ? (
                        <ChevronUp className="w-4 h-4" />
                      ) : (
                        <ChevronDown className="w-4 h-4" />
                      )}
                    </button>

                    {showAdvanced && (
                      <div className="mt-2.5 p-4 rounded-[18px] bg-[#f2f5f6] border border-[#bfc8ca]/40 space-y-3.5 animate-fade-in text-xs sm:text-sm">
                        {/* Duration */}
                        <div>
                          <span className="block font-semibold text-[#191c1d] mb-1.5 text-xs">
                            持續時間：
                          </span>
                          <div className="flex flex-wrap gap-1.5">
                            {DURATION_OPTS.map((dur, dIdx) => (
                              <button
                                key={dIdx}
                                type="button"
                                onClick={() =>
                                  setInput({
                                    ...input,
                                    duration: input.duration === dur ? '' : dur,
                                  })
                                }
                                className={`px-3 py-1 rounded-full border text-xs font-medium transition-all ${
                                  input.duration === dur
                                    ? 'bg-[#006874] text-white border-[#006874]'
                                    : 'bg-white text-[#3f484a] border-[#bfc8ca]/60 hover:bg-[#e0e3e4]'
                                }`}
                              >
                                {dur}
                              </button>
                            ))}
                          </div>
                        </div>

                        {/* Pain Scale */}
                        <div>
                          <div className="flex justify-between items-center mb-1 text-xs">
                            <span className="font-semibold text-[#191c1d]">疼痛程度：</span>
                            <span className="font-bold text-[#006874]">
                              {input.painLevel} 分 ({getPainDescriptor(input.painLevel)})
                            </span>
                          </div>
                          <input
                            type="range"
                            min="0"
                            max="10"
                            value={input.painLevel}
                            onChange={(e) =>
                              setInput({ ...input, painLevel: parseInt(e.target.value) })
                            }
                            className="w-full accent-[#006874] cursor-pointer h-1.5 bg-[#dbe4e6] rounded-lg"
                          />
                        </div>

                        {/* Age Group */}
                        <div>
                          <span className="block font-semibold text-[#191c1d] mb-1.5 text-xs">
                            年齡：
                          </span>
                          <div className="flex flex-wrap gap-1.5">
                            {AGE_GROUPS.map((age, aIdx) => (
                              <button
                                key={aIdx}
                                type="button"
                                onClick={() => setInput({ ...input, ageGroup: age })}
                                className={`px-3 py-1 rounded-full border text-xs font-medium transition-all ${
                                  input.ageGroup === age
                                    ? 'bg-[#006874] text-white border-[#006874]'
                                    : 'bg-white text-[#3f484a] border-[#bfc8ca]/60 hover:bg-[#e0e3e4]'
                                }`}
                              >
                                {age}
                              </button>
                            ))}
                          </div>
                        </div>

                        {/* Chronic Conditions */}
                        <div>
                          <label className="block font-semibold text-[#191c1d] mb-1 text-xs">
                            病史 / 用藥（選填）：
                          </label>
                          <input
                            type="text"
                            value={input.chronicConditions}
                            onChange={(e) =>
                              setInput({ ...input, chronicConditions: e.target.value })
                            }
                            placeholder="如：高血壓、糖尿病、氣喘、服用阿斯匹靈..."
                            className="w-full px-3 py-1.5 rounded-[12px] border border-[#6f797a]/60 text-xs bg-white text-[#191c1d] focus:outline-none focus:ring-2 focus:ring-[#006874]"
                          />
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Action CTA Button */}
                  <button
                    type="button"
                    onClick={handleStartConsultation}
                    className="w-full h-12 rounded-full bg-[#006874] hover:bg-[#00505a] active:scale-[0.99] text-white font-bold text-sm sm:text-base shadow-sm transition-all flex items-center justify-center space-x-2 m3-ripple"
                  >
                    <span>開始分析與追問</span>
                    <ArrowRight className="w-4 h-4" />
                  </button>

                  <p className="text-[11px] text-[#6f797a] text-center">
                    🛡️ 僅供急門診分流參考，若出現意識改變或劇烈劇痛請立即就醫
                  </p>
                </div>
              </div>
            ) : (
              /* M3 Active Chat & Inquiry Flow */
              <div className="space-y-4 max-w-3xl mx-auto">
                {/* M3 Active Session Control Bar */}
                <div className="p-4 rounded-[24px] bg-white border border-[#bfc8ca]/50 shadow-xs flex items-center justify-between">
                  <div className="flex items-center space-x-2.5">
                    <span className="w-3 h-3 rounded-full bg-[#006874] animate-pulse" />
                    <span className="text-xs sm:text-sm font-bold text-[#191c1d]">
                      第 {inquiryRound} 輪問診中
                    </span>
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      type="button"
                      disabled={isAnalyzing}
                      onClick={handleRequestFinalReport}
                      className="px-4 py-2 rounded-full bg-[#dae2ff] hover:bg-[#c2d0ff] text-[#0e1a37] text-xs font-bold transition-all disabled:opacity-50 flex items-center shadow-2xs m3-ripple"
                    >
                      <FileText className="w-3.5 h-3.5 mr-1" />
                      產出最終綜合報告
                    </button>

                    <button
                      type="button"
                      onClick={handleReset}
                      className="p-2 rounded-full text-[#3f484a] hover:bg-[#eceff0] transition-colors"
                      title="重啟新諮詢"
                    >
                      <RefreshCw className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                {/* M3 Message Bubbles & Detail Cards */}
                <div className="space-y-4">
                  {messages.map((msg, idx) => {
                    const isLatestAi = msg.sender === 'AI' && idx === messages.length - 1;

                    if (msg.sender === 'USER') {
                      return (
                        <div key={msg.id} className="flex justify-end animate-fade-in">
                          <div className="max-w-[85%] bg-[#006874] text-white rounded-[24px] rounded-br-[6px] p-4 text-sm shadow-xs">
                            <div className="flex items-start space-x-2">
                              <User className="w-4 h-4 mt-0.5 flex-shrink-0 opacity-80" />
                              <div className="leading-relaxed">{msg.text}</div>
                            </div>
                          </div>
                        </div>
                      );
                    }

                    return (
                      <div key={msg.id} className="space-y-3 animate-fade-in">
                        {msg.analysisResult && (
                          <AnalysisDetailCard analysis={msg.analysisResult} />
                        )}

                        {/* M3 Follow-up Questions Section */}
                        {msg.analysisResult?.followUpQuestions &&
                          msg.analysisResult.followUpQuestions.length > 0 &&
                          !msg.analysisResult.isComprehensive && (
                            <div className="space-y-3 pt-1">
                              <div className="flex items-center text-xs font-bold text-[#006874] bg-[#cde7ec]/50 px-3.5 py-2 rounded-full border border-[#cde7ec]">
                                <Sparkles className="w-3.5 h-3.5 mr-1.5 text-[#006874]" />
                                為提供更精確鑑別，AI 正在針對重要臨床細節向您追問：
                              </div>

                              {msg.analysisResult.followUpQuestions.map((q, qIdx) => (
                                <FollowUpCard
                                  key={q.id || qIdx}
                                  question={q}
                                  index={qIdx + 1}
                                  total={msg.analysisResult!.followUpQuestions.length}
                                  isAnswered={!isLatestAi}
                                  selectedAnswer={msg.selectedOption}
                                  disabled={isAnalyzing}
                                  onAnswer={(ans) =>
                                    handleAnswerFollowUp(q.id, q.question, ans)
                                  }
                                />
                              ))}
                            </div>
                          )}
                      </div>
                    );
                  })}

                  {/* M3 Analyzing Indicator */}
                  {isAnalyzing && (
                    <div className="flex items-center space-x-3 p-4 rounded-[22px] bg-white border border-[#bfc8ca]/50 shadow-2xs animate-fade-in">
                      <div className="w-5 h-5 border-2 border-[#006874] border-t-transparent rounded-full animate-spin flex-shrink-0" />
                      <div className="text-xs sm:text-sm text-[#3f484a] font-medium">
                        AI 正在以臨床醫學思維深層分析病理線索...
                      </div>
                    </div>
                  )}

                  <div ref={chatEndRef} />
                </div>
              </div>
            )}
          </>
        )}
      </main>

      {/* M3 Key Configuration Dialog */}
      {showKeyModal && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-[28px] max-w-md w-full p-6 shadow-xl space-y-4 animate-fade-in">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-[#191c1d] flex items-center">
                <Key className="w-4 h-4 mr-2 text-[#006874]" />
                Gemini API 金鑰設定
              </h3>
              <button
                onClick={() => setShowKeyModal(false)}
                className="w-8 h-8 rounded-full flex items-center justify-center text-[#6f797a] hover:bg-[#eceff0]"
              >
                ✕
              </button>
            </div>
            <p className="text-xs text-[#3f484a] leading-relaxed">
              系統已預載雲端金鑰或內建臨床鑑別引擎。如果您有個人 Google Gemini API Key，可在此輸入自訂金鑰以使用專屬配額。
            </p>
            <div className="space-y-3">
              <div>
                <label className="block text-xs font-bold text-[#191c1d] mb-1">選擇 Gemini 模型版本：</label>
                <select
                  value={selectedModel}
                  onChange={(e) => setSelectedModel(e.target.value)}
                  className="w-full px-4 py-2.5 rounded-[14px] border border-[#6f797a] text-sm bg-white focus:outline-none focus:ring-2 focus:ring-[#006874]"
                >
                  <option value="gemini-2.5-flash">Gemini 2.5 Flash (推薦・極速智能)</option>
                  <option value="gemini-2.0-flash">Gemini 2.0 Flash (經典穩定)</option>
                  <option value="gemini-1.5-flash">Gemini 1.5 Flash (穩定版)</option>
                  <option value="gemini-1.5-pro">Gemini 1.5 Pro (經典高階)</option>
                  <option value="gemini-2.5-pro">Gemini 2.5 Pro (進階・深度推理)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold text-[#191c1d] mb-1">自訂 Google Gemini API Key (選填)：</label>
                <input
                  type="password"
                  value={customApiKey}
                  onChange={(e) => setCustomApiKey(e.target.value)}
                  placeholder="AIzaSy..."
                  className="w-full px-4 py-2.5 rounded-[14px] border border-[#6f797a] text-sm focus:outline-none focus:ring-2 focus:ring-[#006874]"
                />
              </div>
            </div>

            <div className="flex justify-end space-x-2 pt-2">
              <button
                type="button"
                onClick={() => {
                  setCustomApiKey('');
                  setSelectedModel('gemini-2.5-flash');
                  localStorage.removeItem('user_gemini_api_key');
                  localStorage.removeItem('selected_gemini_model');
                  setShowKeyModal(false);
                }}
                className="px-4 py-2 rounded-full text-xs font-semibold text-[#3f484a] hover:bg-[#eceff0]"
              >
                恢復預設
              </button>
              <button
                type="button"
                onClick={() => {
                  localStorage.setItem('user_gemini_api_key', customApiKey.trim());
                  localStorage.setItem('selected_gemini_model', selectedModel);
                  setShowKeyModal(false);
                }}
                className="px-5 py-2 rounded-full text-xs font-bold bg-[#006874] text-white hover:bg-[#00505a]"
              >
                儲存設定
              </button>
            </div>
          </div>
        </div>
      )}

      {/* M3 Footer */}
      <footer className="border-t border-[#eceff0] bg-white py-3 px-4 text-center text-xs text-[#6f797a]">
        © 2026 症狀AI隨身問 • Material 3 規範設計 • 品質第一速度第二
      </footer>
    </div>
  );
};

export default App;
