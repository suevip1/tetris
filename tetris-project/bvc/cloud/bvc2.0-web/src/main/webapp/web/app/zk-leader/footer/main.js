require.config({
    baseUrl: window.BASEPATH,
    paths: {
        /* lib */
        'text':window.LIBPATH + 'frame/requireJS/plugins/text',
        'css':window.LIBPATH + 'frame/requireJS/plugins/css',
        'vue':window.LIBPATH + 'frame/vue/vue-2.5.16',
        'jquery':window.LIBPATH + 'frame/jQuery/jquery-2.2.3.min',
        'json':window.LIBPATH + 'frame/jQuery/jquery.json',
        'element-ui':window.LIBPATH + 'ui/element-ui/element-ui-2.4.3.min',

        /* commons */
        'date':window.COMMONSPATH + 'date/date.ext',
        'string':window.COMMONSPATH + 'string/string.ext',
        'storage':window.COMMONSPATH + 'storage/storage.ext',
        'restfull':window.COMMONSPATH + 'restfull/restfull.api',

        'config':window.APPPATH + 'config',

        /* components */
        'jquery.layout.auto':window.APPPATH + 'component/jQuery/jQuery.layout.auto/js/jQuery.layout.auto',
        'bvc2-layout-buttons':window.APPPATH + 'component/bvc2-layout-buttons/bvc2-layout-buttons',
        'history-message':window.APPPATH + 'component/zk-leader/footer/historyMessage'

    },
    shim:{
        'vue':{
            exports: 'Vue'
        },
        'element':{
            deps:['vue']
        },
        'jquery':{
            exports:'jQuery'
        },
        'json':{
            deps:['jquery'],
            exports:'jQuery'
        }
    }
});

require([
    'storage',
    'vue',
    'config',
    'restfull',
    'element-ui',
    'history-message'
], function(storage, Vue, config, ajax){

    var app = null;

    //缓存token
    storage.setItem(config.ajax.authname, window.TOKEN);

    app = new Vue({
        data:function(){
            return {
                loading:false
            };
        }
    }).$mount('#app');

});