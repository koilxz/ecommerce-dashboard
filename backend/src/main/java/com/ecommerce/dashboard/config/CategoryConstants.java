package com.ecommerce.dashboard.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 类别常量：
 *  - 支付方式 英文→中文 映射
 *  - 商品品类 葡萄牙语→中文 内置映射（Olist 的 product_category_name 为葡萄牙语，
 *    中文名无权威数据源，此处为「内置映射」约定 D1，覆盖数据集全部 73 个品类）
 *  - 商品名合成工具
 */
public final class CategoryConstants {

    /** 支付方式 英文→中文（Olist 常见值） */
    public static final Map<String, String> PAYMENT_ZH = new HashMap<>();

    /** 商品品类 葡萄牙语→中文（内置映射，覆盖全部品类） */
    public static final Map<String, String> CATEGORY_ZH = new HashMap<>();

    static {
        PAYMENT_ZH.put("credit_card", "信用卡");
        PAYMENT_ZH.put("boleto", "Boleto");
        PAYMENT_ZH.put("voucher", "代金券");
        PAYMENT_ZH.put("debit_card", "借记卡");
        PAYMENT_ZH.put("not_defined", "其他");

        // ===== Olist 全部品类 葡萄牙语 → 中文 =====
        CATEGORY_ZH.put("beleza_saude", "健康美容");
        CATEGORY_ZH.put("informatica_acessorios", "电脑配件");
        CATEGORY_ZH.put("automotivo", "汽车");
        CATEGORY_ZH.put("cama_mesa_banho", "床品卫浴");
        CATEGORY_ZH.put("moveis_decoracao", "家具装饰");
        CATEGORY_ZH.put("esporte_lazer", "运动休闲");
        CATEGORY_ZH.put("perfumaria", "香水美妆");
        CATEGORY_ZH.put("utilidades_domesticas", "家居用品");
        CATEGORY_ZH.put("telefonia", "通讯");
        CATEGORY_ZH.put("relogios_presentes", "手表礼品");
        CATEGORY_ZH.put("alimentos_bebidas", "食品饮料");
        CATEGORY_ZH.put("bebes", "母婴");
        CATEGORY_ZH.put("papelaria", "文具");
        CATEGORY_ZH.put("tablets_impressao_imagem", "平板打印影像");
        CATEGORY_ZH.put("brinquedos", "玩具");
        CATEGORY_ZH.put("telefonia_fixa", "固话");
        CATEGORY_ZH.put("ferramentas_jardim", "园艺工具");
        CATEGORY_ZH.put("fashion_bolsas_e_acessorios", "箱包配饰");
        CATEGORY_ZH.put("eletroportateis", "小家电");
        CATEGORY_ZH.put("consoles_games", "游戏主机");
        CATEGORY_ZH.put("pc_gamer", "PC游戏");
        CATEGORY_ZH.put("audio", "音响");
        CATEGORY_ZH.put("fashion_calcados", "鞋靴");
        CATEGORY_ZH.put("cool_stuff", "酷玩");
        CATEGORY_ZH.put("malas_acessorios", "行李配饰");
        CATEGORY_ZH.put("climatizacao", "空调");
        CATEGORY_ZH.put("construcao_ferramentas_construcao", "建筑工具");
        CATEGORY_ZH.put("moveis_cozinha_area_de_servico_jantar_e_jardim", "厨卫餐居园艺家具");
        CATEGORY_ZH.put("construcao_ferramentas_jardim", "园艺建材");
        CATEGORY_ZH.put("fashion_roupa_masculina", "男装");
        CATEGORY_ZH.put("pet_shop", "宠物用品");
        CATEGORY_ZH.put("moveis_escritorio", "办公家具");
        CATEGORY_ZH.put("market_place", "集市");
        CATEGORY_ZH.put("eletronicos", "电子");
        CATEGORY_ZH.put("eletrodomesticos", "大家电");
        CATEGORY_ZH.put("artigos_de_festas", "派对用品");
        CATEGORY_ZH.put("casa_conforto", "家居舒适");
        CATEGORY_ZH.put("construcao_ferramentas_ferramentas", "手工具");
        CATEGORY_ZH.put("agro_industria_e_comercio", "农工商");
        CATEGORY_ZH.put("moveis_colchao_e_estofado", "床垫软装");
        CATEGORY_ZH.put("livros_tecnicos", "技术书籍");
        CATEGORY_ZH.put("casa_construcao", "家居建材");
        CATEGORY_ZH.put("instrumentos_musicais", "乐器");
        CATEGORY_ZH.put("moveis_sala", "客厅家具");
        CATEGORY_ZH.put("construcao_ferramentas_iluminacao", "建材照明");
        CATEGORY_ZH.put("industria_comercio_e_negocios", "工商商务");
        CATEGORY_ZH.put("alimentos", "食品");
        CATEGORY_ZH.put("artes", "艺术");
        CATEGORY_ZH.put("moveis_quarto", "卧室家具");
        CATEGORY_ZH.put("livros_interesse_geral", "大众读物");
        CATEGORY_ZH.put("construcao_ferramentas_seguranca", "建材安防");
        CATEGORY_ZH.put("fashion_underwear_e_moda_praia", "内衣泳装");
        CATEGORY_ZH.put("fashion_esporte", "运动服饰");
        CATEGORY_ZH.put("sinalizacao_e_seguranca", "标识安防");
        CATEGORY_ZH.put("pcs", "电脑");
        CATEGORY_ZH.put("artigos_de_natal", "圣诞用品");
        CATEGORY_ZH.put("fashion_roupa_feminina", "女装");
        CATEGORY_ZH.put("eletrodomesticos_2", "大家电(二)");
        CATEGORY_ZH.put("livros_importados", "进口书籍");
        CATEGORY_ZH.put("bebidas", "酒水饮料");
        CATEGORY_ZH.put("cine_foto", "影音摄影");
        CATEGORY_ZH.put("la_cuisine", "厨具");
        CATEGORY_ZH.put("musica", "音乐");
        CATEGORY_ZH.put("casa_conforto_2", "家居舒适(二)");
        CATEGORY_ZH.put("portateis_casa_forno_e_cafe", "便携烤箱咖啡机");
        CATEGORY_ZH.put("portateis_cozinha_e_preparadores_de_alimentos", "便携厨房料理机");
        CATEGORY_ZH.put("cds_dvds_musicais", "音像");
        CATEGORY_ZH.put("dvds_blu_ray", "影碟");
        CATEGORY_ZH.put("flores", "花卉");
        CATEGORY_ZH.put("artes_e_artesanato", "手工艺");
        CATEGORY_ZH.put("fraldas_higiene", "尿裤卫生");
        CATEGORY_ZH.put("fashion_roupa_infanto_juvenil", "童装");
        CATEGORY_ZH.put("seguros_e_servicos", "保险服务");
    }

