import Editor from '@monaco-editor/react';
import type { Language } from '../types/api';

interface CodeEditorProps {
  value: string;
  language: Language;
  onChange: (value: string) => void;
  readOnly?: boolean;
  height?: string;
}

const monacoLanguageMap: Record<Language, string> = {
  javascript: 'javascript',
  python: 'python',
};

export function CodeEditor({ value, language, onChange, readOnly = false, height = '100%' }: CodeEditorProps) {
  return (
    <div className="overflow-hidden bg-[#1e1e1e]">
      <Editor
        height={height}
        language={monacoLanguageMap[language]}
        value={value}
        onChange={(v) => onChange(v ?? '')}
        options={{
          readOnly,
          minimap: { enabled: false },
          fontSize: 14,
          padding: { top: 16 },
          scrollBeyondLastLine: false,
          wordWrap: 'on',
          automaticLayout: true,
        }}
        theme="vs-dark"
      />
    </div>
  );
}
