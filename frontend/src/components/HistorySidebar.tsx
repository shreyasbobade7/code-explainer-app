import type { SnippetHistoryItem } from '../types/api';

interface HistorySidebarProps {
  items: SnippetHistoryItem[];
  selectedId: string | null;
  onSelect: (item: SnippetHistoryItem) => void;
  onClear: () => void;
}

export function HistorySidebar({ items, selectedId, onSelect, onClear }: HistorySidebarProps) {
  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center justify-between px-3 py-3 border-b border-[var(--border-default)]">
        <h2 className="text-sm font-semibold text-[var(--text-secondary)]">History</h2>
        {items.length > 0 && (
          <button
            onClick={onClear}
            className="text-xs text-[var(--text-muted)] hover:text-[var(--text-primary)] transition-colors"
          >
            Clear
          </button>
        )}
      </div>
      <div className="flex-1 overflow-y-auto p-2">
        {items.length === 0 ? (
          <p className="text-[var(--text-muted)] text-xs px-2 py-4">No snippets yet</p>
        ) : (
          <ul className="space-y-1">
            {items.slice().reverse().map((item) => (
              <li key={item.id}>
                <button
                  onClick={() => onSelect(item)}
                  className={`w-full text-left px-3 py-2.5 rounded-md transition-colors ${
                    selectedId === item.id
                      ? 'bg-[var(--accent-muted)] text-[var(--accent)]'
                      : 'hover:bg-[var(--bg-tertiary)] text-[var(--text-secondary)] hover:text-[var(--text-primary)]'
                  }`}
                >
                  <span className="block text-xs font-medium uppercase tracking-wider text-[var(--text-muted)] mb-1">
                    {item.language === 'javascript' ? 'JavaScript' : 'Python'}
                  </span>
                  <span className="block text-sm font-mono truncate">{shortPreview(item.code)}</span>
                  <span className="block text-xs text-[var(--text-muted)] mt-1">{formatTime(item.timestamp)}</span>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}

function shortPreview(code: string, maxLen = 40): string {
  const firstLine = code.trim().split('\n')[0]?.trim() || '';
  if (!firstLine) return '(empty)';
  return firstLine.length > maxLen ? firstLine.slice(0, maxLen) + '…' : firstLine;
}

function formatTime(timestamp: number): string {
  const d = new Date(timestamp);
  const now = new Date();
  const diff = now.getTime() - d.getTime();
  if (diff < 60000) return 'Just now';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
  return d.toLocaleDateString();
}
