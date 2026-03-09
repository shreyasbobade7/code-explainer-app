interface LoadingOverlayProps {
  visible: boolean;
}

export function LoadingOverlay({ visible }: LoadingOverlayProps) {
  if (!visible) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      aria-live="polite"
      aria-busy="true"
    >
      <div className="card-elevated flex flex-col items-center gap-4 px-8 py-6">
        <div
          className="w-12 h-12 border-2 border-[var(--accent)]/30 border-t-[var(--accent)] rounded-full animate-spin"
          role="status"
        />
        <p className="text-[var(--text-primary)] font-medium">Analyzing code with AI...</p>
        <p className="text-[var(--text-muted)] text-sm">This may take a few seconds</p>
      </div>
    </div>
  );
}
