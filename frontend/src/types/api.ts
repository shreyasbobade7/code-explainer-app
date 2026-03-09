export type Language = 'javascript' | 'python';

export interface DetectedElements {
  functions: string[];
  loops: string[];
  conditionals?: string[];
  variables: string[];
}

export interface ExplainResponse {
  explanation: string;
  optimizedCode: string;
  timeComplexity: string;
  detectedElements: DetectedElements;
}

export interface SnippetHistoryItem {
  id: string;
  code: string;
  language: Language;
  response?: ExplainResponse;
  timestamp: number;
}
