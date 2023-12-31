define([
    'text!' + window.APPPATH + 'component/zk/business/forward/zk-business-forward.html',
    'restfull',
    'jquery',
    'vue',
    'config',
    'element-ui',
    'css!' + window.APPPATH + 'component/zk/business/forward/zk-business-forward.css'
], function(tpl, ajax, $, Vue,config){

	//组件名称
    var pluginName = 'zk-business-forward';

    Vue.component(pluginName, {
        props: [],
        template: tpl,
        data:function(){
            return {
            	baseUrl:window.BASEPATH,
                currentTab:'0',
                qt:'',
                group:'',
                tree:{
                    props:{
                        children:'children',
                        label:'name'
                    },
                    resource:{
                        data:[],
                        select:[]
                    },
                    device:{
                        data:[],
                        select:[]
                    },
                    record:{
                        data:[],
                        select:[]
                    },
                    members:{
                        data:[],
                        select:[]
                    }
                },
                table:{
                    data:[
                        /*{
                            id:'转发id',
                            time:'转发时间',
                            srcType:'源类型',
                            srcInfo:'源信息',
                            dstInstitutionInfo:'目标成员组织信息',
                            dstUserInfo:'目标成员信息',
                            status:'转发状态'
                        },{
                            id:'转发id',
                            time:'转发时间',
                            srcType:'源类型',
                            srcInfo:'源信息',
                            dstInstitutionInfo:'目标成员组织信息',
                            dstUserInfo:'目标成员信息',
                            status:'转发状态'
                        }*/
                    ],
                    total:0,
                    currentPage:1,
                    pageSize:50
                },
                table2:{
                    data:[
                    ],
                    total:0,
                    currentPage:1,
                    pageSize:50
                }
            }
        },
        computed:{

        },
        watch:{
            currentTab:function(){
                var self = this;
                if(self.currentTab == 0){
                    //文件资源
                    if(self.tree.resource.data.length === 0){
                        self.refreshFile();
                    }
                    self.onVodResourceUnCheckAll();
                }else if(self.currentTab == 1){
                    //设备资源
                    if(self.tree.device.data.length === 0){
                        self.refreshDevice();
                    }
                    self.onBundleUnCheckAll();
                }else if(self.currentTab == 2){
                    //录像资源
                    if(self.tree.record.data.length === 0){
                        self.refreshRecord();
                    }
                    self.onRecordUnCheckAll();
                }else if(self.currentTab == 3){
                    self.loadAllForward();
                }
            }
        },
        methods:{
            load:function(currentPage){
                var self = this;
                ajax.post('/command/query/command/forward', {
                    id:self.group.id,
                    currentPage:currentPage,
                    pageSize:self.table.pageSize
                }, function(data){
                    var rows = data.rows;
                    self.table.data.splice(0, self.table.data.length);
                    if(rows && rows.length>0){
                        for(var i=0; i<rows.length; i++){
                            self.table.data.push(rows[i]);
                        }
                    }
                    self.table.total = data.total;
                    self.table.currentPage = currentPage;
                });
            },
            handleClose:function(){
                var self = this;
                self.qt.destroy();
            },
            currentChange:function(currentPage){
                var self = this;
                self.load(currentPage);
            },
            onVodResourceCheckChange:function(data){
                var self = this;
                for(var i=0; i<self.tree.resource.select.length; i++){
                    if(self.tree.resource.select[i] === data){
                        self.tree.resource.select.splice(i, 1);
                        return;
                    }
                }
                self.tree.resource.select.push(data);
            },
            onVodResourceUnCheckAll:function(){
                var self = this;
                for(var i=0; i<self.tree.resource.select.length; i++){
                    self.tree.resource.select[i].checked = false;
                }
                self.tree.resource.select.splice(0, self.tree.resource.select.length);
            },
            onUserCheckChange:function(data){
                var self = this;
                for(var i=0; i<self.tree.members.select.length; i++){
                    if(self.tree.members.select[i] === data){
                        self.tree.members.select.splice(i, 1);
                        return;
                    }
                }
                self.tree.members.select.push(data);
            },
            onUserUnCheckAll:function(){
                var self = this;
                for(var i=0; i<self.tree.members.select.length; i++){
                    self.tree.members.select[i].checked = false;
                }
                self.tree.members.select.splice(0, self.tree.members.select.length);
            },
            onBundleCheckChange:function(data){
                var self = this;
                for(var i=0; i<self.tree.device.select.length; i++){
                    if(self.tree.device.select[i] === data){
                        self.tree.device.select.splice(i, 1);
                        return;
                    }
                }
                self.tree.device.select.push(data);
            },
            onBundleUnCheckAll:function(){
                var self = this;
                for(var i=0; i<self.tree.device.select.length; i++){
                    self.tree.device.select[i].checked = false;
                }
                self.tree.device.select.splice(0, self.tree.device.select.length);
            },
            //录制回放的复选框的change事件
            onRecordCheckChange: function (data) {
                var self = this;
                var index = self.tree.record.select.indexOf(data); //返回所在的索引

                if (index > -1) {
                    self.tree.record.select.splice(index, 1);
                } else {
                    self.tree.record.select.push(data);
                }
            },
            onRecordUnCheckAll:function(){
                var self = this;
                for(var i=0; i<self.tree.record.select.length; i++){
                    self.tree.record.select[i].checked = false;
                }
                self.tree.record.select.splice(0, self.tree.record.select.length);
            },
            refreshDevice:function(){
                var self = this;
                self.tree.device.data.splice(0, self.tree.device.data.length);
                ajax.post('/command/query/find/institution/tree/bundle/2/false/0', null, function(data){
                    if(data && data.length>0){
                        for(var i=0; i<data.length; i++){
                            self.tree.device.data.push(data[i]);
                        }
                    }
                });
            },
            refreshFile:function(){
                var self = this;
                self.tree.resource.data.splice(0, self.tree.resource.data.length);
                ajax.post('/monitor/vod/query/resource/tree', null, function(data){
                    if(data && data.length>0){
                        for(var i=0; i<data.length; i++){
                            self.tree.resource.data.push(data[i]);
                        }
                    }
                });
            },
            refreshRecord:function(){
                var self=this;
                ajax.post('/command/record/query',null,function (data) {
                    if (data.groups.length) {
                        // 转换数据格式：给最外层加type属性，给records加groupName（用开始时间）把records和fragments属性改成children，fragments里的info就是名称
                        for (var i = 0; i < data.groups.length; i++) {
                            data.groups[i].type = 'FOLDER';
                            if (data.groups[i].records.length) {
                                data.groups[i].children = data.groups[i].records;
                                for (var j = 0; j < data.groups[i].records.length; j++) {
                                    //给每项加上type和数据本身所在层级
                                    data.groups[i].records[j].type = 'RECORD_PLAYBACK';
                                    data.groups[i].records[j].level = 2;
                                    data.groups[i].records[j].children = data.groups[i].records[j].fragments;
                                    data.groups[i].children[j].groupName = data.groups[i].records[j].startTime;
                                    if (data.groups[i].records[j].fragments.length) {
                                        for (var k = 0; k < data.groups[i].records[j].fragments.length; k++) {
                                            data.groups[i].records[j].fragments[k].groupName = data.groups[i].records[j].fragments[k].info;
                                            data.groups[i].records[j].fragments[k].type = 'RECORD_PLAYBACK';
                                            data.groups[i].records[j].fragments[k].level = 3;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    self.tree.record.data=data.groups;
                })
            },
            loadAllForward:function(){
                var self = this;
                ajax.post('/command/system/all/forward', {
                    //currentPage:currentPage,
                    //pageSize:self.table2.pageSize
                }, function(data){
                    var rows = data.rows;
                    self.table2.data.splice(0, self.table2.data.length);
                    if(rows && rows.length>0){
                        for(var i=0; i<rows.length; i++){
                            self.table2.data.push(rows[i]);
                        }
                    }
                    self.table2.total = data.total;
                    self.table2.currentPage = currentPage;
                });
            },
            handleRowRemove:function(scope){
                var self = this;
                var row = scope.row;
                ajax.post('/command/basic/forward/stop/by/chairman', {
                    id:self.group.id,
                    forwardIds: $.toJSON([row.id])
                }, function(){
                    for(var i=0; i<self.table.data.length; i++){
                        if(self.table.data[i] === row){
                            self.table.data.splice(i, 1);
                            break;
                        }
                    }
                    self.table.total -= 1;
                });
            },
            handleForwardStopAll:function(){
                var self = this;
                ajax.post('/command/forward/stop/all', {id:self.group.id}, function(){
                    self.table.data.splice(0, self.table.data.length);
                    self.table.total = 0;
                });
            },
            handleForward:function(){
                var self = this;
                if(self.tree.members.select.length <= 0){
                    self.qt.warning('消息提示', '您还没有选择转发对象');
                    return;
                }
                var dst = [];
                for(var i=0; i<self.tree.members.select.length; i++){
                    dst.push(self.tree.members.select[i].id);
                }
                if(self.currentTab == 0){
                    if(self.tree.resource.select.length <= 0){
                        self.qt.warning('消息提示', '您还没有选择资源文件');
                        return;
                    }
                    var src = [];
                    for(var i=0; i<self.tree.resource.select.length; i++){
                        src.push(self.tree.resource.select[i].id);
                    }
                    ajax.post('/command/basic/forward/file', {
                        id:self.group.id,
                        src: $.toJSON(src),
                        dst: $.toJSON(dst)
                    }, function(rows){
                        for(var i=0; i<rows.length; i++){
                            self.table.data.push(rows[i]);
                        }
                        self.table.total += rows.length;
                    });
                }else if(self.currentTab == 1){
                    if(self.tree.device.select.length <= 0){
                        self.qt.warning('消息提示', '您还没有选择设备资源');
                        return;
                    }
                    var src = [];
                    for(var i=0; i<self.tree.device.select.length; i++){
                        src.push(self.tree.device.select[i].id);
                    }
                    ajax.post('/command/basic/forward/device', {
                        id:self.group.id,
                        src: $.toJSON(src),
                        dst: $.toJSON(dst)
                    }, function(rows){
                        for(var i=0; i<rows.length; i++){
                            self.table.data.push(rows[i]);
                        }
                        self.table.total += rows.length;
                    });
                }else if(self.currentTab == 2){
                    if(self.tree.device.record.length <= 0){
                        self.qt.warning('消息提示', '您还没有选择录像资源');
                        return;
                    }
                    var src = [];
                    for(var i=0; i<self.tree.record.select.length; i++){
                        src.push(self.tree.record.select[i].id);
                    }
                    ajax.post('/command/forward/record', {
                        id:self.group.id,
                        src: $.toJSON(src),
                        dst: $.toJSON(dst)
                    }, function(rows){
                        for(var i=0; i<rows.length; i++){
                            self.table.data.push(rows[i]);
                        }
                        self.table.total += rows.length;
                    });
                }
            }
        },
        mounted:function(){
            var self = this;
            self.qt = new QtContext('businessForward', function(){
                var params = self.qt.getWindowParams();
                var id = params.id;

                //初始化ajax
                ajax.init({
                    login:config.ajax.login,
                    authname:config.ajax.authname,
                    debug:config.ajax.debug,
                    messenger:{
                        info:function(message, status){
                           self.qt.info(message)
                        },
                        success:function(message, status){
                            self.qt.success(message)
                        },
                        warning:function(message, status){
                            self.qt.warning(message)
                        },
                        error:function(message, status){
                           self.qt.error(meaage)
                        }
                    }
                });

                ajax.post('/command/basic/query/members', {id:id}, function(data){
                    self.group = data;
                    for(var i=0; i<data.members.length; i++){
                        self.tree.members.data.push(data.members[i]);
                    }
                    self.load(1);
                });
                self.refreshFile();
            });
        }
    });

    return Vue;
});