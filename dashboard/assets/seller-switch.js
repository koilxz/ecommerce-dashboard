/* ============================================================
   DataPeak 店铺视角切换（前端契约 §9）
   纯 <script> 加载，暴露全局：initSellerSwitch()
   依赖全局：apiFetch（auth.js）、curSeller（index.html 内声明）、loadDashboard 等
   功能：
     - 拉取公开接口 /sellers 填充 #sellerView 下拉
     - 切换时设置 curSeller 并整体重载看板（含订单类与受保护接口）
   ============================================================ */

/**
 * 初始化店铺视角下拉。/sellers 为公开接口，无需 token。
 */
async function initSellerSwitch() {
  var sel = document.getElementById('sellerView');
  if (!sel) return;

  try {
    var sellers = await apiFetch('/sellers');   // 公开，无需 X-Token
    if (sellers && sellers.length) {
      sel.innerHTML = '<option value="">全部平台</option>' +
        sellers.map(function (s) {
          var id = s.sellerId != null ? s.sellerId : '';
          var name = s.shopName || (s.city ? (s.city + (s.state ? '-' + s.state : '')) : id);
          return '<option value="' + id + '">' + name + '</option>';
        }).join('');
    }
  } catch (err) {
    // 后端未启动时不阻塞页面，下拉保留“全部平台”
    console.warn('卖家列表加载失败:', err);
  }

  sel.onchange = function () {
    curSeller = sel.value;          // 全局当前店铺；空 = 全平台
    reloadAllDashboard();
  };
}

/**
 * 切换店铺后整体重载看板数据（公开接口 + 受保护接口一并切片到该店铺）。
 * 若当前正停留在销售/用户/商品分析页，连同该页一起重载（这些页同样按 curSeller 切片）。
 */
function reloadAllDashboard() {
  if (typeof loadDashboard === 'function') loadDashboard();
  if (typeof loadDiagnosis === 'function') loadDiagnosis();
  if (typeof loadAlerts === 'function') loadAlerts();
  if (typeof loadSignals === 'function') loadSignals();
  if (typeof loadActionList === 'function') loadActionList();

  var active = document.querySelector('.page.active');
  var page = active ? active.id.replace('page-', '') : '';
  if (page === 'sales' && typeof loadSalesAnalysis === 'function') loadSalesAnalysis();
  else if (page === 'users' && typeof loadUserAnalysis === 'function') loadUserAnalysis();
  else if (page === 'reports' && typeof loadReports === 'function') loadReports();
  else if (page === 'products' && typeof loadProducts === 'function') {
    if (typeof loadProductsKpi === 'function') loadProductsKpi();
    loadProducts();
  }
}
