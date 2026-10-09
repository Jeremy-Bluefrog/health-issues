import React, { useState } from 'react';
import { FollowUpQuestion } from '../types';
import { HelpCircle, Sparkles, Send, Edit3, Check } from 'lucide-react';

interface Props {
  question: FollowUpQuestion;
  index: number;
  total: number;
  isAnswered: boolean;
  selectedAnswer?: string;
  onAnswer: (answer: string) => void;
  disabled?: boolean;
}

export const FollowUpCard: React.FC<Props> = ({
  question,
  index,
  total,
  isAnswered,
  selectedAnswer,
  onAnswer,
  disabled = false,
}) => {
  const [showCustom, setShowCustom] = useState(false);
  const [customText, setCustomText] = useState('');

  const handleCustomSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (customText.trim()) {
      onAnswer(customText.trim());
      setCustomText('');
      setShowCustom(false);
    }
  };

  return (
    <div
      className={`rounded-[24px] border transition-all p-5 sm:p-6 ${
        isAnswered
          ? 'bg-[#ffffff] border-[#bfc8ca]/50 shadow-xs'
          : 'bg-[#ffffff] border-[#9eeffd] shadow-sm ring-1 ring-[#9eeffd]/60'
      }`}
    >
      {/* Header Badge & Index */}
      <div className="flex items-center justify-between gap-2 mb-3">
        <div className="flex items-center space-x-2">
          <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-[#cde7ec] text-[#051f23]">
            <Sparkles className="w-3.5 h-3.5 mr-1 text-[#006874]" />
            AI 深入追問
          </span>
          {total > 1 && (
            <span className="text-xs font-semibold text-[#3f484a]">
              第 {index} / {total} 題
            </span>
          )}
        </div>

        {isAnswered && (
          <span className="inline-flex items-center text-xs px-2.5 py-0.5 rounded-full bg-[#eceff0] text-[#3f484a] font-medium">
            <Check className="w-3 h-3 mr-1 text-[#006874]" />
            已回答
          </span>
        )}
      </div>

      {/* Question Headline */}
      <h3 className="text-base sm:text-lg font-bold text-[#191c1d] leading-snug">
        {question.question}
      </h3>

      {/* Purpose (Assist Info) */}
      {question.purpose && (
        <div className="flex items-center text-xs text-[#006874] mt-2 mb-4 bg-[#cde7ec]/40 px-3 py-1.5 rounded-[12px] border border-[#cde7ec]/60">
          <HelpCircle className="w-3.5 h-3.5 mr-1.5 flex-shrink-0 text-[#006874]" />
          <span className="font-medium">鑑別目的：{question.purpose}</span>
        </div>
      )}

      {/* Answered State or Interactive Chips */}
      {isAnswered && selectedAnswer ? (
        <div className="mt-3 p-3.5 rounded-[16px] bg-[#9eeffd]/20 border border-[#9eeffd]/80 text-sm text-[#001f24] font-medium flex items-center justify-between">
          <span>您的回答：<b className="font-bold">{selectedAnswer}</b></span>
        </div>
      ) : (
        <div className="mt-3 space-y-3">
          <p className="text-xs font-medium text-[#3f484a]">
            請點選最吻合的選項，或自訂描述：
          </p>

          <div className="flex flex-wrap gap-2">
            {question.options.map((opt, optIdx) => (
              <button
                key={optIdx}
                type="button"
                disabled={disabled}
                onClick={() => onAnswer(opt)}
                className="px-4 py-2 text-xs sm:text-sm font-medium rounded-full bg-[#f2f5f6] hover:bg-[#cde7ec] hover:text-[#001f24] border border-[#bfc8ca] text-[#191c1d] transition-all active:scale-[0.98] disabled:opacity-50 flex items-center shadow-2xs m3-ripple"
              >
                {opt}
              </button>
            ))}

            <button
              type="button"
              disabled={disabled}
              onClick={() => setShowCustom(!showCustom)}
              className="inline-flex items-center px-4 py-2 text-xs sm:text-sm font-semibold rounded-full bg-transparent border border-dashed border-[#6f797a] text-[#006874] hover:bg-[#cde7ec]/30 transition-colors"
            >
              <Edit3 className="w-3.5 h-3.5 mr-1.5" />
              {showCustom ? '收起補充' : '自訂其他狀況'}
            </button>
          </div>

          {/* Custom Input Field (M3 Outlined Text Field) */}
          {showCustom && (
            <form onSubmit={handleCustomSubmit} className="flex gap-2 pt-2 animate-fade-in">
              <input
                type="text"
                value={customText}
                onChange={(e) => setCustomText(e.target.value)}
                placeholder="簡短描述您的具體感覺與狀況..."
                disabled={disabled}
                className="flex-1 px-4 py-2.5 text-sm rounded-[14px] bg-white border border-[#6f797a] text-[#191c1d] focus:outline-none focus:ring-2 focus:ring-[#006874] focus:border-transparent transition-all placeholder:text-[#6f797a]"
              />
              <button
                type="submit"
                disabled={disabled || !customText.trim()}
                className="px-5 py-2.5 bg-[#006874] hover:bg-[#00505a] text-white rounded-full text-sm font-bold flex items-center disabled:opacity-50 transition-all active:scale-95 shadow-sm m3-ripple"
              >
                <Send className="w-3.5 h-3.5 mr-1" />
                送出
              </button>
            </form>
          )}
        </div>
      )}
    </div>
  );
};
