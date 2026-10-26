/* ============================================================
   DataPeak 术语解释气泡（前端契约 §6）
   纯 <script> 加载，暴露全局：GLOSSARY / wireGlossary()
   用法：在需要解释的标题上写 <span class="term" data-term="客单价">客单价</span>，
   页面加载后调用 wireGlossary() 即可自动追加 ⓘ 图标（title 为通俗易懂的解释）。
   ============================================================ */

/* 术语 → 人话映射表（全局复用） */
var GLOSSARY = {
  '客单价':   '平均每个订单花了多少钱',
  '复购率':   '老客户回头再买的比例',
  '环比':     '和上一个周期比，涨了还是跌了',
  '同比增长': '和去年同一段时间比，涨了还是跌了',
  'TOP10':    '卖得最好的前 10 名',
  '退单率':   '取消的订单占总订单的比例',
  '物流延误率':'实际送达晚于预计送达的订单占比'
};

/**
 * 扫描页面内所有 [data-term] 元素，追加 ⓘ 解释图标。
 * 若 data-term 命中 GLOSSARY，则使用对应解释；否则取元素自身文本。
 * 重复调用安全（已处理过的元素会打标跳过）。
 */
function wireGlossary() {
  var nodes = document.querySelectorAll('[data-term]');
  nodes.forEach(function (el) {
    if (el.dataset.glossaryWired) return;           // 避免重复注入
    var key = el.getAttribute('data-term');
    var tip = GLOSSARY[key] || el.getAttribute('title') || key;
    var icon = document.createElement('i');
    icon.className = 'info-icon';
    icon.textContent = 'ⓘ';
    icon.title = tip;
    el.appendChild(icon);
    el.dataset.glossaryWired = '1';
  });
}
