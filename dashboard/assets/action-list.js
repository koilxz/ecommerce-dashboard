/* ============================================================
   DataPeak 今日行动清单（增强版 spec C）
   纯 <script> 加载，暴露全局：loadActionList()
   依赖全局：apiFetch（auth.js）、curSeller（index.html）、_dpPeriod（signals-alerts.js）

   数据来源（全部为现有接口，不新增后端）：
     - GET /diagnosis  -> signals（信号）
     - GET /alerts     -> 前 2 条异常预警
     - GET /signals    -> 前 2 条补货 / 促销信号
   合成 3 条建议动作，格式：[动作] 具体做法（依据）。
   例：清仓促销：酷玩品类降价冲量（环比 -41.9%）。
   ⚠️ 数据边界：本数据集无库存/成本/流量/投放字段，动作建议只基于销售额与评分趋势，不得写成"清库存""加投放"。
   ============================================================ */

/* 后端 action -> 卖家语言的动作标签 */
var ACTION_TAG_MAP = {
  '促销去化': '清仓促销',
  '重点投入': '加注主推',
  '观察': '持续观察'
};

/**
 * 拉取诊断 / 预警 / 补货信号，合成“今日行动清单”。
 * 三个请求并行，互不阻塞；单个失败不影响其余。
 */
async function loadActionList() {
  var box = document.getElementById('actionList');
  var periodEl = document.getElementById('actionPeriod');
  if (!box) return;
  box.innerHTML = '<div class="action-loading">正在根据诊断 / 预警 / 补货信号生成建议…</div>';

  var p = (typeof _dpPeriod === 'function') ? _dpPeriod() : '';
  var sid = 'sellerId=' + encodeURIComponent(curSeller || '');
  function q(path) { return path + '?' + (p ? p + '&' : '') + sid; }

  var actions = [];
  try {
    var results = await Promise.all([
      apiFetch(q('/diagnosis'), { needAuth: true }).catch(function () { return null; }),
      apiFetch(q('/alerts'), { needAuth: true }).catch(function () { return []; }),
      apiFetch(q('/signals'), { needAuth: true }).catch(function () { return []; })
    ]);
    var diag = results[0];
    var alerts = results[1] || [];
    var signals = results[2] || [];

    if (periodEl && diag && diag.period && diag.period.label) {
      periodEl.textContent = diag.period.label;
    }

    // 1) 补货 / 促销信号（最直接的动作，优先）
    (signals || []).slice(0, 2).forEach(function (s) {
      var tag = ACTION_TAG_MAP[s.action] || '运营动作';
      var level = (s.action === '促销去化') ? 'red'
        : (s.action === '重点投入' ? 'green' : 'gray');
      var growth = (s.growthRate != null) ? ('环比 ' + (s.growthRate >= 0 ? '+' : '') + s.growthRate + '%') : '环比 -';
      var share = (s.share != null) ? ('占比 ' + s.share + '%') : '占比 -';
      actions.push({
        level: level,
        tag: tag,
        text: s.category + '（' + growth + ' · ' + share + '）'
      });
    });

    // 2) 异常预警（爆雷风险）
    (alerts || []).slice(0, 2).forEach(function (a) {
      actions.push({
        level: (a.severity === 'red') ? 'red' : 'orange',
        tag: '异常预警',
        text: (a.title || '异常') + '：' + (a.detail || '')
      });
    });

    // 3) 诊断信号兜底（复购率 / 评分等），补足到 3 条
    if (diag && diag.signals) {
      diag.signals.forEach(function (s) {
        if (actions.length >= 3) return;
        var label = s.label || '';
        if (label.indexOf('复购') >= 0 || label.indexOf('拉新') >= 0) {
          actions.push({
            level: s.level || 'orange',
            tag: '拉新召回',
            text: label + ' ' + (s.value || '') + '，低于健康线，建议做老客召回 / 新人券'
          });
        } else if (label.indexOf('评分') >= 0 || label.indexOf('口碑') >= 0) {
          actions.push({
            level: s.level || 'orange',
            tag: '口碑守护',
            text: label + ' ' + (s.value || '') + '，建议核查详情页与商品质量'
          });
        }
      });
    }
  } catch (err) {
    console.warn('行动清单生成失败:', err);
  }

  // 兜底：无任何信号时给出中性建议
  if (!actions.length) {
    actions.push({ level: 'gray', tag: '保持观察', text: '当前周期未发现需立即处理的异常，维持现有运营节奏' });
  }
  actions = actions.slice(0, 3);

  box.innerHTML = actions.map(function (a, i) {
    return '<div class="action-item action-' + a.level + '">' +
      '<span class="action-num">' + (i + 1) + '</span>' +
      '<div class="action-body">' +
      '<div class="action-tag">' + a.tag + '</div>' +
      '<div class="action-text">' + a.text + '</div>' +
      '</div></div>';
  }).join('');
}
