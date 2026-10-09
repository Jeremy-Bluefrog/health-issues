import React from 'react';
import { AiAnalysisResult } from '../types';
import { TriageBadge } from './TriageBadge';
import {
  Activity,
  AlertOctagon,
  Building2,
  Info,
  ShieldAlert,
  SunMedium,
  CheckCircle,
} from 'lucide-react';

interface Props {
  analysis: AiAnalysisResult;
}

export const AnalysisDetailCard: React.FC<Props> = ({ analysis }) => {
  return (
    <div className="bg-white rounded-[28px] border border-[#bfc8ca]/60 shadow-sm p-5 sm:p-7 space-y-6">
      {/* M3 Header: Triage Badge & Round Tag */}
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#eceff0] pb-4">
        <div className="flex items-center space-x-2.5">
          <TriageBadge level={analysis.triageLevel} />
          {analysis.isComprehensive && (
            <span className="text-xs px-3 py-1 rounded-full font-bold bg-[#dae2ff] text-[#0e1a37] border border-[#b4c5ff]">
              深度綜合評估報告
            </span>
          )}
        </div>
        <span className="text-xs text-[#3f484a] font-semibold">
          問診第 {analysis.inquiryRound} 輪
        </span>
      </div>

      {/* M3 Summary Surface Container */}
      <div className="p-4 sm:p-5 rounded-[20px] bg-[#cde7ec]/35 border border-[#cde7ec]">
        <div className="flex items-center text-[#006874] font-bold text-sm mb-1.5">
          <Activity className="w-4 h-4 mr-1.5 text-[#006874]" />
          AI 臨床分診摘要
        </div>
        <p className="text-sm text-[#191c1d] leading-relaxed font-normal">
          {analysis.summary}
        </p>
      </div>

      {/* M3 Potential Causes Section */}
      {analysis.possibleCauses && analysis.possibleCauses.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center text-[#191c1d] font-bold text-sm">
            <Info className="w-4 h-4 mr-1.5 text-[#006874]" />
            潛在可能病因鑑別
          </div>

          <div className="grid grid-cols-1 gap-2.5">
            {analysis.possibleCauses.map((cause, idx) => (
              <div
                key={idx}
                className="p-4 rounded-[18px] bg-[#f2f5f6] border border-[#bfc8ca]/50 text-sm hover:border-[#6f797a] transition-all"
              >
                <div className="flex items-center justify-between gap-2 mb-1.5">
                  <span className="font-bold text-[#191c1d] text-sm sm:text-base">
                    {idx + 1}. {cause.name}
                  </span>
                  <span className="text-xs px-2.5 py-0.5 rounded-full font-semibold bg-white border border-[#bfc8ca] text-[#3f484a]">
                    {cause.likelihood}
                  </span>
                </div>
                <p className="text-xs sm:text-sm text-[#3f484a] leading-relaxed mb-2.5">
                  {cause.explanation}
                </p>
                {cause.keySigns && cause.keySigns.length > 0 && (
                  <div className="flex flex-wrap gap-1.5">
                    {cause.keySigns.map((sign, sIdx) => (
                      <span
                        key={sIdx}
                        className="text-xs px-2.5 py-0.5 rounded-full bg-white text-[#3f484a] border border-[#bfc8ca]/70"
                      >
                        • {sign}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* M3 Recommended Specialty Departments */}
      {analysis.recommendedDepartments && analysis.recommendedDepartments.length > 0 && (
        <div className="space-y-3">
          <div className="flex items-center text-[#191c1d] font-bold text-sm">
            <Building2 className="w-4 h-4 mr-1.5 text-[#006874]" />
            建議就診專科
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            {analysis.recommendedDepartments.map((dept, dIdx) => (
              <div
                key={dIdx}
                className="p-3.5 rounded-[18px] bg-[#f2f5f6] border border-[#bfc8ca]/40 flex items-start space-x-3"
              >
                <div className="w-9 h-9 rounded-full bg-[#006874] text-white flex items-center justify-center flex-shrink-0 font-bold text-xs shadow-2xs">
                  專科
                </div>
                <div className="text-xs sm:text-sm flex-1">
                  <div className="font-bold text-[#191c1d] flex items-center justify-between">
                    <span>{dept.name}</span>
                    <span className="text-[#006874] text-xs font-semibold px-2 py-0.5 rounded-full bg-[#cde7ec]">
                      {dept.urgency}
                    </span>
                  </div>
                  <p className="text-[#3f484a] text-xs mt-1 leading-snug">
                    {dept.reason}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* M3 Red Flag Warning Alert Surface */}
      {analysis.redFlagWarnings && analysis.redFlagWarnings.length > 0 && (
        <div className="p-4 sm:p-5 rounded-[22px] bg-[#ffdad6] border border-[#ffb4ab] text-xs sm:text-sm">
          <div className="flex items-center text-[#410002] font-bold text-sm mb-2.5">
            <AlertOctagon className="w-4 h-4 mr-1.5 text-[#ba1a1a] flex-shrink-0" />
            危險紅旗警訊（若出現以下徵候請立即急診）
          </div>
          <ul className="space-y-1.5 text-[#410002]">
            {analysis.redFlagWarnings.map((warning, wIdx) => (
              <li key={wIdx} className="flex items-start">
                <span className="mr-1.5 text-[#ba1a1a] font-bold">⚠️</span>
                <span className="leading-snug">{warning}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {/* M3 Home Care Guidance */}
      {analysis.homeCareAdvice && analysis.homeCareAdvice.length > 0 && (
        <div className="space-y-2.5">
          <div className="flex items-center text-[#191c1d] font-bold text-sm">
            <SunMedium className="w-4 h-4 mr-1.5 text-[#006874]" />
            暫時舒緩與居家照護指引
          </div>
          <div className="space-y-2 text-xs sm:text-sm text-[#3f484a]">
            {analysis.homeCareAdvice.map((advice, aIdx) => (
              <div key={aIdx} className="flex items-start">
                <span className="w-5 h-5 rounded-full bg-[#cde7ec] text-[#006874] text-xs font-bold flex items-center justify-center mr-2.5 flex-shrink-0 mt-0.5">
                  {aIdx + 1}
                </span>
                <span className="leading-relaxed text-[#191c1d]">{advice}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* M3 Legal/Medical Disclaimer */}
      <div className="pt-3 border-t border-[#eceff0] flex items-start text-xs text-[#6f797a] leading-normal">
        <ShieldAlert className="w-4 h-4 mr-1.5 text-[#6f797a] flex-shrink-0 mt-0.5" />
        <span>{analysis.disclaimer}</span>
      </div>
    </div>
  );
};
