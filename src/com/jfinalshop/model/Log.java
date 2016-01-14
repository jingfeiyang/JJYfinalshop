package com.jfinalshop.model;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 日志
 * 
 */

public class Log extends Model<Log>{

	private static final long serialVersionUID = 7930104303560372711L;
	
	public static final Log dao = new Log();

	public static final String ID = "id";// ID
	public static final String CREATE_DATE = "createDate";// 创建日期
	public static final String OPERATION_NAME = "operationName";// 操作名称
	public static final String OPERATOR = "operator";// 操作员
	public static final String ACTION_CLASS_NAME = "actionClassName";// 操作Action名称
	public static final String ACTION_METHOD_NAME = "actionMethodName";// 操作方法名称
	public static final String IP = "ip";// IP
	public static final String INFO = "info";// 日志信息
	
	public boolean save(Log log){
		set(ID, CommonUtil.getUUID());
		set(CREATE_DATE, new Date());
		return save();
	}
	
	public void setCreateDate(Date createDate) {
		set(CREATE_DATE, createDate);
	}	
	public Date getCreateDate() {
		return getDate(CREATE_DATE);
	}
	
	public void setOperationName(String operationName) {
		set(OPERATION_NAME, operationName);
	}	
	public String getOperationName() {
		return getStr(OPERATION_NAME);
	}
	
	public void setOperator(String operator) {
		set(OPERATOR, operator);
	}	
	public String getOperator() {
		return getStr(OPERATOR);
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
	
	public void setIp(String ip) {
		set(IP, ip);
	}	
	public String getIp() {
		return getStr(IP);
	}
	
	public void setInfo(String info) {
		set(INFO, info);
	}	
	public String getInfo() {
		return getStr(INFO);
	}
}
