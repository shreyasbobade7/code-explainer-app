export function ErrorBanner({ message, onDismiss, onRetry }) {
  return (
    <div
      role="alert"
      className="flex items-center justify-between gap-4 px-4 py-3 rounded-lg border border-[var(--error-border)] bg-[var(--error-bg)] text-[var(--error-text)]"
    >
      <div className="flex items-center gap-2 min-w-0">
        <svg className="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <p className="text-sm truncate">{message}</p>
      </div>
      <div className="flex items-center gap-2 shrink-0">
        {onRetry && (
          <button
            onClick={onRetry}
            className="px-3 py-1.5 rounded text-sm font-medium hover:bg-red-500/20 transition-colors"
          >
            Retry
          </button>
        )}
        <button
          onClick={onDismiss}
          className="p-1.5 rounded hover:bg-red-500/20 transition-colors"
          aria-label="Dismiss"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>
  );
}
