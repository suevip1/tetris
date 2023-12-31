define([
    'text!' + window.APPPATH + 'component/zk-leader/recordFile/recordFile.html',
    'restfull',
    'jquery',
    'vue',
    'config',
    'element-ui',
    'css!' + window.APPPATH + 'component/zk-leader/recordFile/recordFile.css'
], function (tpl, ajax, $, Vue, config) {

    //组件名称
    var pluginName = 'record-file';

    Vue.component(pluginName, {
        props: [],
        template: tpl,
        data: function () {
            return {
                baseUrl: window.BASEPATH,
                user: '',
                qt: '',
                props: {
                    children: 'children',
                    label: 'name'
                },
                //设备/用户树
                record: {
                    data: [],
                    select: ''
                },
                //录制文件树
                files: [],
                total:0,
                pageSize:10,
                currentPage: 1,
                startTime: '',
                endTime: '',
                date: '',
                fileSelect: '' //选择的文件
            }
        },
        computed: {
            
        },
        methods: {
            currentChange: function (val) {
            	var self = this;
            	self.currentPage = val;
                self.confirm();
            },
            //获取左侧树形菜单的数据
            refreshRecordFile: function () {
                var self = this;
                self.record.data.splice(0, self.record.data.length);
                ajax.post('/monitor/device/find/institution/tree/0/false', null, function (data) {
                    if (data && data.length > 0) {
                        for (var i = 0; i < data.length; i++) {
                            self.record.data.push(data[i]);
                        }
                    }
                });
            },
            //确定选择 左侧
            confirm: function () {
                var self = this;
                var data = self.record.select;
                var id = data.type == 'BUNDLE' ? data.id : null;
                var param = $.parseJSON(data.param);
                var bundleRealType = data.type == 'BUNDLE' && param.realType == 'XT' ? param.realType : null;
                var startTime=self.date?self.format(self.date[0]):null;
                var endTime=self.date?self.format(self.date[1]):null;
                if (!self.record.select) {
                    self.qt.warning('请先选择左侧列表中设备或用户');
                    return;
                }
                var param = {
                    type: data.type,
                    bundleRealType: bundleRealType,//type为BUNDLE时，如果BUNDLE为XT设备传XT
                    bundleId: id, //设备id,type为BUNDLE时设置此字段
                    recordUserId: data.type == 'USER' ? data.id : null, // 录制的用户id,type为USER时设置此字段
                    currentPage: self.currentPage,
                    pageSize: self.pageSize
                };
                if(startTime) param.timeLowerLimit = startTime;
                if(endTime) param.timeUpperLimit = endTime;
                self.files.splice(0, self.files.length);
                ajax.post('/monitor/record/playback/find/by/condition', param, function (data) {
                	var total = data.total;
                	var rows = data.rows;
                	if(rows && rows.length>0){
                		for(var i=0; i<rows.length; i++){
                			self.files.push(rows[i]);
                		}
                		self.total = total;
                	}
                });
            },
            //日期筛选
            filtrate: function () {
                this.confirm();
            },
            //删除文件
            deleteFile: function (id) {
                var self = this;
                ajax.post('/monitor/record/remove/file/' + id, null, function (data) {
                    self.qt.success('删除成功');
                    for(var i=0; i<self.files.length; i++){
                    	if(self.files[i].id == id){
                    		self.files.splice(i, 1);
                    		self.total -= 1;
                    		break;
                    	}
                    }
                    if(self.files.length <= 0){
                    	if(self.currentPage > 1){
                    		self.currentPage -= 1;
                    		self.confirm();
                    	}else if(self.total > 0){
                    		self.currentPage = 1;
                    		self.confirm();
                    	}
                    }
                })
            },
            //下载
            downLoadFile:function (param) {
                var self=this;
                ajax.post('/monitor/record/download/url/'+param.id,{url:param.previewUrl},function (data) {
                    if(data){
                        self.qt.success('开始下载');
                        self.qt.linkedWebview('rightBar',{id:'downloadFile',param:data.downloadUrl});
                    }else{
                        self.qt.warning('无效地址，不能下载');
                    }
                })
            },
            //提交
            confirmSubmit: function () {
                var self=this;
                //播放地址： this.fileSelect.previewUrl
                ajax.post('/command/vod/record/file/start',{
                    businessType:'vodRecordFile',
                    businessInfo:'点播'+self.fileSelect.fileName+'文件',
                    url:self.fileSelect.previewUrl
                },function (data) {
                    self.qt.linkedWebview('hidden',{id:'playRecordFile', params:$.toJSON([data])});
                    self.close();
                })
            },
            //格式化日期
            format: function (str) {
                if (str) {
                    str = str.toString();
                    var str = str.substring(0, 24);
                    var d = new Date(str);
                    var a = [d.getFullYear(), d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds()];
                    for (var i = 0, len = a.length; i < len; i++) {
                        if (a[i] < 10) {
                            a[i] = '0' + a[i];
                        }
                    }
                    str = a[0] + '-' + a[1] + '-' + a[2] + ' ' + a[3] + ':' + a[4] + ':' + a[5];
                    return str;
                }
            },
            // 关闭页面
            close: function () {
                this.qt.destroy();
            }
        },
        mounted: function () {
            var self = this;
            self.qt = new QtContext('recordFile', function () {
                //初始化ajax
                ajax.init({
                    login: config.ajax.login,
                    redirectLogin: false,
                    authname: config.ajax.authname,
                    debug: config.ajax.debug,
                    messenger: {
                        info: function (message, status) {
                            self.qt.info(message);
                        },
                        success: function (message, status) {
                            self.qt.success(message);
                        },
                        warning: function (message, status) {
                            self.qt.warning(message);
                        },
                        error: function (message, status) {
                            self.qt.error(message);
                        }
                    }
                });

                self.refreshRecordFile();

                self.qt.get(['user'], function (variables) {
                    self.user = variables.user ? $.parseJSON(variables.user) : {};
                });
            });
        }
    });

    return Vue;
});