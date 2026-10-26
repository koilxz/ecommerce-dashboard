/* ============================================================
   DataPeak 全局鉴权与请求封装（前端契约 §1）
   纯 <script> 加载（非 ES module），以下符号暴露为全局：
     API_BASE / getToken / setToken / apiFetch / showToast
   注意：index.html 原本自带 const API_BASE 与 function showToast，
   为避免重复声明冲突，本文件为唯一权威来源，index.html 已移除其副本。
   ============================================================ */

/* 后端基址。
   ⚠️ 必须用 127.0.0.1，不能改成 localhost：
   WorkBuddy 预览面板的 CSP connect-src 只放行 http://127.0.0.1:*，
   写 localhost 会被浏览器策略直接拦掉 fetch（表现=登录提示"用户名或密码错误"）。 */
var API_BASE = 'http://127.0.0.1:8080/api';

/* token 在 sessionStorage 中的键名 */
var TOKEN_KEY = 'datapk_token';

/* ---------- token 管理 ---------- */
function getToken() {
  return sessionStorage.getItem(TOKEN_KEY);
}

function setToken(t) {
  if (t) sessionStorage.setItem(TOKEN_KEY, t);
  else sessionStorage.removeItem(TOKEN_KEY);
}

/* 请求超时（毫秒）。后端聚合基于 673MB H2 数据集，实测热响应：
   kpi≈17s / alerts≈19s / diagnosis≈11s / signals≈18s，冷启动更久。
   原 3s 会令所有真实请求误判超时并静默走 mock 兜底，故按实测上限放宽至 60s
   （仍保留超时/断网兜底与 backendAvailable 短路逻辑）。 */
var FETCH_TIMEOUT_MS = 60000;

/* ---------- 统一请求封装 ---------- */
/**
 * 统一请求：自动带 X-Token；FETCH_TIMEOUT_MS 超时；401 清 token 并跳登录；网络错误 toast。
 * @param {string} path 接口路径，例如 '/orders/kpi?...' 或 '/diagnosis?...'
 * @param {object} [opts]
 * @param {string} [opts.method='GET']
 * @param {object|null} [opts.body]
 * @param {boolean} [opts.needAuth=false] 受保护接口（/diagnosis /signals /alerts）必须传 true
 * @returns {Promise<any>} 解析为 ApiResponse.data
 */
async function apiFetch(path, opts) {
  opts = opts || {};
  var method = opts.method || 'GET';
  var body = opts.body || null;
  var needAuth = opts.needAuth || false;

  var headers = { 'Content-Type': 'application/json' };
  if (needAuth) {
    var t = getToken();
    if (!t) {
      location.href = 'welcome.html';
      throw new Error('未登录');
    }
    headers['X-Token'] = t;
  }

  try {
    var res = await fetch(API_BASE + path, {
      method: method,
      headers: headers,
      body: body ? JSON.stringify(body) : null,
      signal: AbortSignal.timeout(FETCH_TIMEOUT_MS)
    });

    if (res.status === 401) {
      setToken(null);
      location.href = 'welcome.html';
      throw new Error('登录失效');
    }
    if (!res.ok) throw new Error('HTTP ' + res.status);

    var json = await res.json();
    // 业务错误（code !== 200）按契约提示 message
    if (json && json.code !== undefined && json.code !== 200) {
      showToast(json.message || '请求失败', 'error');
      throw new Error(json.message || '业务错误');
    }
    return json ? json.data : null;
  } catch (err) {
    // 鉴权类错误直接上抛，交由调用方/页面处理
    if (err.message === '未登录' || err.message === '登录失效') throw err;
    // 超时 / 网络错误
    showToast('数据加载失败，请确认后端已启动', 'error');
    throw err;
  }
}

/* ---------- 轻量 Toast（契约 §1 / §7） ---------- */
/**
 * 页面底部提示。
 * @param {string} msg 文本
 * @param {string} [type] 'success' | 'error' | ''（默认）
 */
function showToast(msg, type) {
  var t = document.getElementById('toast');
  if (!t) return;
  t.textContent = msg;
  t.className = 'toast show ' + (type || '');
  setTimeout(function () { t.classList.remove('show'); }, 3000);
}
