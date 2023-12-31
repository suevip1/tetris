define([
    'text!' + window.APPPATH + 'monitor/external-folder/page-monitor-external-folder.html',
    'restfull',
    'config',
    'commons',
    'vue',
    'element-ui',
    'bvc2-header',
    'bvc2-system-nav-side',
	'extral',
	'bvc2-monitor-external-folder'
], function(tpl, ajax, config, commons, Vue){

    var pageId = 'page-monitor-external-folder';

    var init = function(p){

		var $page = document.getElementById(pageId);
		$page.innerHTML = tpl;

        //设置标题
        commons.setTitle(pageId);

		new Vue({
			el:'#' + pageId + '-wrapper',
			data:{
				menurouter: false,
				shortCutsRoutes:commons.data,
				active:"/page-monitor-external-folder",
				header:commons.getHeader(0)
			},
			methods: {

			}

		});
    };

    var destroy = function(){

    };

    var page = {
            path:'/' + pageId,
            component:{
                template:'<div id="' + pageId + '" class="page-wrapper"></div>'
            },
            init:init,
            destroy:destroy
        };

    return page;
});