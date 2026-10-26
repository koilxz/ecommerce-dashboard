/* ============================================================
   DataPeak 本期经营诊断（前端契约 §4）
   纯 <script> 加载，暴露全局：loadDiagnosis()
   依赖全局：apiFetch（auth.js）、curSeller（index.html 内声明）
   渲染目标：#diagPeriod / #diagConclusion / #diagSignals
   ============================================================ */

/**
 * 拉取 /diagnosis 并渲染诊断卡片。
 * 周期取当前看板筛选的起止日期（无则留空，由后端决定“所选周期”）。
 */
async function loadDiagnosis() {
  var period = '';
  var sd = document.getElementById('dashStartDate');
  var ed = document.getElementById('dashEndDate');
  if (sd && ed) {
    period = 'startDate=' + encodeURIComponent(sd.value) +
             '&endDate=' + encodeURIComponent(ed.value);
  }
  var path = '/diagnosis?' + period +
             (period ? '&' : '') + 'sellerId=' + encodeURIComponent(curSeller || '');

  try {
    var data = await apiFetch(path, { needAuth: true });
    if (!data) return;

    var periodEl = document.getElementById('diagPeriod');
    var conclEl = document.getElementById('diagConclusion');
    var sigEl = document.getElementById('diagSignals');

    if (periodEl) {
      periodEl.textContent = (data.period && data.period.label) ? data.period.label : '所选周期';
    }
    if (conclEl) {
      // conclusionText 已是服务端拼装的“人话结论”，前端只渲染
      conclEl.innerHTML = data.conclusionText || '暂无诊断结论';
    }
    // D：诊断卡顶部结论条（一句话结论 + 建议动作，卖家语言）
    var barEl = document.getElementById('diagConclusionBar');
    if (barEl) barEl.innerHTML = diagModuleConclusion(data);
    // ③④ 评分 / 履约 图（复用同一份 diagnosis 数据，避免重复请求）
    if (typeof renderHealthCharts === 'function') renderHealthCharts(data);
    // 缓存，供今日行动清单等复用
    window._dpDiagnosis = data;
    if (sigEl) {
      var signals = data.signals || [];
      sigEl.innerHTML = signals.map(function (s) {
        var lvl = s.level || 'gray';
        var label = s.label || '';
        var val = s.value != null ? s.value : '';
        return '<span class="chip chip-' + lvl + '">' + label + ' · ' + val + '</span>';
      }).join('');
    }
  } catch (err) {
    // 401 等鉴权错误已由 apiFetch 处理（跳转登录）；其余静默，不阻塞看板
    if (err.message !== '未登录' && err.message !== '登录失效') {
      console.warn('诊断加载失败:', err);
    }
  }
}

/**
 * 生成诊断卡顶部结论条（D）：一句话结论 + 建议动作，卖家语言、无术语。
 * @param {object} data /diagnosis 返回的 DiagnosisDTO
 * @returns {string} HTML
 */
function diagModuleConclusion(data) {
  var k = (data && data.kpi) || {};
  var g = (k.salesGrowth == null) ? null : Number(k.salesGrowth);
  var score = (k.avgReviewScore == null) ? null : Number(k.avgReviewScore);
  var conclusion, action;
  if (g == null) {
    conclusion = '本期生意数据暂缺';
    action = '确认后端数据后重试';
  } else if (g >= 0) {
    conclusion = '生意在涨（销售额环比 +' + g.toFixed(1) + '%）';
    action = '趁势加码，把货和上新往增长品类倾斜';
  } else {
    conclusion = '生意在跌（销售额环比 ' + g.toFixed(1) + '%）';
    action = '排查下跌品类，考虑清仓 / 停补，稳住现金流';
  }
  if (score != null && score < 3.5) {
    action += '；评分偏低，优先查详情页与质量';
  }
  return '<b>结论：</b>' + conclusion + '　<b>建议：</b>' + action;
}
