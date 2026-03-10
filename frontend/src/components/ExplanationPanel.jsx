export function ExplanationPanel({ response, loading, error }) {
  if (loading) {
    return (
      <div className="h-full flex items-center justify-center text-[var(--text-muted)]">
        <div className="flex flex-col items-center gap-3">
          <div
            className="w-10 h-10 border-2 border-[var(--accent)]/30 border-t-[var(--accent)] rounded-full animate-spin"
            role="status"
          />
          <span className="text-sm">Processing...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-full flex items-center justify-center p-4 rounded-lg border border-[var(--error-border)] bg-[var(--error-bg)]">
        <p className="text-center text-[var(--error-text)] text-sm">{error}</p>
      </div>
    );
  }

  if (!response) {
    return (
      <div className="h-full flex flex-col items-center justify-center text-[var(--text-muted)] gap-3">
        <svg className="w-14 h-14 opacity-40" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <p className="text-sm text-center">Click <strong>Explain Code</strong> to analyze</p>
      </div>
    );
  }

  const explanation = response.explanation ?? '';
  const timeComplexity = response.timeComplexity ?? 'Unknown';
  const detectedElements = response.detectedElements ?? {
    functions: [],
    loops: [],
    conditionals: [],
    variables: [],
  };

  return (
    <div className="space-y-5 overflow-y-auto h-full pr-2">
      <section>
        <h3 className="text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-2">Explanation</h3>
        <p className="text-[var(--text-primary)] leading-relaxed text-sm whitespace-pre-wrap">
          {explanation || 'No explanation available.'}
        </p>
      </section>

      <section>
        <h3 className="text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-2">Time Complexity</h3>
        <span className="inline-flex items-center px-3 py-1.5 rounded-md bg-[var(--accent-muted)] text-[var(--accent)] font-mono text-sm">
          {timeComplexity}
        </span>
      </section>

      <section>
        <h3 className="text-xs font-semibold text-[var(--text-muted)] uppercase tracking-wider mb-3">Detected Elements</h3>
        <DetectedElementsList elements={detectedElements} />
      </section>
    </div>
  );
}

function DetectedElementsList({ elements }) {
  const safe = elements ?? {};
  const sections = [
    { label: 'Functions', items: Array.isArray(safe.functions) ? safe.functions : [], color: 'text-blue-400' },
    { label: 'Loops', items: Array.isArray(safe.loops) ? safe.loops : [], color: 'text-amber-400' },
    { label: 'Conditionals', items: Array.isArray(safe.conditionals) ? safe.conditionals : [], color: 'text-purple-400' },
    { label: 'Variables', items: Array.isArray(safe.variables) ? safe.variables : [], color: 'text-emerald-400' },
  ].filter((s) => s.items.length > 0);

  if (sections.length === 0) {
    return <p className="text-[var(--text-muted)] text-sm">No elements detected</p>;
  }

  return (
    <div className="space-y-3">
      {sections.map(({ label, items, color }) => (
        <div key={label}>
          <span className="text-xs text-[var(--text-muted)]">{label}</span>
          <div className="flex flex-wrap gap-1.5 mt-1">
            {items.map((item) => (
              <span
                key={item}
                className={`inline-flex px-2 py-0.5 rounded text-xs font-mono ${color} bg-[var(--bg-tertiary)]`}
              >
                {item}
              </span>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