    private CategoryConstants() {
    }

    /** 支付方式中文名（未知原样返回） */
    public static String paymentZh(String en) {
        if (en == null || en.isEmpty()) {
            return "未知";
        }
        return PAYMENT_ZH.getOrDefault(en, en);
    }

    /** 品类中文名（葡萄牙语键，无映射时回退原值） */
    public static String categoryZh(String pt) {
        if (pt == null || pt.isEmpty()) {
            return "未分类";
        }
        return CATEGORY_ZH.getOrDefault(pt, pt);
    }

    /** 中文品类名 → 葡萄牙语键（用于把前端中文筛选条件转回数据库存储值） */
    public static String zhToCategory(String zh) {
        if (zh == null || zh.isEmpty() || "all".equalsIgnoreCase(zh)) {
            return null;
        }
        for (Map.Entry<String, String> e : CATEGORY_ZH.entrySet()) {
            if (e.getValue().equals(zh)) {
                return e.getKey();
            }
        }
        // 未命中则原样返回（极端情况不会匹配任何行，安全降级）
        return zh;
    }

    /** 商品友好名：中文品类 + 商品ID后6位（Olist 无商品名，诚实合成） */
    public static String productName(String categoryZh, String productId) {
        if (productId == null) {
            return categoryZh;
        }
        String tail = productId.length() > 6 ? productId.substring(productId.length() - 6) : productId;
        return categoryZh + " #" + tail;
    }
}
