package com.jfinalshop.controller.shop;

import cn.dreampie.captcha.CaptchaRender;

import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.ext.route.ControllerBind;


/**
 * 前台类 - 首页
 * 
 */
@ControllerBind(controllerKey = "/")
public class IndexController extends Controller{
	
	// 首页
	public void index(){
		redirect("/index.html");
	}
	
	/**
	 * 验证码
	 */
	@Clear
	public void captcha() {
		int width = 0, height = 0, minnum = 0, maxnum = 0, fontsize = 0;
		CaptchaRender captcha = new CaptchaRender();
		if (isParaExists("width")) {
			width = getParaToInt("width");
		}
		if (isParaExists("height")) {
			height = getParaToInt("height");
		}
		if (width > 0 && height > 0)
			captcha.setImgSize(width, height);
		if (isParaExists("minnum")) {
			minnum = getParaToInt("minnum");
		}
		if (isParaExists("maxnum")) {
			maxnum = getParaToInt("maxnum");
		}
		if (minnum > 0 && maxnum > 0)
			captcha.setFontNum(minnum, maxnum);
		if (isParaExists("fontsize")) {
			fontsize = getParaToInt("fontsize");
		}
		if (fontsize > 0)
			captcha.setFontSize(fontsize, fontsize);
		// 干扰线数量 默认0
		captcha.setLineNum(1);
		// 噪点数量 默认50
		captcha.setArtifactNum(10);
		// 使用字符 去掉0和o 避免难以确认
		captcha.setCode("123456789");
		 //验证码在session里的名字 默认 captcha,创建时间为：名字_time
		// captcha.setCaptchaName("captcha");
	    //验证码颜色 默认黑色
		// captcha.setDrawColor(new Color(255,0,0));
	    //背景干扰物颜色  默认灰
		// captcha.setDrawBgColor(new Color(0,0,0));
	    //背景色+透明度 前三位数字是rgb色，第四个数字是透明度  默认透明
		// captcha.setBgColor(new Color(225, 225, 0, 100));
	    //滤镜特效 默认随机特效 //曲面Curves //大理石纹Marble //弯折Double //颤动Wobble //扩散Diffuse
		captcha.setFilter(CaptchaRender.FilterFactory.Curves);
		// 随机色 默认黑验证码 灰背景元素
		captcha.setRandomColor(true);
		render(captcha);
	}
}
