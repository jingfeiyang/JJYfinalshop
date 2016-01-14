/***
 *	lSelect(Link Select) 无限级联动下拉菜单插件
 *
 *  JSON数据格式示例：[{"title": "北京", "value": "beijing"},{"title": "湖南", "value": "hunan"},{"title": "湖北", "value": "hubei"}]
 *
 *	http://www.shopxx.net
 *
 *	Copyright (c) 2008 SHOP++. All rights reserved. 
 **/

(function($){
$.extend($.fn, {
	lSelect: function(options){
	
		// 默认参数
		var settings = {
			url: "",// 数据获取url
			parameter: "path",// 数据获取参数名称
			title: "title",// 定义JSON数据格式：选择名称
			value: "value",// 定义JSON数据格式：选择值
			emptyOption: "请选择",// 选择提示,null表示无提示
			cssClass: "lSelect",// 下拉框css名称
			cssStyle: {"margin-right": "10px"},// 下拉框左右css样式
			isFadeIn: true// 是否渐显
		};
		
		$.extend(settings, options);
		
		return this.each(function(){
			
			var $this = $(this);
			$this.hide();
			var selectGroupClass = "lSelect" + Math.round(Math.random() * 1000000);
			var items = {};
			var selectName = $this.attr("name");
			var defaultSelectedValue = $this.val();
			
			if (defaultSelectedValue == "") {
				addSelect($this);
			} else {
				var $select = $this;
				var defaultSelectedValueArray = defaultSelectedValue.split(",");
				for (var i = 0; i < defaultSelectedValueArray.length; i++) {
					var $nextSelect = addSelect($select, defaultSelectedValueArray[i]);
					if($nextSelect) {
						$select = $nextSelect;
					}
				}
			}
			
			// 绑定Select元素
			function bindSelect(element) {
				element.bind("change", function(){
					addSelect(element);
					$this.val(element.val());
				});
			}
			
			// 获取Json数据
			function getJson(key) {
				if(typeof(items[key]) == "undefined") {
					var url = settings.url;
					if (key != "lSelectRoot") {
						var parameter = settings.parameter;
						if (parameter != null) {
							if(url.indexOf("?") > 0) {
								url = url + "&" + parameter + "=" + key;
							} else {
								url = url + "?" + parameter + "=" + key;
							}
						}
					}
					$.ajaxSetup({async: false});
					$.getJSON(url, function(json) {
						items[key] = json;
					});
				}
				return items[key];
			}
			
			// 填充option
			function fill(element, key, selected) {
				var json = getJson(key);
				if (!json) {
					return false;
				}
				var length = 0;
				for (j in json){
					length ++;
				}
				if (length == 0) {
					return false;
				} else {
					element.empty();
					if(settings.emptyOption != null) {
						element.append('<option value="">' + settings.emptyOption + '</option>');
					}
					$.each(json, function(id, object) {
						var optionValue = "";
						if (object.value.indexOf(",") >= 0) {
							var optionValueArray = object.value.split(",");
							optionValue =  optionValueArray[optionValueArray.length - 1];
						} else {
							optionValue = object.value;
						}
						
						var option;
						if(selected && optionValue == selected) {
							option	= $('<option value="' + object.value + '" selected>' + object.title + '</option>');
						} else {
							option	= $('<option value="' + object.value + '">' + object.title + '</option>');
						}
						element.append(option);
					});
					return true;
				}
			}
			
			// 增加select
			function addSelect(element, selected) {
				var $nextSelect;
				var isFill;
				if(element.is("select")) {
					element.nextAll("." + selectGroupClass).remove();
					if(element.val() == "") {
						return;
					}
					element.after('<select class="' + settings.cssClass + ' ' + selectGroupClass + '" style="display: none;"></select>');
					$nextSelect = element.next("." + selectGroupClass);
					isFill = fill($nextSelect, element.val(), selected);
				} else {
					element.after('<select class="' + settings.cssClass + ' ' + selectGroupClass + '" style="display: none;"></select>');
					$nextSelect = element.next("." + selectGroupClass);
					isFill = fill($nextSelect, "lSelectRoot", selected);
				}
				if (isFill) {
					element.css(settings.cssStyle);
					if(settings.isFadeIn) {
						$nextSelect.fadeIn();
						bindSelect($nextSelect);
					} else {
						$nextSelect.show();
					}
					return $nextSelect;
				} else {
					$nextSelect.remove();
				}
			}
		});

	}
});
})(jQuery);