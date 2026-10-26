/* ============================================================
   DataPeak 信号 / 预警（前端契约 §5）
   纯 <script> 加载，暴露全局：loadAlerts() / loadSignals()
   依赖全局：apiFetch（auth.js）、curSeller（index.html 内声明）
   渲染目标：
     - loadAlerts  -> #alertBar（页头固定横条）
     - loadSignals -> #restockList（补货建议，含“推测口径，非实时库存”角标）
   两者互不耦合，与诊断卡片也解耦。
   ============================================================ */

/* 取当前看板周期的查询串（无日期则留空） */
function _dpPeriod() {
  var sd = document.getElementById('dashStartDate');
  var ed = document.getElementById('dashEndDate');
  if (sd && ed) {
    return 'startDate=' + encodeURIComponent(sd.value) +
           '&endDate=' + encodeURIComponent(ed.value);
  }
  return '';
}

/**
 * 拉取 /alerts，渲染页头预警条。无预警则隐藏。
 */
async function loadAlerts() {
  var p = _dpPeriod();
  var path = '/alerts?' + p + (p ? '&' : '') + 'sellerId=' + encodeURIComponent(curSeller || '');
  var bar = document.getElementById('alertBar');
  if (!bar) return;

  try {
    var alerts = await apiFetch(path, { needAuth: true });
    if (!alerts || !alerts.length) {
      bar.hidden = true;
      bar.innerHTML = '';
      return;
    }
    bar.hidden = false;
    // D：预警条模块结论（一句话摘要 + 优先处理提示）
    var summary = '<span class="alert-item alert-summary">⚠ 检测到 ' + alerts.length +
      ' 条异常，建议优先处理下方标红项</span>';
    bar.innerHTML = summary + alerts.map(function (a) {
      var sev = a.severity || 'red';
      var title = a.title || '预警';
      var detail = a.detail || '';
      return '<span class="alert-item alert-' + sev + '">⚠ ' + title + '：' + detail + '</span>';
    }).join('');
  } catch (err) {
    if (err.message !== '未登录' && err.message !== '登录失效') {
      console.warn('预警加载失败:', err);
    }
    bar.hidden = true;
  }
}

/**
 * 拉取 /signals，渲染“补货建议”区块。每条信号展示 action，并带“推测口径”角标（note 字段）。
 */
async function loadSignals() {
  var p = _dpPeriod();
  var path = '/signals?' + p + (p ? '&' : '') + 'sellerId=' + encodeURIComponent(curSeller || '');
  var list = document.getElementById('restockList');
  if (!list) return;

  try {
    var signals = await apiFetch(path, { needAuth: true });
    // D：补货建议顶部结论条（卖家语言）
    var barEl = document.getElementById('restockConclusionBar');
    if (!signals || !signals.length) {
      list.innerHTML = '<div class="restock-note">暂无补货建议</div>';
      if (barEl) barEl.innerHTML = '<b>结论：</b>暂无明显补货 / 促销信号　<b>建议：</b>保持观察，按需补货';
      return;
    }
    if (barEl) barEl.innerHTML = restockConclusion(signals);
    list.innerHTML = signals.map(function (s) {
      var cat = s.category || '未知品类';
      var action = s.action || '观察';
      var note = s.note || '基于销售趋势推测，非实时库存';
      return '<div class="restock-card">' +
               '<span class="restock-badge">' + note + '</span>' +
               '<div class="restock-cat">' + cat + '</div>' +
               '<span class="restock-action">' + action + '</span>' +
               '<div class="restock-note">增速 ' + (s.growthRate != null ? s.growthRate + '%' : '-') +
               ' · 口碑 ' + (s.avgScore != null ? s.avgScore : '-') +
               ' · 占比 ' + (s.share != null ? s.share + '%' : '-') + '</div>' +
             '</div>';
    }).join('');
  } catch (err) {
    if (err.message !== '未登录' && err.message !== '登录失效') {
      console.warn('补货建议加载失败:', err);
    }
  }
}

/**
 * 生成补货建议顶部结论条（D）：从信号里挑最该做的一件事讲给卖家听。
 * @param {Array} signals /signals 返回的 CategorySignalDTO 数组
 * @returns {string} HTML
 */
function restockConclusion(signals) {
  var promo = null, boost = null;
  signals.forEach(function (s) {
    if (s.action === '促销去化' && !promo) promo = s;
    if (s.action === '重点投入' && !boost) boost = s;
  });
  var parts = [];
  if (promo) parts.push('「' + promo.category + '」环比 ' + (promo.growthRate != null ? promo.growthRate + '%' : '-') + '，建议降价清仓 / 停止补货');
  if (boost) parts.push('「' + boost.category + '」环比 +' + (boost.growthRate != null ? boost.growthRate + '%' : '-') + '，可加注主推');
  if (!parts.length && signals.length) {
    var s0 = signals[0];
    parts.push('「' + s0.category + '」建议「' + (s0.action || '观察') + '」');
  }
  return '<b>结论：</b>' + (parts.join('；') || '暂无明显信号') + '　<b>建议：</b>按下方品类动作执行（基于销售趋势推测，非实时库存）';
}
