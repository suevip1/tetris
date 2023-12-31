define([
    'text!' + window.APPPATH + 'group/info/combineaudio/forward/page-group-info-forward-combineaudio.html',
    'restfull',
    'config',
    'commons',
    'vue',
    'element-ui',
    'bvc2-header',
    'bvc2-group-nav-side',
    'bvc2-group-table-base',
    'extral'
], function(tpl, ajax, config, commons, Vue){

    var pageId = 'page-group-info-forward-combineaudio';

    var init = function(p){

        var $page = document.getElementById(pageId);
        $page.innerHTML = tpl;

        var v_combineAudio = new Vue({
            el:'#' + pageId + '-wrapper',
            data:{
                menurouter: false,
                shortCutsRoutes:commons.data,
                active:"/page-group-list",
                header:commons.getHeader(0),
                side:{
                    active:'1-2'
                },
                group:p,
                table:{
                    breadcrumb:[
                        {
                            label:'转发',
                            href:'#/page-group-info-forward/'+ p.id + '/' + p.pageSize + '/' + p.currentPage
                        },{
                            label:'混音转发',
                            href:'#/page-group-info-forward-combineaudio/' + p.id + '/' + p.uuid + '/' + p.pageSize + '/' + p.currentPage
                        }
                    ],
                    columns:[
                        {
                            label:'混音源',
                            prop:'bundleName',
                            type:'simple',
                            width:'400'
                        },{
                            label:'混音uuid',
                            prop:'combineAudioUuid',
                            type:'simple'
                        }],
                    load:'/device/group/control/query/forward/combineAudio/' + p.uuid,
                    remove:'/device/group/control/remove/combineAudio/' + p.id,
                    refresh:'/device/group/control/refresh/combineAudio/' + p.id,
                    pk:'combineAudioUuid',
                    search:{
                        text:'',
                        sign:'bundleName',
                        condition:'混音源过滤'
                    },
                    pagesizes:[10, 20, 30, 40],
                    pagesize:10,
                    currentpage:1,
                    combine:[1,2],
                    combineparam:'combineAudioUuid'
                }
            },
            methods:{

            }

        });

    };

    var destroy = function(){

    };

    var groupInfo = {
        path:'/' + pageId + '/:id' + '/:uuid' + '/:pageSize' + '/:currentPage',
        component:{
            template:'<div id="' + pageId + '" class="page-wrapper"></div>'
        },
        init:init,
        destroy:destroy
    };

    return groupInfo;
});
