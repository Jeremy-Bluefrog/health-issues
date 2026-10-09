import React from 'react';
import { TriageLevel } from '../types';
import { AlertTriangle, Clock, Hospital, CheckCircle2 } from 'lucide-react';

interface Props {
  level: TriageLevel;
  className?: string;
  showIcon?: boolean;
}

export const TriageBadge: React.FC<Props> = ({ level, className = '', showIcon = true }) => {
  const configs: Record<
    TriageLevel,
    { label: string; bg: string; text: string; icon: React.ReactNode }
  > = {
    EMERGENCY: {
      label: '緊急就醫 (急診)',
      bg: 'bg-[#ffdad6] text-[#410002] border border-[#ffb4ab]',
      text: 'text-[#410002]',
      icon: <AlertTriangle className="w-3.5 h-3.5 mr-1 text-[#ba1a1a]" />,
    },
    PROMPT: {
      label: '儘速就醫 (24小時內)',
      bg: 'bg-[#ffdbcc] text-[#351000] border border-[#ffb690]',
      text: 'text-[#351000]',
      icon: <Clock className="w-3.5 h-3.5 mr-1 text-[#9c4300]" />,
    },
    ROUTINE_CLINIC: {
      label: '建議常規門診',
      bg: 'bg-[#ffe08d] text-[#261900] border border-[#f5bf38]',
      text: 'text-[#261900]',
      icon: <Hospital className="w-3.5 h-3.5 mr-1 text-[#7a5900]" />,
    },
    SELF_CARE: {
      label: '自我照護與持續觀察',
      bg: 'bg-[#89f8c7] text-[#002114] border border-[#5bdbad]',
      text: 'text-[#002114]',
      icon: <CheckCircle2 className="w-3.5 h-3.5 mr-1 text-[#006c4c]" />,
    },
  };

  const config = configs[level] || configs.ROUTINE_CLINIC;

  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-bold transition-all shadow-2xs ${config.bg} ${className}`}
    >
      {showIcon && config.icon}
      {config.label}
    </span>
  );
};
