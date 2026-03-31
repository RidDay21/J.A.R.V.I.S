let currentBaseUrl = "http://localhost:8080";

document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Загружаем сгенерированный JSON
        const response = await fetch('api-data.json');
        const apiData = await response.json();

        document.getElementById('api-title').textContent = apiData.title || 'API Docs';
        document.getElementById('api-version').textContent = apiData.version || '';
        if (apiData.baseUrl) {
            currentBaseUrl = apiData.baseUrl;
            const displayEl = document.getElementById('display-base-url');
            if (displayEl) displayEl.textContent = currentBaseUrl;
        }

        renderEndpoints(apiData.endpoints, apiData.schemas);
    } catch (e) {
        document.getElementById('api-title').textContent = 'Ошибка загрузки api-data.json';
        console.error(e);
    }
});

function renderEndpoints(endpoints, schemas) {
    const container = document.getElementById('endpoints-container');

    endpoints.forEach((ep, index) => {
        const details = document.createElement('details');
        details.className = 'endpoint';

        // Заголовок (Summary)
        const summary = document.createElement('summary');
        summary.innerHTML = `
            <span class="method-badge method-${ep.method}">${ep.method}</span>
            <span class="path">${ep.path}</span>
        `;

        // Тело эндпоинта
        const body = document.createElement('div');
        body.className = 'endpoint-body';

        let paramsHtml = '';
        if (ep.parameters && ep.parameters.length > 0) {
            paramsHtml += `<h4>Параметры:</h4>`;
            ep.parameters.forEach(p => {
                paramsHtml += `
                    <div class="param-row">
                        <strong>${p.name}</strong> (${p.in}, ${p.type}) ${p.required ? '*' : ''}: 
                        <input type="text" data-name="${p.name}" data-in="${p.in}" placeholder="значение...">
                    </div>
                `;
            });
        }

        let bodyHtml = '';
        if (ep.requestBody && schemas) {
            bodyHtml += `<h4>Request Body (${ep.requestBody.type}):</h4>`;
            // Генерируем пример JSON на основе схемы
            const sampleObj = buildSampleJson(ep.requestBody.type, schemas);
            bodyHtml += `<textarea class="request-body" id="body-${index}">${JSON.stringify(sampleObj, null, 2)}</textarea>`;
        }

        body.innerHTML = `
            ${paramsHtml}
            ${bodyHtml}
            <button class="btn-execute" onclick="executeRequest(${index}, '${ep.method}', '${ep.path}')">Execute</button>
            <div id="response-${index}" style="display: none;">
                <h4>Ответ сервера:</h4>
                <pre class="response-block" id="response-content-${index}"></pre>
            </div>
        `;

        details.appendChild(summary);
        details.appendChild(body);
        container.appendChild(details);
    });
}

// Рекурсивно строит пример JSON объекта на основе схем DTO
function buildSampleJson(schemaName, schemas, depth = 0) {
    if (depth > 5) return "Рекурсия слишком глубокая";

    const schema = schemas[schemaName];
    if (!schema) return {};

    const obj = {};
    if (schema.fields) {
        schema.fields.forEach(f => {
            if (f.ref) {
                // Если поле ссылается на другой класс — рекурсия
                obj[f.name] = buildSampleJson(f.ref, schemas, depth + 1);
            } else {
                // Иначе подставляем имя типа как строку
                obj[f.name] = f.type;
            }
        });
    }
    return obj;
}

// Выполнение реального запроса к API
async function executeRequest(index, method, path) {
    const baseUrl = currentBaseUrl.replace(/\/$/, "");
    let finalUrl = baseUrl + path;

    const container = document.getElementById('endpoints-container').children[index];
    const inputs = container.querySelectorAll('input[data-in]');

    const queryParams = new URLSearchParams();
    const headers = {};

    // Собираем параметры
    inputs.forEach(input => {
        const val = input.value;
        if (!val) return;

        const inType = input.getAttribute('data-in');
        const name = input.getAttribute('data-name');

        if (inType === 'path') {
            finalUrl = finalUrl.replace(`{${name}}`, encodeURIComponent(val));
        } else if (inType === 'query') {
            queryParams.append(name, val);
        } else if (inType === 'header') {
            headers[name] = val;
        }
    });

    if (queryParams.toString()) {
        finalUrl += '?' + queryParams.toString();
    }

    const fetchOptions = {
        method: method,
        headers: headers
    };

    // Подставляем тело запроса, если оно есть (для POST/PUT)
    const bodyTextarea = document.getElementById(`body-${index}`);
    if (bodyTextarea && (method === 'POST' || method === 'PUT')) {
        fetchOptions.body = bodyTextarea.value;
        fetchOptions.headers['Content-Type'] = 'application/json';
    }

    // Выводим результат
    const responseDiv = document.getElementById(`response-${index}`);
    const responseContent = document.getElementById(`response-content-${index}`);
    responseDiv.style.display = 'block';
    responseContent.textContent = 'Отправка запроса...';

    try {
        const res = await fetch(finalUrl, fetchOptions);
        let text = await res.text();

        try { // Пытаемся красиво отформатировать JSON
            const json = JSON.parse(text);
            text = JSON.stringify(json, null, 2);
        } catch (e) {}

        responseContent.textContent = `Status: ${res.status}\n\n${text}`;
    } catch (err) {
        responseContent.textContent = `Ошибка сети (CORS или сервер недоступен):\n${err.message}`;
    }
}