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
            // Обновляем поле ввода, если оно есть
            const baseUrlInput = document.getElementById('base-url');
            if (baseUrlInput) baseUrlInput.value = currentBaseUrl;
        }


        renderEndpoints(apiData.endpoints, apiData.schemas);
        if (apiData.schemas) {
            renderSchemas(apiData.schemas);
         }
    } catch (e) {
        document.getElementById('api-title').textContent = 'Ошибка загрузки api-data.json';
        console.error(e);
    }
});

function renderEndpoints(endpoints, schemas) {
    const container = document.getElementById('endpoints-container');
    container.innerHTML = ''; // очищаем контейнер

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

        // ПАРАМЕТРЫ
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

        // REQUEST BODY (тело запроса)
        let requestHtml = '';
        if (ep.requestBody && ep.requestBody.type && schemas) {
            requestHtml += `<h4>Request Body (${ep.requestBody.type}):</h4>`;
            const sampleObj = buildSampleJson(ep.requestBody.type, schemas);
            requestHtml += `<textarea class="request-body" id="body-${index}">${JSON.stringify(sampleObj, null, 2)}</textarea>`;
        }

        // RESPONSE BODY (схема ответа) - ЭТОТ БЛОК БЫЛ ОТСУТСТВУЕТ
        let responseHtml = '';
        if (ep.responseBody && ep.responseBody.type && schemas && ep.responseBody.type !== 'void') {
            responseHtml += `<h4>Response Body (${ep.responseBody.type}):</h4>`;
            const sampleResponse = buildSampleJson(ep.responseBody.type, schemas);
            responseHtml += `<pre class="response-schema-preview">${JSON.stringify(sampleResponse, null, 2)}</pre>`;
        }

        body.innerHTML = `
            ${paramsHtml}
            ${requestHtml}
            ${responseHtml}
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

    // Если это примитив, а не ссылка на схему — возвращаем заглушку сразу
    if (schemaName === 'string') return "string";
    if (schemaName === 'number') return 0;
    if (schemaName === 'boolean') return true;

    const schema = schemas[schemaName];
    if (!schema) return schemaName;

    const obj = {};
    if (schema.fields) {
        schema.fields.forEach(f => {
            if (f.ref) {
                // Если поле ссылается на другой класс — рекурсия
                obj[f.name] = buildSampleJson(f.ref, schemas, depth + 1);
            } else {
                // Подставляем осмысленные значения вместо имени типа
                if (f.type === 'string') obj[f.name] = 'string';
                else if (f.type === 'number') obj[f.name] = 0;
                else if (f.type === 'boolean') obj[f.name] = true;
                else if (f.type === 'array') obj[f.name] = [];
                else obj[f.name] = f.type;
            }
        });
    }
    return obj;
}

function renderSchemas(schemas) {
    const container = document.getElementById('schemas-container');
    container.innerHTML = '';

    for (const [schemaName, schema] of Object.entries(schemas)) {
        const details = document.createElement('details');
        details.className = 'schema-item';

        const summary = document.createElement('summary');
        summary.innerHTML = `<span class="schema-name">${schemaName}</span>`;

        const content = document.createElement('div');
        content.className = 'schema-content';

        // Генерируем описание полей
        let fieldsHtml = '<div class="schema-fields">';
        if (schema.fields) {
            schema.fields.forEach(f => {
                fieldsHtml += `
                    <div class="schema-field-row">
                        <span class="field-name">${f.name}</span>
                        <span class="field-type">${f.ref ? f.ref : f.type}</span>
                    </div>
                `;
            });
        }
        fieldsHtml += '</div>';

        content.innerHTML = fieldsHtml;
        details.appendChild(summary);
        details.appendChild(content);
        container.appendChild(details);
    }
}

// Выполнение реального запроса к API
async function executeRequest(index, method, path) {
    // Берём URL из поля ввода, если оно есть
    const baseUrlInput = document.getElementById('base-url');
    const baseUrl = baseUrlInput ? baseUrlInput.value.replace(/\/$/, "") : currentBaseUrl.replace(/\/$/, "");
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