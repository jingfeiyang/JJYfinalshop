package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 日志配置
 * 
 */
public class LogConfig extends Model<LogConfig> {
	private static final long serialVersionUID = -5243567936734506683L;
	public static final LogConfig dao = new LogConfig();

	public static final String OPERATION_NAME = "operationName";// 操作名称
	public static final String ACTION_CLASS_NAME = "actionClassName";// 需要进行日志记录的Action名称
	public static final String ACTION_METHOD_NAME = "actionMethodName";// 需要进行日志记录的方法名称
	public static final String DESCRIPTION = "description";// 描述

	/**
	 * 根据Action类名称获取LogConfig对象集合.
	 * 
	 * @param actionClassName
	 *            Action类名称
	 * @return LogConfig对象集合
	 */
	public List<LogConfig> getLogConfigList(String actionClassName) {
		String sql = "select * from LogConfig  where actionClassName = ?";
		return dao.find(sql,actionClassName);
	}
	
	public List<LogConfig> getAll(){
		String sql = "select * from LogConfig";
		return dao.find(sql);
	}
	
	public void setOperationName(String operationName) {
		set(OPERATION_NAME, operationName);
	}
	
	public String getOperationName() {
		return getStr(OPERATION_NAME);
	}

	public void setActionClassName(String actionClassName) {
		set(ACTION_CLASS_NAME, actionClassName);
	}
	
	public String getActionClassName() {
		return getStr(ACTION_CLASS_NAME);
	}
	
	public void setActionMethodName(String actionMethodName) {
		set(ACTION_METHOD_NAME, actionMethodName);
	}
	
	public String getActionMethodName() {
		return getStr(ACTION_METHOD_NAME);
	}
	
	public void setDescription(String description) {
		set(DESCRIPTION, description);
	}
	
	public String getDescription() {
		return getStr(DESCRIPTION);
	}
	
	
}
