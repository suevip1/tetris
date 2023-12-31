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
        'bvc2-monitor-ptzctrl':window.APPPATH + 'component/bvc2-monitor-ptzctrl/bvc2-monitor-ptzctrl',
        'bvc2-monitor-user-called-message':window.APPPATH + 'component/bvc2-monitor-message/user-called/bvc2-monitor-user-called-message',
        'bvc2-monitor-call':window.APPPATH + 'component/bvc2-monitor-call/bvc2-monitor-call',
        'native-sip-player':window.APPPATH + 'component/jQuery/zk_Player/zk_SipPlayer/js/zk_SipPlayer',
        'player':window.APPPATH + 'component/jQuery/player/js/Tetris.player',
        'player-panel':window.APPPATH + 'component/jQuery/playerPanel/js/Tetris.playerPanel',
        'jquery.layout.auto':window.APPPATH + 'component/jQuery/jQuery.layout.auto/js/jQuery.layout.auto',
        'bvc2-dialog-single-osd':window.APPPATH + 'component/bvc2-dialog-single-osd/bvc2-dialog-single-osd',
        'bvc2-monitor-terminal-live':window.APPPATH + 'component/bvc2-monitor-live/terminal/bvc2-monitor-terminal-live'

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
        },
        'native-sip-player':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'player':{
            deps:['jquery'],
            exports:'jQuery'
        },
        'player-panel':{
            deps:['player'],
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
    'bvc2-dialog-single-osd',
    'bvc2-monitor-terminal-live'
], function(storage, Vue, config, ajax){

    var app = null;

    //缓存token
    storage.setItem(config.ajax.authname, window.TOKEN);

    //初始化ajax
    ajax.init({
        login:config.ajax.login,
        authname:config.ajax.authname,
        debug:config.ajax.debug,
        messenger:{
            info:function(message, status){
                if(!app) return;
                app.$message({
                    message: message,
                    type: 'info'
                });
            },
            success:function(message, status){
                if(!app) return;
                app.$message({
                    message: message,
                    type: 'success'
                });
            },
            warning:function(message, status){
                if(!app) return;
                app.$message({
                    message: message,
                    type: 'warning'
                });
            },
            error:function(message, status){
                if(!app) return;
                app.$message({
                    message: message,
                    type: 'error'
                });
            }
        }
    });

    app = new Vue({
        data:function(){
            return {
                loading:false
            };
        }
    }).$mount('#app');

    //页面销毁方法
    window.destroy = function(){
        app.$refs.bvc2MonitorTerminalLive.destroy();
    }

});