define([
    'text!' + window.APPPATH + 'component/bvc2-monitor-record/bvc2-monitor-record.html',
    'context',
    'restfull',
    'jquery',
    'vue',
    'element-ui',
    'date',
    'css!' + window.APPPATH + 'component/bvc2-monitor-record/bvc2-monitor-record.css'
], function(tpl, context, ajax, $, Vue){

	//组件名称
    var pluginName = 'bvc2-monitor-record';

    var app = context.getProp('app');

    Vue.component(pluginName, {
        props: [],
        template: tpl,
        data:function(){
            return {
                table:{
                    data:[],
                    page:{
                        currentPage:0,
                        pageSize:20,
                        total:0
                    },
                    prependRow:''
                },
                condition:{
                    mode:'MANUAL',
                    device:'',
                    deviceName:'',
                    timeScope:''
                },
                dialog:{
                    addRecord:{
                        visible:false,
                        tree:{
                            data:[],
                            props:{
                                children:'children',
                                label:'name'
                            },
                            currentUser:'',
                            currentXtDevice:'',
                            currentVideo:'',
                            currentAudio:''
                        },
                        mode:'MANUAL',
                        fileName:'',
                        timeScope:'',
                        loading:false
                    },
                    selectDevice:{
                        visible:false,
                        tree:{
                            data:[],
                            props:{
                                children:'children',
                                label:'name'
                            },
                            currentDevice:''
                        },
                        loading:false
                    }
                }
            }
        },
        computed:{
            condition_mode:function(){
                var self = this;
                return self.condition.mode;
            },
            condition_device:function(){
                var self = this;
                return self.condition.device;
            },
            condition_timeScope:function(){
                var self = this;
                return self.condition.timeScope;
            },
            dialog_addRecord_tree_currentUser:function(){
                var self = this;
                return self.dialog.addRecord.tree.currentUser;
            },
            dialog_addRecord_tree_currentXtDevice:function(){
                var self = this;
                return self.dialog.addRecord.tree.currentXtDevice;
            },
            dialog_addRecord_tree_currentVideo:function(){
                var self = this;
                return self.dialog.addRecord.tree.currentVideo;
            },
            dialog_addRecord_tree_currentAudio:function(){
                var self = this;
                return self.dialog.addRecord.tree.currentAudio;
            }
        },
        watch:{
            condition_mode:function(mode){
                var self = this;
                self.load(1);
            },
            condition_device:function(device){
                var self = this;
                self.load(1);
            },
            condition_timeScope:function(timeScope){
                var self = this;
                self.load(1);
            },
            dialog_addRecord_tree_currentUser:function(v){
                var self = this;
                if(v){
                    self.dialog.addRecord.tree.currentXtDevice = '';
                    self.dialog.addRecord.tree.currentVideo = '';
                    self.dialog.addRecord.tree.currentAudio = '';
                }
            },
            dialog_addRecord_tree_currentXtDevice:function(v){
                var self = this;
                if(v){
                    self.dialog.addRecord.tree.currentUser = '';
                    self.dialog.addRecord.tree.currentVideo = '';
                    self.dialog.addRecord.tree.currentAudio = '';
                }
            },
            dialog_addRecord_tree_currentVideo:function(v){
                var self = this;
                if(v){
                    self.dialog.addRecord.tree.currentUser = '';
                    self.dialog.addRecord.tree.currentXtDevice = '';
                }
            },
            dialog_addRecord_tree_currentAudio:function(v){
                var self = this;
                if(v){
                    self.dialog.addRecord.tree.currentXtDevice = '';
                    self.dialog.addRecord.tree.currentUser = '';
                }
            }
        },
        methods:{
            destroy:function(){

            },
            load:function(currentPage){
                var self = this;
                var condition = {
                    mode:self.condition.mode,
                    device:self.condition.device,
                    currentPage:currentPage,
                    pageSize:self.table.page.pageSize
                };
                if(self.condition.timeScope && self.condition.timeScope.length>0){
                    var datetimePatten = 'yyyy-MM-dd HH:mm:ss';
                    condition.startTime = self.condition.timeScope[0].format(datetimePatten);
                    condition.endTime = self.condition.timeScope[1].format(datetimePatten);
                }
                self.table.data.splice(0, self.table.data.length);
                ajax.post('/monitor/record/load', condition, function(data){
                    var total = data.total;
                    var rows = data.rows;
                    self.table.page.currentPage = currentPage;
                    self.table.page.total = total;
                    if(self.table.prependRow){
                        self.table.data.push(self.table.prependRow);
                    }
                    if(rows && rows.length>0){
                        for(var i=0; i<rows.length; i++){
                            if(self.table.prependRow.id !== rows[i].id)self.table.data.push(rows[i]);
                        }
                    }
                    self.table.prependRow = '';
                });
            },
            rowDelete:function(scope){
                var self = this;
                var row = scope.row;
                var h = self.$createElement;
                self.$msgbox({
                    title:'危险操作',
                    message:h('div', null, [
                        h('div', {class:'el-message-box__status el-icon-warning'}, null),
                        h('div', {class:'el-message-box__message'}, [
                            h('p', null, ['停止任务后可在回放页面查询录制内容，是否继续?'])
                        ])
                    ]),
                    type:'wraning',
                    showCancelButton: true,
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    beforeClose:function(action, instance, done){
                        instance.confirmButtonLoading = true;
                        if(action === 'confirm'){
                            ajax.post('/monitor/record/stop/' + row.id, null, function(data, status){
                                instance.confirmButtonLoading = false;
                                done();
                                if(status !== 200) return;
                                for(var i=0; i<self.table.data.length; i++){
                                    if(self.table.data[i].id === row.id){
                                        self.table.data.splice(i ,1);
                                        break;
                                    }
                                }
                                self.table.page.total -= 1;
                                if(self.table.data.length<=0 && self.table.page.currentPage>1){
                                    self.load(self.table.page.currentPage - 1);
                                }
                            }, null, [403,404,409,500]);
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
            },
            handleAddRecordClose:function(){
                var self = this;
                self.dialog.addRecord.tree.data.splice(0, self.dialog.addRecord.tree.data.length);
                self.dialog.addRecord.tree.currentVideo = '';
                self.dialog.addRecord.tree.currentAudio = '';
                self.dialog.addRecord.mode = 'MANUAL';
                self.dialog.addRecord.fileName = '';
                self.dialog.addRecord.timeScope = '';
                self.dialog.addRecord.loading = false;
                self.dialog.addRecord.visible = false;
                app.removeDeviceLoop('monitor-record-encode-tree');
            },
            handleAddRecord:function(){
                var self = this;
                self.dialog.addRecord.visible = true;
                self.dialog.addRecord.tree.data.splice(0, self.dialog.addRecord.tree.data.length);
                ajax.post('/monitor/device/find/institution/tree/0/true', null, function(data){
                    if(data && data.length>0){
                        for(var i=0; i<data.length; i++){
                            self.dialog.addRecord.tree.data.push(data[i]);
                        }
                        app.addDeviceLoop('monitor-record-encode-tree', data);
                    }
                });
            },
            addRecordModeChange:function(){
                var self = this;
                self.dialog.addRecord.timeScope = '';
            },
            handleAddRecordCommit:function(){
                var self = this;
                if(!self.dialog.addRecord.tree.currentVideo && !self.dialog.addRecord.tree.currentAudio && !self.dialog.addRecord.tree.currentUser && !self.dialog.addRecord.tree.currentXtDevice){
                    self.$message({
                        type:'warning',
                        message:'请选择录制内容！'
                    });
                    return;
                }
                if(!self.dialog.addRecord.fileName){
                    self.$message({
                        type:'warning',
                        message:'请输入文件名！'
                    });
                    return;
                }
                var task = {
                    mode:self.dialog.addRecord.mode,
                    fileName:self.dialog.addRecord.fileName
                };
                if(self.dialog.addRecord.timeScope && self.dialog.addRecord.timeScope.length>0){
                    var datetimePatten = 'yyyy-MM-dd HH:mm:ss';
                    task.startTime = self.dialog.addRecord.timeScope[0].format(datetimePatten);
                    task.endTime = self.dialog.addRecord.timeScope[1].format(datetimePatten);
                }

                if(self.dialog.addRecord.tree.currentUser){
                    //录制用户
                    var currentUser = $.parseJSON(self.dialog.addRecord.tree.currentUser);
                    task.targetUserId = currentUser.userId;
                    self.dialog.addRecord.loading = true;
                    ajax.post('/monitor/record/add/record/user/task', task, function(data, status){
                        self.dialog.addRecord.loading = false;
                        if(status !== 200) return;
                        if(self.dialog.addRecord.mode === self.condition.mode){
                            self.table.data.splice(0, 0, data);
                        }else{
                            self.condition.mode = self.dialog.addRecord.mode;
                            self.table.prependRow = data;
                        }
                        self.handleAddRecordClose();
                    }, null, [403,404,409,500]);
                }else if(self.dialog.addRecord.tree.currentXtDevice){
                    //录制xt设备
                    var currentXtDevice = $.parseJSON(self.dialog.addRecord.tree.currentXtDevice);
                    task.bundleId = currentXtDevice.bundleId;
                    self.dialog.addRecord.loading = true;
                    ajax.post('/monitor/record/add/xt/device', task, function(data, status){
                        self.dialog.addRecord.loading = false;
                        if(status !== 200) return;
                        if(self.dialog.addRecord.mode === self.condition.mode){
                            self.table.data.splice(0, 0, data);
                        }else{
                            self.condition.mode = self.dialog.addRecord.mode;
                            self.table.prependRow = data;
                        }
                        self.handleAddRecordClose();
                    }, null, [403,404,409,500]);
                }else{
                    //录制设备
                    if(self.dialog.addRecord.tree.currentVideo){
                        var currentVideo = $.parseJSON(self.dialog.addRecord.tree.currentVideo);
                        task.videoBundleId = currentVideo.bundleId;
                        task.videoBundleName = currentVideo.bundleName;
                        task.videoBundleType = currentVideo.bundleType;
                        task.videoLayerId = currentVideo.nodeUid;
                        task.videoChannelId = currentVideo.channelId;
                        task.videoBaseType = currentVideo.channelType;
                        task.videoChannelName = currentVideo.channelId;
                    }

                    if(self.dialog.addRecord.tree.currentAudio){
                        var currentAudio = $.parseJSON(self.dialog.addRecord.tree.currentAudio);
                        task.audioBundleId = currentAudio.bundleId;
                        task.audioBundleName = currentAudio.bundleName;
                        task.audioBundleType = currentAudio.bundleType;
                        task.audioLayerId = currentAudio.nodeUid;
                        task.audioChannelId = currentAudio.channelId;
                        task.audioBaseType = currentAudio.channelType;
                        task.audioChannelName = currentAudio.channelId;
                    }
                    self.dialog.addRecord.loading = true;

                    ajax.post('/monitor/record/add', task, function(data, status){
                        self.dialog.addRecord.loading = false;
                        if(status !== 200) return;
                        if(self.dialog.addRecord.mode === self.condition.mode){
                            self.table.data.splice(0, 0, data);
                        }else{
                            self.condition.mode = self.dialog.addRecord.mode;
                            self.table.prependRow = data;
                        }
                        self.handleAddRecordClose();
                    }, null, [403,404,409,500]);
                }
            },
            handleSelectDeviceClose:function(){
                var self = this;
                self.dialog.selectDevice.tree.data.splice(0, self.dialog.selectDevice.tree.data.length);
                self.dialog.selectDevice.tree.currentDevice = '';
                self.dialog.selectDevice.loading = false;
                self.dialog.selectDevice.visible = false;
                app.removeDeviceLoop('monitor-record-device-tree');
            },
            handleSelectDevice:function(){
                var self = this;
                self.dialog.selectDevice.visible = true;
                self.dialog.selectDevice.tree.data.splice(0, self.dialog.selectDevice.tree.data.length);
                ajax.post('/monitor/device/find/institution/tree/0/false', null, function(data){
                    if(data && data.length>0){
                        for(var i=0; i<data.length; i++){
                            self.dialog.selectDevice.tree.data.push(data[i]);
                        }
                        app.addDeviceLoop('monitor-record-device-tree', data);
                    }
                });
            },
            handleSelectDeviceCommit:function(){
                var self = this;
                var currentDevice = $.parseJSON(self.dialog.selectDevice.tree.currentDevice);
                if(currentDevice.bundleId){
                    self.condition.device = currentDevice.bundleId;
                    self.condition.deviceName = currentDevice.bundleName;
                }else if(currentDevice.userId){
                    self.condition.device = currentDevice.encoderId;
                    self.condition.deviceName = currentDevice.username;
                }
                self.handleSelectDeviceClose();
            }
        },
        mounted:function(){
            var self = this;
            self.load(1);
        }
    });

    return Vue;
});