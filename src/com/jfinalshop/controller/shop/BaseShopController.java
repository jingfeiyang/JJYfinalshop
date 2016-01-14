package com.jfinalshop.controller.shop;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.interceptor.MemberInterceptor;
import com.jfinalshop.model.Member;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 前台类 - 前台基类
 *
 */

@Before(MemberInterceptor.class)
public class BaseShopController<M extends Model<M>> extends Controller {

	public static final String STATUS = "status";
	public static final String WARN = "warn";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String MESSAGE = "message";
	public static final String CONTENT = "content";
	
	// 排序方式
	public enum OrderType{
		asc, desc
	}
	private String property;// 查找属性名称
	private String keyword;// 查找关键字
	
	private String orderBy;// 排序字段
	private String orderType;// 排序方式
		
	
	protected String id;
	protected String[] ids;
	protected String logInfo;// 日志记录信息
	protected String redirectionUrl = "redirectionUrl";// 操作提示后的跳转URL,为null则返回前一页
	
	protected List<String> errorMessageList;
	
	public BaseShopController() {
		// 把class的变量保存起来，不用每次去取
		this.setModelClass(getClazz());
	}

	/**
	 * 获取M的class
	 * 
	 * @return M
	 */
	@SuppressWarnings("unchecked")
	public Class<M> getClazz() {
		Type t = getClass().getGenericSuperclass();
		Type[] params = ((ParameterizedType) t).getActualTypeArguments();
		return (Class<M>) params[0];
	}

	protected Class<M> modelClass;

	public Class<M> getModelClass() {
		return modelClass;
	}

	public void setModelClass(Class<M> modelClass) {
		this.modelClass = modelClass;
	}

	/**
	 * 通用分页查找
	 */
	public void findByPage() {
		String select = "select *";
		String sqlExceptSelect = "from " + getModelClass().getSimpleName() + " where 1 = 1 ";

		if (StrKit.notBlank(getProperty()) && StrKit.notBlank(getKeyword())) {
			sqlExceptSelect += "and " + getProperty() + " like '%" + getKeyword() + "%'";
		}
		
		sqlExceptSelect += " order by " + getOrderBy() +" "+ getOrderType();
				
		Page<M> pager = getModel(getModelClass()).paginate(getParaToInt("pageNumber", 1), getParaToInt("pageSize", 20), select, sqlExceptSelect);
		setAttr("pager", pager);
	}

	/**
	 * 通用查找全部
	 */
	public void getAll() {
		renderJson(Db.find("select * from " + getModelClass().getSimpleName() + " order by id asc;"));
	}

	/**
	 * 通用根据id查找
	 */
	public void getById() {
		renderJson(Db.findById(getModelClass().getSimpleName(), getParaToInt("id")));
	}

	/**
	 * 通用新增
	 * 
	 */
	public void saved(Model<M> model){
		model.set("id", CommonUtil.getUUID());
		model.set("createDate", new Date());
		model.save();
	}

	/**
	 * 通用修改
	 * 
	 */
	public void updated(Model<M> model){
		model.set("modifyDate", new Date());
		model.update();
	}

	/**
	 * 通用删除
	 * 
	 * @throws Exception
	 */
	public void delete() throws Exception {
		renderText(getModel(getModelClass()).delete() + "");
	}
	
	// 获取系统配置信息
	public SystemConfig getSystemConfig() {
		return SystemConfigUtil.getSystemConfig();
	}
	
	// 获取商品价格货币格式
		public String getPriceCurrencyFormat() {
		return SystemConfigUtil.getPriceCurrencyFormat();
	}
	
	// 获取商品价格货币格式（包含货币单位）
	public String getPriceUnitCurrencyFormat() {
		return SystemConfigUtil.getPriceUnitCurrencyFormat();
	}
	
	// 获取订单货币格式
	public String getOrderCurrencyFormat() {
		return SystemConfigUtil.getOrderCurrencyFormat();
	}
	
	// 获取订单货币格式（包含货币单位）
	public String getOrderUnitCurrencyFormat() {
		return SystemConfigUtil.getOrderUnitCurrencyFormat();
	}
	
	public void renderSuccessMessage(String message,String url){
		setAttr(MESSAGE, message);
		setAttr(redirectionUrl, url);
		render("/shop/success.html");
	}
	
	
	// 输出JSON成功消息，返回null
	public void ajaxJsonSuccessMessage(String message) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put(MESSAGE, message);
		renderJson(jsonMap);
	}

	// 输出JSON错误消息，返回null
	public void ajaxJsonErrorMessage(String message) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, ERROR);
		jsonMap.put(MESSAGE, message);
		renderJson(jsonMap);
	}
	
	// 输出JSON警告消息，返回null
	public void ajaxJsonWarnMessage(String message) {
		setAttr(STATUS, WARN);
		setAttr(MESSAGE, message);
		renderJson();
	}
	
	public void addActionError(String error){
		setAttr("errorMessages", error);
		render("/shop/error.html");	
	}
	
	/**
	 * 根据表名、属性名称、属性值判断在数据库中是否唯一(若新修改的值与原来值相等则直接返回true).
	 * 
	 * @param tableName
	 *            表名
	 * @param propertyName
	 *            属性名称
	 * @param value
	 *            属性值
	 * @return boolean
	 */
	public boolean isUnique(String tableName,String propertyName, String value) {		
		if (StrKit.notBlank(tableName) && StrKit.notBlank(propertyName) && StrKit.notBlank(value)) {
			String sql = "select * from " + tableName + " where " + propertyName + " = ? ";
			return Db.findFirst(sql,value) == null;
		}else{
			return false;
		}		
	}
	
	// 获取当前登录会员，若未登录则返回null
	public Member getLoginMember() {
		String id = getSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME);
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		Member loginMember = Member.dao.findById(id);
		return loginMember;
	}

	public String getProperty() {
		property = getPara("property","");
		return property;
	}

	public String getKeyword() {
		keyword = getPara("keyword","");
		return keyword;
	}

	public String getOrderBy() {
		orderBy = getPara("orderBy","createDate");
		return orderBy;
	}

	public String getOrderType() {
		orderType = getPara("orderType",OrderType.desc.name());
		return orderType;
	}
	
}
