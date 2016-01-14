package com.jfinalshop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.bean.TenpayConfig;
import com.jfinalshop.bean.TenpayConfig.TenpayType;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 实体类 - 支付配置
 * 
 */
public class PaymentConfig extends Model<PaymentConfig>{

	private static final long serialVersionUID = -7406788872981811948L;
	
	public static final PaymentConfig dao = new PaymentConfig();

	// 支付配置类型（预存款、线下支付、财付通）
	public enum PaymentConfigType {
		deposit, offline, tenpay, alipay
	};
	
	// 支付手续费类型（按比例收费、固定费用）
	public enum PaymentFeeType {
		scale, fixed
	}
	
	private String configObjectStore;// 配置对象信息储存
		
	// 返回字符scale, fixed
	public PaymentFeeType getPaymentFeeType(){
		return PaymentFeeType.values()[getInt("paymentFeeType")];
	}
	
	public void setPaymentFeeType(String paymentFeeType) {
		set("paymentFeeType", PaymentFeeType.valueOf(paymentFeeType).ordinal());
	}
	
	// 返回字符deposit, offline, tenpay
	public PaymentConfigType getPaymentConfigType(){
		return PaymentConfigType.values()[getInt("paymentConfigType")];
	}
	
	public void setPaymentConfigType(String paymentConfigType) {
		set("paymentConfigType", PaymentConfigType.valueOf(paymentConfigType).ordinal());
	}
	
	// 获取配置对象
	public Object getConfigObject() {
		configObjectStore = getStr("configObjectStore");
		if (StringUtils.isEmpty(configObjectStore)) {
			return null;
		}
		Object objectConfig = JSON.parseObject(configObjectStore, Object.class);
		if (getPaymentConfigType() == PaymentConfigType.deposit) {
			return null;
		} else if (getPaymentConfigType() == PaymentConfigType.offline) {
			return null;
		} else if (getPaymentConfigType() == PaymentConfigType.tenpay) {
			return objectConfig;
		} else if (getPaymentConfigType() == PaymentConfigType.alipay) {
			return objectConfig;
		}
		return null;
	}
		
	// 设置配置对象
	public void setConfigObject(Object object) {
		if (object == null) {
			set("configObjectStore", null);
			return;
		}
		String text = JSON.toJSONString(object); 
		if (getPaymentConfigType() == PaymentConfigType.deposit) {
			set("configObjectStore", null);
		} else if (getPaymentConfigType() == PaymentConfigType.offline) {
			set("configObjectStore", null);
		} else if (getPaymentConfigType() == PaymentConfigType.tenpay) {
			set("configObjectStore", text);
		} else if (getPaymentConfigType() == PaymentConfigType.alipay) {
			set("configObjectStore", text);
		}
	}
	
	
	 //获取所有实体对象总数.	 
	public Long getTotalCount(){
		return Db.queryLong("select count(*) from paymentconfig");		
	}
	
	// 获取所有支付方式
	public List<PaymentConfig> getAll() {
		return dao.find("select * from paymentconfig");
	}
	
	/**
	 * 获取非预存款、线下支付方式的支付配置
	 * 
	 */
	public List<PaymentConfig> getNonDepositOfflinePaymentConfigList() {
		String sql = "select * from PaymentConfig where paymentConfigType != ? and paymentConfigType != ? order by orderList asc";
		return dao.find(sql,PaymentConfigType.valueOf(PaymentConfigType.deposit.name()).ordinal(),PaymentConfigType.valueOf(PaymentConfigType.offline.name()).ordinal());
	}
	
	/**
	 * 根据总金额计算支付费用
	 * 
	 * @return 支付费用
	 */
	public BigDecimal getPaymentFee(BigDecimal totalAmount) {
		BigDecimal paymentFee = new BigDecimal("0");// 支付费用
		if (getInt("paymentFeeType") == PaymentFeeType.valueOf(PaymentFeeType.scale.name()).ordinal()){
			paymentFee = totalAmount.multiply(new BigDecimal(paymentFee.toString()).divide(new BigDecimal("100")));
		} else {
			paymentFee = getBigDecimal("paymentFee");
		}
		return SystemConfigUtil.getOrderScaleBigDecimal(paymentFee);
	}
	
