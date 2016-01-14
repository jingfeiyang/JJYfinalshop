package com.jfinalshop.model;

import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 管理员
 * 
 */
public class Admin extends Model<Admin>{

	private static final long serialVersionUID = -57555379613217315L;
	
	public static final String HASH_ALGORITHM = "SHA-1";
	public static final int HASH_INTERATIONS = 1024;
	
	public static final Admin dao = new Admin();
	
	public List<Role> getRoleList() {
		String sql ="" 
			 +" select r.*"
			 +"  from admin_role a left outer join role r on a.roleset_id = r.id " 
			 +" where a.adminset_id = ?"; 
		return Role.dao.find(sql,getStr("id"));
	}
	
	/**
	 * 根据用户名获取管理员对象，若管理员不存在，则返回null（不区分大小写）
	 * 
	 */
	public Admin getAdminByUsername(String username) {
		return dao.findFirst("select * from admin where username = ?",username);
	}
	
	/**
	 * 根据用户名、密码验证管理员
	 * 
	 * @param username
	 *            用户名
	 *            
	 * @param password
	 *            密码
	 * 
	 * @return 验证是否通过
	 */
	public boolean verifyAdmin(String username, String password) {
		Admin member = getAdminByUsername(username);
		if (member != null && member.getStr("password").equals(DigestUtils.md5Hex(password))) {
			return true;
		} else {
			return false;
		}
	}
	
		
	/**
	 * 根据用户ID查询该用户所拥有的角色列表
	 * @param userId
	 * @return
	 */
	public List<String> getRolesName(String adminId) {
		String sql =  "" 
				 +"select r.value"
				 +"  from admin a" 
				 +"  left outer join admin_role ar" 
				 +"    on a.id = ar.adminset_id" 
				 +"  left outer join role r" 
				 +"    on r.id = ar.roleset_id" 
				 +" where a.id = ?";

		return Db.query(sql, adminId);
	}
	
	/**
	 * 根据用户ID查询该用户所拥有的权限列表
	 * @param userId
	 * @return
	 */
	public List<String> getAuthoritiesName(String adminId) {
		String sql = "" 
				 + "select a.value"
				 +"  from admin u" 
				 +"  left outer join admin_role ru" 
				 +"    on u.id = ru.adminset_id" 
				 +"  left outer join role r" 
				 +"    on ru.roleset_id = r.id" 
				 +"  left outer join role_resource ra" 
				 +"    on r.id = ra.roleset_id" 
				 +"  left outer join resource a" 
				 +"    on ra.resourceset_id = a.id" 
				 +" where u.id = ?";		
		return Db.query(sql, adminId);
	}
}
