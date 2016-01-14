package com.jfinalshop.cfg;

import org.beetl.ext.jfinal.BeetlRenderFactory;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.plugin.shiro.ShiroPlugin;
import com.jfinal.ext.plugin.tablebind.AutoTableBindPlugin;
import com.jfinal.ext.plugin.tablebind.SimpleNameStyles;
import com.jfinal.ext.route.AutoBindRoutes;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinalshop.util.SerialNumberUtil;

public class JFWebConfig extends JFinalConfig{

	/**
     * 供Shiro插件使用。
     */
	Routes routes;    
	
	@Override
	public void configConstant(Constants me) {
		//SqlReporter.setLogger(true); 
		me.setErrorView(401, "401.html");
		me.setErrorView(403, "403.html");
		me.setError404View("404.html");
		me.setError500View("500.html");
		
		// 加载数据库配置文件
		loadPropertyFile("jdbc.properties");
		// 设定Beetl
		me.setMainRenderFactory(new BeetlRenderFactory());
		// 设定为开发者模式
		me.setDevMode(true);
	}

	@Override
	public void configRoute(Routes me) {
		this.routes = me;
		me.add(new AutoBindRoutes());		
	}

	@Override
	public void configPlugin(Plugins me) {
		// mysql
		String url = getProperty("jdbcUrl");
		String username = getProperty("user");
		String password = getProperty("password");
		String driverClass = getProperty("driverClass");
		String filters = getProperty("filters");
		
		// mysql 数据源
		DruidPlugin dsMysql = new DruidPlugin(url, username, password,driverClass, filters);
		dsMysql.setMaxActive(200);
		me.add(dsMysql);
		
		ActiveRecordPlugin arpMysql = new ActiveRecordPlugin("mysql",dsMysql);
		me.add(arpMysql);
		
		AutoTableBindPlugin atbp = new AutoTableBindPlugin(dsMysql, SimpleNameStyles.LOWER);
		atbp.setShowSql(true);
		atbp.setDialect(new MysqlDialect());// 配置MySql方言
		me.add(atbp);
		
		//加载Shiro插件
		me.add(new ShiroPlugin(routes));
	}

	@Override
	public void configInterceptor(Interceptors me) {}

	@Override
	public void configHandler(Handlers me) {}

	public void afterJFinalStart(){		
		SerialNumberUtil.lastSnNumberInit();	
	}
	
}
