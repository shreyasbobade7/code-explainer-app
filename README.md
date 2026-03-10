### 1. Set your Gemini API key

**Linux/Mac:**
```bash
export GEMINI_API_KEY=your-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY="your-api-key-here"
```

**Alternative:** Create `backend/src/main/resources/application-local.properties` with:
```properties
app.gemini.api-key=your-api-key-here
```

---

### Configuration table

| Property                         | Default            | Description           |
| -------------------------------- | ------------------ | --------------------- |
| `GEMINI_API_KEY`                 | (required)         | Google Gemini API key |
| `app.gemini.model`               | `gemini-2.0-flash` | Gemini model          |
| `app.python.path`                | `python`           | Python interpreter    |
| `app.python.ast.timeout-seconds` | `10`               | AST parsing timeout   |
| `server.port`                    | `8080`             | Server port           |

---

# 6️⃣ Final API Flow

Frontend → Backend → Gemini

```
React UI
   ↓
POST /api/explain
   ↓
CodeExplainerService
   ↓
AST Parser (JS / Python)
   ↓
GeminiService
   ↓
Gemini API
   ↓
Structured JSON response
```

---

# 7️⃣ Example API Response

```json
{
  "explanation": "This function calculates the Fibonacci number using recursion.",
  "optimizedCode": "function fib(n){ let a=0,b=1; for(let i=0;i<n;i++){[a,b]=[b,a+b]} return a; }",
  "timeComplexity": "O(n)"
}
```

---

# 8️⃣ Cursor AI Prompt (to build everything automatically)

Paste this in **Cursor AI**:

```
Create a production-ready Spring Boot 3.2 backend called AI Code Explainer.

Requirements:

1. REST endpoint
POST /api/explain

Request:
{
 "code": "string",
 "language": "javascript | python"
}

Response:
{
 "explanation": "...",
 "optimizedCode": "...",
 "timeComplexity": "...",
 "detectedElements": {
   "functions": [],
   "loops": [],
   "conditionals": [],
   "variables": []
 }
}

Architecture:

controller/
service/
parser/
model/
config/

Features:

1. JavaScript AST parsing using Rhino
2. Python AST parsing using subprocess calling parse_python.py
3. Gemini AI integration
4. Spring WebFlux WebClient
5. Clean logging and error handling
6. DTO validation
7. Maven project structure

Gemini endpoint:
https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent

Environment variable:
GEMINI_API_KEY

Return structured JSON from Gemini.
```

