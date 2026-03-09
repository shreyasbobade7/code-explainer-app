# AI Code Explainer – Frontend

Modern React frontend for the AI Code Explainer backend.

## Stack

- **Vite** – build tool
- **React 19** + **TypeScript**
- **Tailwind CSS**
- **Monaco Editor**
- **react-diff-viewer-continued**

## Features

- Code editor (Monaco) with syntax highlighting
- Language selector (JavaScript / Python)
- Explain Code button – calls backend `/api/explain`
- Explanation panel – main purpose, explanation, key elements
- Detected code elements – functions, loops, conditionals, variables
- Time complexity display
- Diff viewer – original vs optimized code
- Snippet history sidebar

## Layout

```
┌──────────────────────────────────────────────────────────────────┐
│ Header: Language selector | Explain Code button                   │
├────────┬─────────────────────────────────────┬────────────────────┤
│History │ Code Editor                         │ Explanation Panel  │
│sidebar │                                     │ - Explanation      │
│        │                                     │ - Time Complexity  │
│        │                                     │ - Detected Elements│
├────────┴─────────────────────────────────────┴────────────────────┤
│ Diff Viewer (Original vs Optimized)                               │
└──────────────────────────────────────────────────────────────────┘
```

## Setup

```bash
cd frontend
npm install
npm run dev
```

Runs at http://localhost:5173. Proxies `/api` to http://localhost:8080.

## Build

```bash
npm run build
```
