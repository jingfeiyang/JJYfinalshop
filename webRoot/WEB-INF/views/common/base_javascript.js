/***
 *	JFINALSHOP Base JavaScript
 *
 *	http://www.JFinalshop.com
 *
 *	Copyright (c) 2014 JFINALSHOP. All rights reserved. 
 **/
 
var JFinalshop = {
	base: "${base}",
	currencySign: "${systemConfig.currencySign}",// 货币符号
	currencyUnit: "${systemConfig.currencyUnit}",// 货币单位
	priceScale: "${systemConfig.priceScale}",// 商品价格精确位数
	priceRoundType: "${systemConfig.priceRoundType}",// 商品价格精确方式
	orderScale: "${systemConfig.orderScale}",// 订单金额精确位数
	orderRoundType: "${systemConfig.orderRoundType}"// 订单金额精确方式
};

if(!window.XMLHttpRequest) {

	// 解决IE6透明PNG图片BUG
	DD_belatedPNG.fix(".png");
	
	// 解决IE6不缓存背景图片问题
	document.execCommand("BackgroundImageCache", false, true);

}

// 添加收藏夹
function addFavorite(url, title) {
	if (document.all) {
		window.external.addFavorite(url, title);
	} else if (window.sidebar) {
		window.sidebar.addPanel(title, url, "");
	}
}

// 浮点数加法运算
function floatAdd(arg1, arg2) {
	var r1, r2, m;
	try{
		r1 = arg1.toString().split(".")[1].length;
	} catch(e) {
		r1 = 0;
	}
	try {
		r2 = arg2.toString().split(".")[1].length;
	} catch(e) {
		r2 = 0;
	}
	m = Math.pow(10, Math.max(r1, r2));
	return (arg1 * m + arg2 * m) / m;
}

// 浮点数减法运算
function floatSub(arg1, arg2) {
	var r1, r2, m, n;
	try {
		r1 = arg1.toString().split(".")[1].length;
	} catch(e) {
		r1 = 0
	}
	try {
		r2 = arg2.toString().split(".")[1].length;
	} catch(e) {
		r2 = 0
	}
	m = Math.pow(10, Math.max(r1, r2));
	n = (r1 >= r2) ? r1 : r2;
	return ((arg1 * m - arg2 * m) / m).toFixed(n);
}

// 浮点数乘法运算
function floatMul(arg1, arg2) {    
	var m = 0, s1 = arg1.toString(), s2 = arg2.toString();
	try {
		m += s1.split(".")[1].length;
	} catch(e) {}
	try {
		m += s2.split(".")[1].length;
	} catch(e) {}
	return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
}

// 浮点数除法运算
function floatDiv(arg1, arg2) {
	var t1 = 0, t2 = 0, r1, r2;    
	try {
		t1 = arg1.toString().split(".")[1].length;
	} catch(e) {}
	try {
		t2 = arg2.toString().split(".")[1].length;
	} catch(e) {}
	with(Math) {
		r1 = Number(arg1.toString().replace(".", ""));
		r2 = Number(arg2.toString().replace(".", ""));
		return (r1 / r2) * pow(10, t2 - t1);
	}
}

// 设置数值精度
function setScale(value, scale, roundingMode) {
	if (roundingMode.toLowerCase() == "roundhalfup") {
		return (Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale)).toFixed(scale);
	} else if (roundingMode.toLowerCase() == "roundup") {
		return (Math.ceil(value * Math.pow(10, scale)) / Math.pow(10, scale)).toFixed(scale);
	} else {
		return (Math.floor(value * Math.pow(10, scale)) / Math.pow(10, scale)).toFixed(scale);
	}
}

// 格式化商品价格货币
function priceCurrencyFormat(price) {
	price = setScale(price, JFinalshop.priceScale, JFinalshop.priceRoundType);
	return JFinalshop.currencySign + price;
}

// 格式化商品价格货币（包含货币单位）
function priceUnitCurrencyFormat(price) {
	price = setScale(price, JFinalshop.priceScale, JFinalshop.priceRoundType);
	return JFinalshop.currencySign + price + JFinalshop.currencyUnit;
}

