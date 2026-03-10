export async function explainCode(code, language) {
  const res = await fetch('/api/explain', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code: String(code ?? ''), language: String(language ?? 'javascript') }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    const message = err?.error ?? `Request failed: ${res.status}`;
    throw new Error(typeof message === 'string' ? message : String(message));
  }
  const data = await res.json();
  return {
    explanation: data?.explanation ?? '',
    optimizedCode: data?.optimizedCode ?? '',
    timeComplexity: data?.timeComplexity ?? 'Unknown',
    detectedElements: data?.detectedElements ?? {
      functions: [],
      loops: [],
      conditionals: [],
      variables: [],
    },
  };
}
