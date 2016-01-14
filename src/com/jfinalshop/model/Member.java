package com.jfinalshop.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 会员
 * 
 */
public class Member extends Model<Member> {

	private static final long serialVersionUID = 1312507976941245078L;

	public static final String LOGIN_MEMBER_ID_SESSION_NAME = "loginMemberId";// 保存登录会员ID的Session名称
	public static final String LOGIN_MEMBER_USERNAME_COOKIE_NAME = "loginMemberUsername";// 保存登录会员用户名的Cookie名称
	public static final String LOGIN_REDIRECTION_URL_SESSION_NAME = "redirectionUrl";// 保存登录来源URL的Session名称
	public static final String PASSWORD_RECOVER_KEY_SEPARATOR = "_";// 密码找回Key分隔符
	public static final int PASSWORD_RECOVER_KEY_PERIOD = 30;// 密码找回Key有效时间（单位：分钟）
	
	private Map<String, String> memberAttributeMapStore = new HashMap<String, String>(); // 会员注册项储存
	
	public static final Member dao = new Member();

	public boolean containEmail(String email) {
		return dao.findFirst("select email from member where email=? limit 1", email) != null;
	}

	public boolean containUsername(String username) {
		return dao.findFirst("select username from member where username=? limit 1", username) != null;
	}
	
	/**
	 * 根据用户名获取会员对象，若会员不存在，则返回null（不区分大小写）
	 * 
	 */
	public Member getMemberByUsername(String username) {
		return dao.findFirst("select * from member where username = ? ",username);
	}
	
	/**
	 * 根据用户名判断此用户是否存在（不区分大小写）
	 * 
	 */
	public boolean isExistByUsername(String username) {
		String sql = "select * from Member where lower(username) = lower(?)";
		Member member = dao.findFirst(sql,username);
		if (member != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 根据用户名、密码验证会员
	 * 
	 * @param username
	 *            用户名
	 *            
	 * @param password
	 *            密码
	 * 
	 * @return 验证是否通过
	 */
	public boolean verifyMember(String username, String password) {
		Member member = getMemberByUsername(username);
		if (member != null && member.getStr("password").equals(DigestUtils.md5Hex(password))) {
			return true;
		} else {
			return false;
		}
	}
	
	// 获取会员注册项
	public Map<String, String> getMemberAttributeMap() {
		String sql = "select * from member_memberattributemapstore where member_id = ?";
		List<MemberAttributeMapStore> memberAttributeList = MemberAttributeMapStore.dao.find(sql,getStr("id"));
		
		if(memberAttributeList != null && memberAttributeList.size() > 0){
			for(MemberAttributeMapStore memberAttribute : memberAttributeList){
				memberAttributeMapStore.put(memberAttribute.getStr("mapkey_id"), memberAttribute.getStr("element"));
			}
		}
		return memberAttributeMapStore;
	}
	
	// 设置会员注册项
	public void setMemberAttributeMap(Map<String, String> memberAttributeMap) {
		if (memberAttributeMap == null || memberAttributeMap.size() == 0) {
			memberAttributeMapStore = null;
			return;
		}		
		// 先删除已存的
		Db.deleteById("member_memberattributemapstore", "member_id", getStr("id"));
		// 再保存
		MemberAttributeMapStore memberAttribute = new MemberAttributeMapStore();
		for (Entry<String, String> entry: memberAttributeMap.entrySet()) {
			memberAttribute.set("member_id", getStr("id"));
			memberAttribute.set("mapkey_id", entry.getKey());
			memberAttribute.set("element", entry.getValue());
			memberAttribute.save();
		}
	}
		
	
	/**
	 * 生成密码找回Key
	 * 
	 * @return 密码找回Key
	 */
	public String buildPasswordRecoverKey() {
		return System.currentTimeMillis() + Member.PASSWORD_RECOVER_KEY_SEPARATOR + CommonUtil.getUUID() + DigestUtils.md5Hex(CommonUtil.getRandomString(10));
	}
	
	/**
	 * 根据密码找回Key获取生成日期
	 * 
	 * @return 生成日期
	 */
	public Date getPasswordRecoverKeyBuildDate(String passwordRecoverKey) {
		long time = Long.valueOf(StringUtils.substringBefore(passwordRecoverKey, Member.PASSWORD_RECOVER_KEY_SEPARATOR));
		return new Date(time);
	}
	
	/**
	 * 获取所有实体对象总数.
	 * 
	 * @return 实体对象总数
	 */
	public Long getTotalCount() {
		String sql = "select count(*) from member ";
		return Db.queryLong(sql);
	}
	
	// 等级
	public MemberRank getMemberRank(){
		String sql = "select * from memberrank where id = ? order by createDate desc";
		return MemberRank.dao.findFirst(sql,getStr("memberRank_id"));
	}
	
	// 订单项
	public List<Orders> getOrderList(){
		String sql = "select * from orders where member_id = ? order by createDate desc";
		return Orders.dao.find(sql,getStr("id"));
	}
	
	// 购物车项
	public List<CartItem> getCartItemList() {
		String sql = "select * from cartitem where member_id = ? order by createDate desc";
		return CartItem.dao.find(sql,getStr("id"));
	}
	
	// 收货地址
	public List<Receiver> getReceiverList() {
		return Receiver.dao.getReceiverList(this);
	}
	
	// 预存款充值记录
	public List<Deposit> getDepositList(){
		String sql = "select * from deposit where member_id = ? order by createDate desc ";
		return Deposit.dao.find(sql,getStr("id"));
	}
	
	// 预存款
	public Deposit getDeposit(){
		String sql = "select * from deposit where member_id = ? order by createDate desc ";
		return Deposit.dao.findFirst(sql,getStr("id"));
	}
	
	// 收藏夹商品
	public List<Product> getFavoriteProductList() {
		String sql = "SELECT p.* FROM member_product m LEFT JOIN product p ON m.favoriteProductSet_id = p.id WHERE m.favoriteMemberSet_id = ?";
		return Product.dao.find(sql,getStr("id"));
	}
	
	// 收件箱消息
	public List<Message> getInboxMessageList() {
		String sql = "select * from message t where t.toMember_id = ? ";
		return Message.dao.find(sql,getStr("id"));
	}
	
	// 发件箱消息
	public List<Message> getOutboxMessageList() {
		String sql = "select * from message t where t.fromMember_id = ? ";
		return Message.dao.find(sql,getStr("id"));
	}
}
