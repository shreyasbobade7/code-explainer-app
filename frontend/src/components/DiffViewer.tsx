import ReactDiffViewer, { DiffMethod } from 'react-diff-viewer-continued';

interface DiffViewerProps {
  original: string;
  optimized: string;
  visible: boolean;
}

const diffStyles = {
  variables: {
    dark: {
      diffViewerBackground: '#1e1e1e',
      diffViewerColor: '#e4e4e7',
      addedBackground: '#14532d',
      addedColor: '#86efac',
      removedBackground: '#7f1d1d',
      removedColor: '#fca5a5',
      wordAddedBackground: '#22c55e80',
      wordRemovedBackground: '#ef444480',
      addedGutterBackground: '#166534',
      removedGutterBackground: '#991b1b',
      gutterBackground: '#18181b',
      gutterColor: '#71717a',
      diffViewerTitleBackground: '#27272a',
      diffViewerTitleColor: '#a1a1aa',
    },
  },
};

export function DiffViewer({ original, optimized, visible }: DiffViewerProps) {
  if (!visible) return null;

  return (
    <div className="overflow-hidden bg-[#1e1e1e]">
      <div className="flex items-center justify-between px-4 py-2.5 bg-[var(--bg-tertiary)] border-b border-[var(--border-default)]">
        <span className="text-[var(--text-secondary)] text-sm font-medium">Code diff comparison</span>
      </div>
      <div className="[&_.diff]:!rounded-none">
        <ReactDiffViewer
          oldValue={original || '// (no code)'}
          newValue={optimized || '// (no optimized version)'}
          splitView={true}
          useDarkTheme={true}
          leftTitle="Original Code"
          rightTitle="Optimized Code"
          showDiffOnly={false}
          disableWordDiff={false}
          compareMethod={DiffMethod.WORDS}
          styles={diffStyles}
        />
      </div>
    </div>
  );
}
