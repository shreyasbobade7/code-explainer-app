import Editor from '@monaco-editor/react';

const monacoLanguageMap = {
  javascript: 'javascript',
  python: 'python',
};

export function CodeEditor({ value, language, onChange, readOnly = false, height = '100%' }) {
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
