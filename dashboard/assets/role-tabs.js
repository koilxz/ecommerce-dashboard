/* ============================================================
   DataPeak 三角色视角切换（增强版 spec B）
   纯 <script> 加载，暴露全局：initRoleTabs() / switchRole()
   依赖全局：window.dpResizeCharts（index.html 内提供，用于 resize 图表）

   角色 -> 模块可见性通过模块容器上的 data-role="owner ops dealer" 控制，
   纯 JS/CSS 显隐，不新增任何后端接口。
     - owner  店主 林姐：行动清单 / 诊断卡 / 补货信号 / 销售趋势 / 品类 / 评分
     - ops    代运营 小吴：行动清单 / 预警条 / 店铺对比 / 销售趋势 / 延迟取消
     - dealer 经销商 陈总：行动清单 / 品类增速 / 补货信号 / 支付方式 / 价格分布
   ============================================================ */

var DP_ROLE_KEY = 'dp_role';   // sessionStorage 键：记住上次视角

/**
 * 切换到指定角色视角：显隐 #page-dashboard 内带 data-role 的模块，并 resize 图表。
 * @param {string} role owner / ops / dealer
 */
function switchRole(role) {
  if (['owner', 'ops', 'dealer'].indexOf(role) < 0) role = 'owner';
  window.curRole = role;

  document.querySelectorAll('#page-dashboard [data-role]').forEach(function (el) {
    var roles = (el.getAttribute('data-role') || '').split(/\s+/);
    el.style.display = (roles.indexOf(role) >= 0) ? '' : 'none';
  });

  document.querySelectorAll('#roleTabs .role-tab').forEach(function (t) {
    t.classList.toggle('active', t.getAttribute('data-role') === role);
  });

  try { sessionStorage.setItem(DP_ROLE_KEY, role); } catch (e) { /* ignore */ }

  // 隐藏容器中的图表显示后需重算尺寸（ECharts 在 0 尺寸下初始化，显示后 resize）
  setTimeout(function () {
    if (typeof window.dpResizeCharts === 'function') window.dpResizeCharts();
  }, 60);
}

/**
 * 绑定角色 tab 点击事件，并按上次记忆 / 默认“店主视角”初始化。
 */
function initRoleTabs() {
  var tabs = document.querySelectorAll('#roleTabs .role-tab');
  tabs.forEach(function (t) {
    t.addEventListener('click', function () {
      switchRole(t.getAttribute('data-role'));
    });
  });
  var saved = 'owner';
  try { saved = sessionStorage.getItem(DP_ROLE_KEY) || 'owner'; } catch (e) { /* ignore */ }
  switchRole(saved);
}
