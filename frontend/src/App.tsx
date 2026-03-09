import { useState, useCallback } from 'react';
import { CodeEditor } from './components/CodeEditor';
import { ExplanationPanel } from './components/ExplanationPanel';
import { DiffViewer } from './components/DiffViewer';
import { HistorySidebar } from './components/HistorySidebar';
import { ErrorBanner } from './components/ErrorBanner';
import { LoadingOverlay } from './components/LoadingOverlay';
import { explainCode } from './api/explain';
import type { Language, ExplainResponse, SnippetHistoryItem } from './types/api';

const DEFAULT_JS = `function calculateSum(arr) {
  let total = 0;
  for (let i = 0; i < arr.length; i++) {
    total += arr[i];
  }
  return total;
}`;

const DEFAULT_PY = `def calculate_sum(arr):
    total = 0
    for num in arr:
        total += num
    return total`;

function generateId() {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

function App() {
  const [code, setCode] = useState(DEFAULT_JS);
  const [language, setLanguage] = useState<Language>('javascript');
  const [response, setResponse] = useState<ExplainResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<SnippetHistoryItem[]>([]);
  const [selectedHistoryId, setSelectedHistoryId] = useState<string | null>(null);
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleExplain = useCallback(async () => {
    setError(null);
    setLoading(true);
    try {
      const result = await explainCode(code, language);
      setResponse(result);
      const item: SnippetHistoryItem = {
        id: generateId(),
        code,
        language,
        response: result,
        timestamp: Date.now(),
      };
      setHistory((prev) => [...prev.slice(-9), item]);
      setSelectedHistoryId(item.id);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong');
    } finally {
      setLoading(false);
    }
  }, [code, language]);

  const handleSelectHistory = useCallback((item: SnippetHistoryItem) => {
    setCode(item.code);
    setLanguage(item.language);
    setResponse(item.response ?? null);
    setSelectedHistoryId(item.id);
    setError(null);
  }, []);

  const handleClearHistory = useCallback(() => {
    setHistory([]);
    setSelectedHistoryId(null);
  }, []);

  const handleLanguageChange = (lang: Language) => {
    setLanguage(lang);
    setCode(lang === 'javascript' ? DEFAULT_JS : DEFAULT_PY);
  };

  const hasResponse = !!response;
  const originalCode = code;
  const optimizedCode = response?.optimizedCode ?? '';

  return (
    <div className="h-screen flex flex-col bg-[var(--bg-primary)] text-[var(--text-primary)]">
      <LoadingOverlay visible={loading} />

      {/* Header */}
      <header className="flex items-center justify-between px-3 sm:px-4 py-3 border-b border-[var(--border-default)] bg-[var(--bg-secondary)] shrink-0 gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <button
            onClick={() => setSidebarOpen((o) => !o)}
            className="lg:hidden p-2 rounded hover:bg-[var(--bg-tertiary)]"
            aria-label={sidebarOpen ? 'Hide sidebar' : 'Show sidebar'}
          >
            <svg className="w-5 h-5 text-[var(--text-secondary)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <h1 className="text-base sm:text-lg font-semibold truncate">AI Code Explainer</h1>
        </div>
        <div className="flex items-center gap-2 sm:gap-4 shrink-0">
          <div className="flex rounded-lg bg-[var(--bg-tertiary)] p-0.5">
            {(['javascript', 'python'] as const).map((lang) => (
              <button
                key={lang}
                onClick={() => handleLanguageChange(lang)}
                className={`px-3 sm:px-4 py-2 rounded-md text-xs sm:text-sm font-medium transition-colors capitalize ${
                  language === lang
                    ? 'bg-[var(--accent-muted)] text-[var(--accent)]'
                    : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                }`}
              >
                {lang === 'javascript' ? 'JS' : 'Py'}
              </button>
            ))}
          </div>
          <button
            onClick={handleExplain}
            disabled={loading}
            className="px-4 sm:px-5 py-2 sm:py-2.5 rounded-lg bg-[var(--accent)] hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed font-medium text-black transition-opacity text-sm sm:text-base"
          >
            {loading ? 'Analyzing...' : 'Explain'}
          </button>
        </div>
      </header>

      {/* Main content */}
      <div className="flex-1 flex min-h-0">
        {/* History sidebar - collapsible on mobile */}
        <aside
          className={`${
            sidebarOpen ? 'w-52 sm:w-56' : 'w-0'
          } shrink-0 flex flex-col border-r border-[var(--border-default)] bg-[var(--bg-secondary)] overflow-hidden transition-all duration-200 lg:!w-56`}
        >
          <HistorySidebar
            items={history}
            selectedId={selectedHistoryId}
            onSelect={handleSelectHistory}
            onClear={handleClearHistory}
          />
        </aside>

        {/* Editor + Explanation area */}
        <main className="flex-1 flex flex-col min-w-0 p-3 sm:p-4 gap-3 sm:gap-4 overflow-auto">
          {error && (
            <ErrorBanner
              message={error}
              onDismiss={() => setError(null)}
              onRetry={handleExplain}
            />
          )}

          {/* Top: Editor left, Explanation right - stack on small screens */}
          <div className="flex-1 flex flex-col lg:flex-row gap-3 sm:gap-4 min-h-0">
            <div className="flex-1 min-w-0 flex flex-col min-h-[200px] lg:min-h-0">
              <label className="text-xs text-[var(--text-muted)] mb-1 block">Code Editor</label>
              <div className="flex-1 min-h-[180px] card overflow-hidden">
                <CodeEditor value={code} language={language} onChange={setCode} height="500px" />
              </div>
            </div>
            <div className="w-full lg:w-96 shrink-0 flex flex-col min-h-[200px] lg:min-h-0">
              <label className="text-xs text-[var(--text-muted)] mb-1 block">Explanation</label>
              <div className="flex-1 min-h-[180px] card p-4 overflow-y-auto">
                <ExplanationPanel response={response} loading={loading} error={error} />
              </div>
            </div>
          </div>

          {/* Bottom: Diff viewer */}
          {hasResponse && (
            <div className="h-60 sm:h-72 shrink-0 card overflow-hidden">
              <DiffViewer
                original={originalCode}
                optimized={optimizedCode}
                visible={hasResponse}
              />
            </div>
          )}
        </main>
      </div>
    </div>
  );
}

export default App;
