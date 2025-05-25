// KHAN OVERDRIVE - GAMA HACKER VERSION
let loadedPlugins = [];

console.clear();
const noop = () => {};
console.warn = console.error = window.debug = noop;

const splashScreen = document.createElement('div');

class EventEmitter {
  constructor() { this.events = {}; }
  on(t, e) {
    (Array.isArray(t) ? t : [t]).forEach(t => {
      (this.events[t] = this.events[t] || []).push(e);
    });
  }
  off(t, e) {
    (Array.isArray(t) ? t : [t]).forEach(t => {
      this.events[t] && (this.events[t] = this.events[t].filter(h => h !== e));
    });
  }
  emit(t, ...e) {
    this.events[t]?.forEach(h => h(...e));
  }
  once(t, e) {
    const s = (...i) => {
      e(...i);
      this.off(t, s);
    };
    this.on(t, s);
  }
}

const plppdo = new EventEmitter();

new MutationObserver(mutationsList => 
  mutationsList.some(m => m.type === 'childList') && plppdo.emit('domChanged')
).observe(document.body, { childList: true, subtree: true });

const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
const findAndClickBySelector = selector => document.querySelector(selector)?.click();

function sendToast(text, duration = 3000, gravity = 'bottom') {
  Toastify({
    text,
    duration,
    gravity,
    position: "center",
    stopOnFocus: true,
    style: { background: "#00ff00", color: "black", fontWeight: "bold" }
  }).showToast();
}

async function showSplashScreen() {
  splashScreen.style.cssText = `
    position:fixed;top:0;left:0;width:100%;height:100%;
    background-color:black;display:flex;align-items:center;
    justify-content:center;z-index:9999;opacity:0;
    transition:opacity 0.5s ease;user-select:none;
    color:#00ff00;font-family:monospace;font-size:30px;text-align:center;
    animation: blink 1s step-start 0s infinite;
  `;
  splashScreen.innerHTML = '<div><span>GAMA HACKER</span><br><small>Iniciando...</small></div>';
  document.body.appendChild(splashScreen);
  setTimeout(() => splashScreen.style.opacity = '1', 10);
  const style = document.createElement('style');
  style.textContent = `
    @keyframes blink {
      50% { opacity: 0.1; }
    }
  `;
  document.head.appendChild(style);
}

async function hideSplashScreen() {
  splashScreen.style.opacity = '0';
  setTimeout(() => splashScreen.remove(), 500);
}

async function loadScript(url, label) {
  const response = await fetch(url);
  const script = await response.text();
  loadedPlugins.push(label);
  eval(script);
}

async function loadCss(url) {
  return new Promise(resolve => {
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.type = 'text/css';
    link.href = url;
    link.onload = resolve;
    document.head.appendChild(link);
  });
}

function setupMain() {
  const originalFetch = window.fetch;
  window.fetch = async function(input, init) {
    let body;
    if (input instanceof Request) {
      body = await input.clone().text();
    } else if (init?.body) {
      body = init.body;
    }
    if (body?.includes('"operationName":"updateUserVideoProgress"')) {
      try {
        let bodyObj = JSON.parse(body);
        if (bodyObj.variables?.input) {
          const durationSeconds = bodyObj.variables.input.durationSeconds;
          bodyObj.variables.input.secondsWatched = durationSeconds;
          bodyObj.variables.input.lastSecondWatched = durationSeconds;
          body = JSON.stringify(bodyObj);
          if (input instanceof Request) {
            input = new Request(input, { body });
          } else {
            init.body = body;
          }
          sendToast("⚡ Vídeo completado automáticamente!", 1000);
        }
      } catch (e) {}
    }
    const originalResponse = await originalFetch.apply(this, arguments);
    try {
      const clonedResponse = originalResponse.clone();
      const responseBody = await clonedResponse.text();
      let responseObj = JSON.parse(responseBody);
      if (responseObj?.data?.assessmentItem?.item?.itemData) {
        let itemData = JSON.parse(responseObj.data.assessmentItem.item.itemData);
        if (itemData.question.content[0] === itemData.question.content[0].toUpperCase()) {
          itemData.answerArea = {
            calculator: false,
            chi2Table: false,
            periodicTable: false,
            tTable: false,
            zTable: false
          };
          itemData.question.content = "Hackeado por: GAMA HACKER [[☃ radio 1]]";
          itemData.question.widgets = {
            "radio 1": {
              type: "radio",
              options: {
                choices: [{ content: "💚", correct: true }]
              }
            }
          };
          responseObj.data.assessmentItem.item.itemData = JSON.stringify(itemData);
          return new Response(JSON.stringify(responseObj), {
            status: originalResponse.status,
            statusText: originalResponse.statusText,
            headers: originalResponse.headers
          });
        }
      }
    } catch (e) {}
    return originalResponse;
  };

  (async () => {
    const selectors = [
      `[data-testid="choice-icon__library-choice-icon"]`,
      `[data-testid="exercise-check-answer"]`,
      `[data-testid="exercise-next-question"]`,
      `._1udzurba`,
      `._awve9b`
    ];
    window.khanwareDominates = true;
    let errorCounter = 0;

    while (window.khanwareDominates) {
      for (const selector of selectors) {
        findAndClickBySelector(selector);

        const errorBox = document.querySelector('[data-test-id="error-message"]');
        if (errorBox?.innerText.includes("Oh no") || errorBox?.innerText.includes("GRAPHQL_ERROR")) {
          sendToast("❌ Error detectado. Reiniciando bot.", 5000);
          window.khanwareDominates = false;
          return;
        }

        const element = document.querySelector(`${selector}> div`);
        if (element?.innerText === "Mostrar resumo") {
          sendToast("✅ Exercício finalizado!");
        }
      }
      await delay(300); // Velocidad aumentada
    }
  })();
}

if (!/^https?:\/\/([a-z0-9-]+\.)?khanacademy\.org/.test(window.location.href)) {
  window.location.href = "https://pt.khanacademy.org/";
} else {
  (async function init() {
    await showSplashScreen();
    await Promise.all([
      loadScript('https://cdn.jsdelivr.net/npm/darkreader@4.9.92/darkreader.min.js', 'darkReaderPlugin').then(()=>{ DarkReader.setFetchMethod(window.fetch); DarkReader.enable(); }),
      loadCss('https://cdn.jsdelivr.net/npm/toastify-js/src/toastify.min.css'),
      loadScript('https://cdn.jsdelivr.net/npm/toastify-js', 'toastifyPlugin')
    ]);
    await delay(1500);
    await hideSplashScreen();
    setupMain();
    sendToast("💚｜KHAN OVERDRIVE INICIADO!");
    console.clear();
  })();
}