// 格式化订单金额货币
function orderCurrencyFormat(price) {
	price = setScale(price, JFinalshop.orderScale, JFinalshop.orderRoundType);
	return JFinalshop.currencySign + price;
}

// 格式化订单金额货币（包含货币单位）
function orderUnitCurrencyFormat(price) {
	price = setScale(price, JFinalshop.orderScale, JFinalshop.orderRoundType);
	return JFinalshop.currencySign + price + JFinalshop.currencyUnit;
}

$().ready( function() {

	// Tab效果
	$("ul.tab").tabs(".tabContent", {
		tabs: "input"
	});
	
	// 所见即所得编辑器
	$("textarea.wysiwyg").tinymce({
		script_url: JFinalshop.base + "/template/common/tiny_mce/tiny_mce.js",
		language: "zh",
		theme: "advanced",
		plugins: "table,advimage,inlinepopups,preview,media,contextmenu,paste,fullscreen",
		theme_advanced_buttons1: "code,fullscreen,preview,|,bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,bullist,numlist,styleselect,formatselect,fontselect,fontsizeselect",
		theme_advanced_buttons2: "undo,redo,|,link,unlink,anchor,|,sub,sup,|,forecolor,backcolor,image,media,tablecontrols",
		theme_advanced_buttons3: "",
		theme_advanced_toolbar_location: "top",
		theme_advanced_toolbar_align: "left",
		content_css: "css/content.css",
		template_external_list_url: "lists/template_list.js",
		external_link_list_url: "lists/link_list.js",
		external_image_list_url: "lists/image_list.js",
		media_external_list_url: "lists/media_list.js",
		relative_urls: false,
		remove_script_host: false
	});

	// 内容窗口
	$("body").prepend('<div id="contentWindow" class="contentWindow"><div class="windowTop"><div class="windowTitle"></div><a class="messageClose windowClose" href="#" hidefocus="true"></a></div><div class="windowMiddle"><div class="windowContent"></div></div><div class="windowBottom"></div></div>');

	// 消息提示窗口
	$("body").prepend('<div id="messageWindow" class="messageWindow"><div class="windowTop"><div class="windowTitle">提示信息&nbsp;</div><a class="messageClose windowClose" href="#" hidefocus="true"></a></div><div class="windowMiddle"><div class="messageContent"><span class="icon">&nbsp;</span><span class="messageText"></span></div><input type="button" class="formButton messageButton windowClose" value="确  定" hidefocus="true"/></div><div class="windowBottom"></div></div>');
	
	// 滑动提示框
	$("body").prepend('<div id="tipWindow" class="tipWindow"><span class="icon">&nbsp;</span><span class="messageText"></span></div>');
	
	// 内容窗口
	$("#contentWindow").jqm({
		overlay: 60,
		closeClass: "windowClose",
		modal: true,
		trigger: false,
		onHide: function(object) {
			object.o.remove();
			object.w.fadeOut();
		}
	}).jqDrag(".windowTop");
	
	// 消息提示窗口
	$("#messageWindow").jqm({
		overlay: 60,
		closeClass: "windowClose",
		modal: true,
		trigger: false,
		onHide: function(object) {
			object.o.remove();
			object.w.fadeOut();
		}
	}).jqDrag(".windowTop");
	
	// 内容窗口
	$.window = function () {
		var $contentWindow = $("#contentWindow");
		var $windowTitle = $("#contentWindow .windowTitle");
		var $windowContent = $("#contentWindow .windowContent");
		var windowTitle;
		var windowContent;
		if (arguments.length == 1) {
			windowTitle = "";
			windowContent = arguments[0];
		} else {
			windowTitle = arguments[0];
			windowContent = arguments[1];
		}
		$windowTitle.html(windowTitle);
		$windowContent.html(windowContent);
		$contentWindow.jqmShow();
	}
	
	// 关闭内容窗口
	$.closeWindow = function () {
		var $contentWindow = $("#contentWindow");
		$contentWindow.jqmHide();
	}
	
	// 警告信息
	$.message = function () {
		var $messageWindow = $("#messageWindow");
		var $icon = $("#messageWindow .icon");
		var $messageText = $("#messageWindow .messageText");
		var $messageButton = $("#messageWindow .messageButton");
		var messageType;
		var messageText;
		if (arguments.length == 1) {
			messageType = "warn";
			messageText = arguments[0];
		} else {
			messageType = arguments[0];
			messageText = arguments[1];
		}
		if (messageType == "success") {
			$icon.removeClass("warn").removeClass("error").addClass("success");
		} else if (messageType == "error") {
			$icon.removeClass("warn").removeClass("success").addClass("error");
		} else {
			$icon.removeClass("success").removeClass("error").addClass("warn");
		}
		$messageText.html(messageText);
		$messageWindow.jqmShow();
		$messageButton.focus();
	}
	
	// 滑动提示框
	$.tip = function () {
		var $tipWindow = $("#tipWindow");
		var $icon = $("#tipWindow .icon");
		var $messageText = $("#tipWindow .messageText");
		var messageType;
		var messageText;
		if (arguments.length == 1) {
			messageType = "warn";
			messageText = arguments[0];
		} else {
			messageType = arguments[0];
			messageText = arguments[1];
		}
		if (messageType == "success") {
			$icon.removeClass("warn").removeClass("error").addClass("success");
		} else if (messageType == "error") {
			$icon.removeClass("warn").removeClass("success").addClass("error");
		} else {
			$icon.removeClass("success").removeClass("error").addClass("warn");
		}
		$messageText.html(messageText);
		$tipWindow.css({"margin-left": "-" + parseInt($tipWindow.width() / 2) + "px", "left": "50%"});
		setTimeout(function() {
			$tipWindow.animate({left: 0, opacity: "hide"}, "slow");
		}, 1000);
		$tipWindow.show();
	}
	
	// 日期选择框
	var $currentDatePicker;
	var datePickerOptions = {
        format: "Y-m-d",
		date: new Date(),
		calendars: 1,
		starts: 1,
		position: "right",
		prev: "<<",
		next: ">>",
		locale: {
			days: ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"],
			daysShort: ["周日", "周一", "周二", "周三", "周四", "周五", "周六", "周日"],
			daysMin: ["日", "一", "二", "三", "四", "五", "六", "日"],
			months: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
			monthsShort: ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
			weekMin: ' '
		},
		onBeforeShow: function(){
			$currentDatePicker = $(this);
			var currentDate = $.trim($currentDatePicker.val());
			if (currentDate != "") {
				var reg = /^(\d{1,4})(-|\/)(\d{1,2})\2(\d{1,2})$/;
				if(currentDate.match(reg) != null) {
					$currentDatePicker.DatePickerSetDate($currentDatePicker.val(), true);
				}
			}
		},
		onChange: function(formated, dates){
			$currentDatePicker.val(formated);
		}
    };
    $("input.datePicker").DatePicker(datePickerOptions);
	
	// 重新绑定日期选择框
	$.bindDatePicker = function () {
		$("input.datePicker").DatePicker(datePickerOptions);
	}
	
	// 表单验证
	$("form.validate").validate({
		errorClass: "validateError",
		ignore: ".ignoreValidate",
		errorPlacement: function(error, element) {
			var messagePosition = element.metadata().messagePosition;
			if("undefined" != typeof messagePosition && messagePosition != "") {
				var $messagePosition = $(messagePosition);
				if ($messagePosition.size() > 0) {
					error.insertAfter($messagePosition).fadeOut(300).fadeIn(300);
				} else {
					error.insertAfter(element).fadeOut(300).fadeIn(300);
				}
			} else {
				error.insertAfter(element).fadeOut(300).fadeIn(300);
			}
		},
		submitHandler: function(form) {
			$(form).find(":submit").attr("disabled", true);
			form.submit();
		}
	});
	
	// 提示效果
	$("input[title], label[title]").qtip({
		content: {
			text: true
		},
		style: {
			name: "cream",
			width: {
				max: 500
			}
		}
	});
	
	// 图片预览
	$("a.imagePreview").each(function() {
		$this = $(this);
		$this.qtip({
			content: '<img src="' + $this.attr("href") + '?timestamp=' + (new Date()).valueOf() + '" />',
			style: {
				width: "auto",
				padding: "1px"
			}
		});
	})

});