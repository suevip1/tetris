/**
 * Created by lvdeyang on 2018/12/24 0024.
 */
define([
    'text!' + window.APPPATH + 'omms/hardware/server/page-omms-hardware-server.html',
    'config',
    'jquery',
    'restfull',
    'context',
    'commons',
    'vue',
    'ace-for-shell',
    'element-ui',
    'mi-frame',
    'date',
    'css!' + window.APPPATH + 'omms/hardware/server/page-omms-hardware-server.css'
], function(tpl, config, $, ajax, context, commons, Vue, ace){

    var pageId = 'page-omms-hardware-server';

    var charts = {};

    var init = function(){

        //璁剧疆鏍囬
        commons.setTitle(pageId);

        var $page = document.getElementById(pageId);
        $page.innerHTML = tpl;

        new Vue({
            el: '#' + pageId + '-wrapper',
            data: {
                basePath:window.BASEPATH,
                menus: context.getProp('menus'),
                user: context.getProp('user'),
                groups: context.getProp('groups'),
                table:{
                    data:[],
                    page:{
                        currentPage:0,
                        pageSize:50,
                        pageSizes:[50, 100, 500, 1000],
                        total:0
                    }
                },
                dialog:{
                    addServer:{
                        visible:false,
                        name:'',
                        ip:'',
                        gadgetPort:'',
                        creator:'',
                        remark:''
                    },
                    editServer:{
                        visible:false,
                        id:'',
                        name:'',
                        ip:'',
                        gadgetPort:'',
                        creator:'',
                        remark:''
                    }
                }
            },
            computed:{

            },
            watch:{

            },
            methods:{
                load:function(currentPage){
                    var self = this;
                    self.table.data.splice(0, self.table.data.length);
                    ajax.post('/server/load', {
                        currentPage:currentPage,
                        pageSize:self.table.page.pageSize
                    }, function(data){
                        var total = data.total;
                        var rows = data.rows;
                        if(rows && rows.length>0){
                            for(var i=0; i<rows.length; i++){
                                self.table.data.push(rows[i]);
                            }
                        }
                        self.table.page.currentPage = currentPage;
                        self.table.page.total = total;
                    });
                },
                handleCreate:function(){
                    var self = this;
                    self.dialog.addServer.visible = true;
                },
                handleAddServerClose:function(){
                    var self = this;
                    self.dialog.addServer.visible = false;
                    self.dialog.addServer.name = '';
                    self.dialog.addServer.ip = '';
                    self.dialog.addServer.gadgetPort = '';
                    self.dialog.addServer.creator = '';
                    self.dialog.addServer.remark = '';
                },
                handleAddServerSubmit:function(){
                    var self = this;
                    ajax.post('/server/add',{
                        name:self.dialog.addServer.name,
                        ip:self.dialog.addServer.ip,
                        gadgetPort:self.dialog.addServer.gadgetPort,
                        remark:self.dialog.addServer.remark,
                        creator:self.dialog.addServer.creator
                    }, function(data){
                        self.table.data.splice(0, 0, data);
                        self.table.page.total += 1;
                        self.handleAddServerClose();
                    });
                },
                handleDelete:function(){
                    var self = this;
                },
                rowKey:function(row){
                    return row.id;
                },
                handleRowEdit:function(scope){
                    var self = this;
                    var row = scope.row;
                    self.dialog.editServer.visible = true;
                    self.dialog.editServer.id = row.id;
                    self.dialog.editServer.name = row.name;
                    self.dialog.editServer.ip = row.ip;
                    self.dialog.editServer.gadgetPort = row.gadgetPort;
                    self.dialog.editServer.creator = row.creator;
                    self.dialog.editServer.remark = row.remark;
                },
                handleEditServerClose:function(){
                    var self = this;
                    self.dialog.editServer.visible = false;
                    self.dialog.editServer.id = '';
                    self.dialog.editServer.name = '';
                    self.dialog.editServer.ip = '';
                    self.dialog.editServer.gadgetPort = '';
                    self.dialog.editServer.creator = '';
                    self.dialog.editServer.remark = '';
                },
                handleEditServerSubmit:function(){
                    var self = this;
                    ajax.post('/server/edit', {
                        id:self.dialog.editServer.id,
                        name:self.dialog.editServer.name,
                        ip:self.dialog.editServer.ip,
                        gadgetPort:self.dialog.editServer.gadgetPort,
                        remark:self.dialog.editServer.remark,
                        creator:self.dialog.editServer.creator
                    }, function(data){
                        for(var i=0; i<self.table.data.length; i++){
                            if(self.table.data[i].id === data.id){
                                self.table.data[i].id = data.id;
                                self.table.data[i].name = data.name;
                                self.table.data[i].ip = data.ip;
                                self.table.data[i].gadgetPort = data.gadgetPort;
                                self.table.data[i].remark = data.remark;
                                self.table.data[i].creator = data.creator;
                                break;
                            }
                        }
                        self.handleEditServerClose();
                    });
                },
                handleRowDelete:function(scope){
                    var self = this;
                    var row = scope.row;

                    var h = self.$createElement;
                    self.$msgbox({
                        title:'危险操作',
                        message:h('div', null, [
                            h('div', {class:'el-message-box__status el-icon-warning'}, null),
                            h('div', {class:'el-message-box__message'}, [
                                h('p', null, ['此操作将永久删除该服务器，且不可恢复，是否继续?'])
                            ])
                        ]),
                        type:'wraning',
                        showCancelButton: true,
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        beforeClose:function(action, instance, done){
                            instance.confirmButtonLoading = true;
                            if(action === 'confirm'){
                                ajax.post('/server/delete', {
                                    id:row.id
                                }, function(data, status){
                                    instance.confirmButtonLoading = false;
                                    if(status !== 200) return;
                                    for(var i=0; i<self.table.data.length; i++){
                                        if(self.table.data[i].id === row.id){
                                            self.table.data.splice(i, 1);
                                            break;
                                        }
                                    }
                                    self.table.page.total -= 1;
                                    if(self.table.total>0 && self.table.data.length===0){
                                        self.load(self.table.page.currentPage - 1);
                                    }
                                    done();
                                }, null, ajax.NO_ERROR_CATCH_CODE);
                            }else{
                                instance.confirmButtonLoading = false;
                                done();
                            }
                        }
                    }).catch(function(){});
                },
                handleSizeChange:function(pageSize){
                    var self = this;
                    self.table.page.pageSize = pageSize;
                    self.load(1);
                },
                handleCurrentChange:function(currentPage){
                    var self = this;
                    self.load(currentPage);
                }
            },
            mounted:function(){
                var self = this;
                self.load(1);
            }
        });

    };

    var destroy = function(){

    };

    var groupList = {
        path:'/' + pageId,
        component:{
            template:'<div id="' + pageId + '" class="page-wrapper"></div>'
        },
        init:init,
        destroy:destroy
    };

    return groupList;

});