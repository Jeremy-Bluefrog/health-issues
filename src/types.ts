export type TriageLevel = 'EMERGENCY' | 'PROMPT' | 'ROUTINE_CLINIC' | 'SELF_CARE';

export interface PossibleCause {
  name: string;
  likelihood: string;
  explanation: string;
  keySigns?: string[];
}

export interface FollowUpQuestion {
  id: string;
  question: string;
  purpose: string;
  options: string[];
}

export interface DepartmentRecommendation {
  name: string;
  reason: string;
  urgency: string;
}

export interface AiAnalysisResult {
  triageLevel: TriageLevel;
  summary: string;
  possibleCauses: PossibleCause[];
  followUpQuestions: FollowUpQuestion[];
  recommendedDepartments: DepartmentRecommendation[];
  redFlagWarnings: string[];
  homeCareAdvice: string[];
  disclaimer: string;
  isComprehensive?: boolean;
  inquiryRound: number;
}

export type MessageSender = 'USER' | 'AI' | 'SYSTEM';

export interface ChatMessage {
  id: string;
  sender: MessageSender;
  timestamp: number;
  text: string;
  analysisResult?: AiAnalysisResult;
  answeredQuestionId?: string;
  selectedOption?: string;
}

export interface InitialSymptomInput {
  description: string;
  duration: string;
  painLevel: number;
  ageGroup: string;
  chronicConditions: string;
}

export interface ConsultationHistoryItem {
  id: string;
  createdAt: number;
  initialSymptom: string;
  triageLevel: TriageLevel;
  summary: string;
  causesSummary: string;
  deptSummary: string;
  fullAnalysis: AiAnalysisResult;
}