	/**
	 * 生成担保交易支付请求URL
	 * 
	 * @param paymentConfig
	 *            支付类型
	 *            
	 * @param paymentSn
	 *            支付编号
	 *            
	 * @param totalAmount
	 *            总金额
	 * 
	 * @param description
	 *            交易描述
	 * 
	 * @return 担保交易支付请求URL
	 */
	public String buildTenpayPartnerPaymentUrl(PaymentConfig paymentConfig, String paymentSn, BigDecimal totalAmount, String description) {
		TenpayConfig tenpayConfig = (TenpayConfig) paymentConfig.getConfigObject();
		String totalAmountString = totalAmount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.UP).toString();
		String mchType = null;
		if (tenpayConfig.getTenpayType() != TenpayType.partnerMaterial) {
			mchType = "1";
		} else {
			mchType = "2";
		}
		String data = null;
		/*try {
			if (StringUtils.isNotEmpty(description)) {
				description = URLEncoder.encode(description, "GB2312");
				BASE64Decoder bASE64Decoder = new BASE64Decoder();
				data = new String(bASE64Decoder.decodeBuffer("c2hvcHh4Lm5ldA=="));
			} else {
				description = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		String version = "2";// 版本号
		String cmdno = "12";// 业务代码（12：担保交易支付）
		String encode_type = "2";// 编码方式（1：GB2312编码；2：UTF-8编码）
		String chnid = tenpayConfig.getBargainorId();// 收款方财付通账号
		String seller = tenpayConfig.getBargainorId();// 收款方财付通账号
		String mch_name = "";// 商品名称
		String mch_price = totalAmountString;// 商品价格（单位：分）
		String transport_desc = "";// 物流公司或物流方式描述
		String transport_fee = "0";// 物流费用（单位：分）
		String mch_desc = description;// 交易描述
		String need_buyerinfo = "2";// 是否需要填写物流信息（1：需要；2：不需要）
		String mch_type = mchType;// 交易类型（1、实物交易；2、虚拟交易）
		String mch_vno = paymentSn;// 支付编号
		String mch_returl = SystemConfigUtil.getSystemConfig().getShopUrl() + TenpayConfig.RETURN_URL;// 结果处理URL
		String show_url = SystemConfigUtil.getSystemConfig().getShopUrl() + TenpayConfig.RETURN_URL;// 结果展示URL
		String attach = data;// 商户数据
		String key = tenpayConfig.getKey();// 密钥
		
		Map<String, String> parameterMap = new LinkedHashMap<String, String>();
		parameterMap.put("attach", attach);
		parameterMap.put("chnid", chnid);
		parameterMap.put("cmdno", cmdno);
		parameterMap.put("encode_type", encode_type);
		parameterMap.put("key", key);
		parameterMap.put("mch_desc", mch_desc);
		parameterMap.put("mch_name", mch_name);
		parameterMap.put("mch_price", mch_price);
		parameterMap.put("mch_returl", mch_returl);
		parameterMap.put("mch_type", mch_type);
		parameterMap.put("mch_vno", mch_vno);
		parameterMap.put("need_buyerinfo", need_buyerinfo);
		parameterMap.put("seller", seller);
		parameterMap.put("show_url", show_url);
		parameterMap.put("transport_desc", transport_desc);
		parameterMap.put("transport_fee", transport_fee);
		parameterMap.put("version", version);
		
		// 生成签名
		String sign = DigestUtils.md5Hex(buildParameterString(parameterMap)).toUpperCase();
		
		parameterMap.put("sign", sign);
		parameterMap.remove("key");
		
		// 生成参数字符串
		String parameterString = buildParameterString(parameterMap);
		
		return TenpayConfig.PAYMENT_URL + "?" + parameterString;
	}
	/**
	 * 生成即时交易支付请求URL
	 * 
	 * @param paymentConfig
	 *            支付类型
	 *            
	 * @param paymentSn
	 *            支付编号
	 *            
	 * @param totalAmount
	 *            总金额
	 *    
	 * @param description
	 *            交易描述
	 *            
	 * @param ip
	 *            客户IP
	 * 
	 * @return 即时交易支付请求URL
	 */
	public String buildTenpayDirectPaymentUrl(PaymentConfig paymentConfig, String paymentSn, BigDecimal totalAmount, String description, String ip) {
		TenpayConfig tenpayConfig = (TenpayConfig) paymentConfig.getConfigObject();
		String transactionId = buildTenpayTransactionId(tenpayConfig.getBargainorId(), paymentSn);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		String dateString = simpleDateFormat.format(new Date());
		String totalAmountString = totalAmount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.UP).toString();
		String data = null;
		/*try {
			if (StringUtils.isNotEmpty(description)) {
				description = URLEncoder.encode(description, "GB2312");
				BASE64Decoder bASE64Decoder = new BASE64Decoder();
				data = new String(bASE64Decoder.decodeBuffer("c2hvcHh4Lm5ldA=="));
			} else {
				description = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		String cmdno = "1";// 业务代码（1：即时交易支付）
		String date = dateString;// 支付日期
		String bank_type = "0";// 银行类型（0：财付通）
		String desc = description;// 交易描述
		String purchaser_id = "";// 客户财付通帐户
		String bargainor_id = tenpayConfig.getBargainorId();// 商户号
		String transaction_id = transactionId;// 交易号
		String sp_billno = paymentSn;// 支付编号
		String total_fee = totalAmountString;// 总金额（单位：分）
		String fee_type = "1";// 支付币种（1：人民币）
		String return_url = SystemConfigUtil.getSystemConfig().getShopUrl() + TenpayConfig.RETURN_URL;// 结果处理URL
		String attach = data;// 商户数据
		String spbill_create_ip = ip;// 客户IP
		String key = tenpayConfig.getKey();// 密钥
		
		Map<String, String> parameterMap = new LinkedHashMap<String, String>();
		parameterMap.put("cmdno", cmdno);
		parameterMap.put("date", date);
		parameterMap.put("bargainor_id", bargainor_id);
		parameterMap.put("transaction_id", transaction_id);
		parameterMap.put("sp_billno", sp_billno);
		parameterMap.put("total_fee", total_fee);
		parameterMap.put("fee_type", fee_type);
		parameterMap.put("return_url", return_url);
		parameterMap.put("attach", attach);
		parameterMap.put("spbill_create_ip", spbill_create_ip);
		parameterMap.put("key", key);

		// 生成签名
		String sign = DigestUtils.md5Hex(buildParameterString(parameterMap)).toUpperCase();
		
		parameterMap.put("bank_type", bank_type);
		parameterMap.put("desc", desc);
		parameterMap.put("purchaser_id", purchaser_id);
		parameterMap.put("sign", sign);
		parameterMap.remove("key");
		
		// 生成参数字符串
		String parameterString = buildParameterString(parameterMap);
		
		return TenpayConfig.PAYMENT_URL + "?" + parameterString;
	}
	
	// 根据商户号、支付编号生成财付通交易号
	public String buildTenpayTransactionId (String bargainorId, String paymentSn) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
		String dateString = simpleDateFormat.format(new Date());
		int count = 10 - paymentSn.length();
		if (count > 0) {
			StringBuffer stringBuffer = new StringBuffer();
			for (int i = 0; i < count; i ++) {
				stringBuffer.append("0");
			}
			stringBuffer.append(paymentSn);
			paymentSn = stringBuffer.toString();
		} else {
			paymentSn = StringUtils.substring(paymentSn, count);
		}
		return bargainorId + dateString + paymentSn;
	}
	
	public String buildParameterString(Map<String, String> parameterMap) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String key : parameterMap.keySet()) {
			String value = parameterMap.get(key);
			if (StringUtils.isNotEmpty(value)) {
				stringBuffer.append("&" + key + "=" + value);
			}
		}
		stringBuffer.deleteCharAt(0);
		return stringBuffer.toString();
	}
}
