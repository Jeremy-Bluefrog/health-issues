import React, { useState } from 'react';
import { ConsultationHistoryItem } from '../types';
import { TriageBadge } from './TriageBadge';
import { AnalysisDetailCard } from './AnalysisDetailCard';
import { Clock, Trash2, ArrowLeft, FolderOpen, Calendar } from 'lucide-react';

interface Props {
  history: ConsultationHistoryItem[];
  onDelete: (id: string) => void;
  onClearAll: () => void;
  onClose: () => void;
}

export const HistoryDrawer: React.FC<Props> = ({
  history,
  onDelete,
  onClearAll,
  onClose,
}) => {
  const [selectedItem, setSelectedItem] = useState<ConsultationHistoryItem | null>(null);

  return (
    <div className="max-w-3xl mx-auto p-4 sm:p-6 animate-fade-in space-y-4">
      {/* M3 Top Bar */}
      <div className="flex items-center justify-between border-b border-[#eceff0] pb-3">
        <div className="flex items-center space-x-2">
          {selectedItem && (
            <button
              onClick={() => setSelectedItem(null)}
              className="p-2 rounded-full hover:bg-[#eceff0] text-[#191c1d] mr-1 transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
          )}
          <h2 className="text-lg font-bold text-[#191c1d]">
            {selectedItem ? '問診評估詳細紀錄' : '過往問診與分診紀錄'}
          </h2>
        </div>

        {!selectedItem && history.length > 0 && (
          <button
            onClick={() => {
              if (window.confirm('確定要清空所有問診紀錄嗎？')) {
                onClearAll();
              }
            }}
            className="text-xs text-[#ba1a1a] hover:text-[#ba1a1a] font-semibold px-3 py-1.5 rounded-full hover:bg-[#ffdad6] transition-colors flex items-center"
          >
            <Trash2 className="w-3.5 h-3.5 mr-1" />
            清空紀錄
          </button>
        )}
      </div>

      {selectedItem ? (
        <div className="space-y-4">
          <div className="p-4 sm:p-5 rounded-[24px] bg-[#f2f5f6] border border-[#bfc8ca]/50 text-xs sm:text-sm">
            <span className="text-[#3f484a] font-medium">病患最初主訴：</span>
            <div className="font-bold text-[#191c1d] text-sm sm:text-base mt-1">
              {selectedItem.initialSymptom}
            </div>
            <div className="text-xs text-[#6f797a] mt-1.5 flex items-center">
              <Calendar className="w-3.5 h-3.5 mr-1" />
              諮詢時間：{new Date(selectedItem.createdAt).toLocaleString()}
            </div>
          </div>
          <AnalysisDetailCard analysis={selectedItem.fullAnalysis} />
        </div>
      ) : history.length === 0 ? (
        <div className="py-16 text-center space-y-3">
          <div className="w-16 h-16 rounded-full bg-[#cde7ec] text-[#006874] mx-auto flex items-center justify-center">
            <FolderOpen className="w-8 h-8" />
          </div>
          <h3 className="text-base font-bold text-[#191c1d]">尚無過往問診紀錄</h3>
          <p className="text-xs sm:text-sm text-[#3f484a] max-w-sm mx-auto leading-relaxed">
            您在諮詢時所完成的每份分診分析將自動儲存於此，方便日後就醫時向臨床醫師說明病程。
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {history.map((item) => (
            <div
              key={item.id}
              onClick={() => setSelectedItem(item)}
              className="p-4 sm:p-5 rounded-[24px] bg-white border border-[#bfc8ca]/50 shadow-2xs hover:border-[#006874] hover:shadow-xs cursor-pointer transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-3"
            >
              <div className="space-y-1.5 flex-1 min-w-0">
                <div className="flex items-center space-x-2">
                  <TriageBadge level={item.triageLevel} />
                  <span className="text-xs text-[#6f797a] flex items-center">
                    <Clock className="w-3 h-3 mr-1" />
                    {new Date(item.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="font-bold text-[#191c1d] text-sm sm:text-base truncate">
                  {item.initialSymptom}
                </div>
                <p className="text-xs text-[#3f484a] line-clamp-1">{item.summary}</p>
                {item.deptSummary && (
                  <div className="text-xs text-[#006874] font-semibold">
                    建議專科：{item.deptSummary}
                  </div>
                )}
              </div>

              <div className="flex items-center space-x-2 self-end sm:self-center">
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(item.id);
                  }}
                  className="p-2.5 rounded-full text-[#6f797a] hover:text-[#ba1a1a] hover:bg-[#ffdad6] transition-colors"
                  title="刪除"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
