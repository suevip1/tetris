define([
    'text!' + window.APPPATH + 'component/bvc2-preview-layout/bvc2-preview-layout.html',
    'text!' + window.APPPATH + 'component/bvc2-preview-layout/bvc2-preview-layout-polling-dialog.html',
    'restfull',
    'context',
    'jquery.layout.auto',
    'vue',
    'element-ui',
    'bvc2-dialog-set-forward',
    'text!' + window.APPPATH + 'component/bvc2-preview-layout/flowplayer-3.2.12.min',
], function(tpl, dialog, ajax, context, $, Vue){

    //组件名称
    var bvc2AutoLayout = 'bvc2-preview-layout';

    var getLayoutContainer = function(el){
        return $(el).find('.bvc2-preview-layout-container');
    };

    var playerStateCode = {
        "-1": "初始化中",
        "1": "已停止",
        "2": "加载中（可能一直不成功）",
        "3": "播放中",
        "4": "暂停"
    };

    //设置定时器，检查播放器状态
    setInterval(function(){
        //console.log('检查播放器状态');
        var players = $.find('.flowplayer');
        for(var index in players){
            var playerObj = players[index];
            var playId = playerObj.id;
            var player = $f(playId);
            //var stateCode = player.getState();
            try{
                var stateCode = player.getState();
            }catch(e){
                console.log(playId + '可能崩溃，正在重置');
                var url = playerObj.getAttribute('url');
                //$.find('#' + playId).remove;//不需移除即可重置
                setPlayer(playId, url);
                continue;
            }
            if(stateCode==1 || stateCode==4){
                console.log(playId + '检查播放器状态:' + stateCode);
                player.play();
            }
            if(stateCode==2){
                console.log(playId + '检查播放器状态:' + stateCode);
                setTimeout(function(){
                    var player = $f(playId);
                    var stateCode2 = player.getState();
                    console.log(playId + '状态为2后再次检查播放器状态：' + stateCode2);
                    if(stateCode2==2){
                        //console.log(playId + '检查播放器状态依然为2，强制播放');
                        player.play();
                    }
                }, 2500);
            }
        }
    }, 3000);

    //设置播放器
    var setPlayer = function(playId, playUrl){
        var onFinishVideoFun = function(){
            console.log(playId + '预览停止，播放器状态码：' + $f(playId).getState());
            //$f(playId).play();
        };
        var playerUrl = window.APPPATH + 'component/bvc2-preview-layout/flowplayer.commercial.swf';
        var hlsPlayerUrl = window.APPPATH + 'component/bvc2-preview-layout/flashlsFlowPlayer.swf';
        try {
            flowplayer(playId, {src: playerUrl, wmode: "opaque"}, {
                //onPause: onPauseVideoFun,
                //onResume: onResumeVideoFun,
                onFinish: onFinishVideoFun,
                plugins: {
                    controls: {
                        scrubber: false,
                        time: false
                        //opacity: 0.5,
                    },
                    flashls: {
                        url: hlsPlayerUrl
                    }
                },
                clip: {
                    url: playUrl,
                    urlResolvers: "flashls",
                    provider: "flashls"
                }
            });//.ipad();
        }catch(e){
            console.log(e);
            return;
        }
    };

    var tdHtml = '<div class="screen-config-polling-time" style="width:95px; position:absolute; left:0; top:0; display:none;">' +
                     '<div class="el-input el-input-group el-input--mini">' +
                         '<input type="text" autocomplete="off" placeholder="轮询间隔" class="el-input__inner" value="5秒">' +
                     '</div>'+
                 '</div>' +
                 '<button class="el-button el-button--text screen-config-empty"><span class="el-icon-delete"></span></button>'+
                 '<div class="screen-config-text"></div>' +
                 '<div role="switch" class="el-switch screen-config-polling-status-switch is-checked">' +
                     '<input type="checkbox" name="" true-value="true" class="el-switch__input">' +
                     '<span class="el-switch__label el-switch__label--left">' +
                         '<span>暂停</span>' +
                     '</span>' +
                     '<span class="el-switch__core" style="width: 40px;"></span>' +
                     '<span class="el-switch__label el-switch__label--right is-active">' +
                         '<span aria-hidden="true">开始</span>' +
                     '</span>' +
                 '</div>'+
                 '<div class="screen-config-polling-switch">' +
                     '<label>' +
                            //原 开启轮询
                         '<span class="el-checkbox__label"></span>' +
                     '</label>'+
                 '</div>';

    //设置轮询状态
    var setPollingStatus = function($td, pollingStatus){
        var $pollingStatus = $td.find('.screen-config-polling-status-switch');
        var $labelLeft = $pollingStatus.find('.el-switch__label--left');
        var $labelRight = $pollingStatus.find('.el-switch__label--right');
        if(pollingStatus === 'RUN'){
            $pollingStatus.addClass('is-checked');
            $labelLeft.removeClass('is-active');
            $labelRight.addClass('is-active');
        }else if(pollingStatus === 'PAUSE'){
            $pollingStatus.removeClass('is-checked');
            $labelLeft.addClass('is-active');
            $labelRight.removeClass('is-active');
        }
    };

    //获取轮询状态
    var getPollingStatus = function($td){
        var $pollingStatus = $td.find('.el-switch screen-config-polling-status-switch');
        if($pollingStatus.is('.is-checked')){
            return 'RUN';
        }else{
            return 'PAUSE';
        }
    };

    //设置是否轮询
    var setPolling = function($td, isPolling){
        var $checkbox = $td.find('.screen-config-polling-switch .el-checkbox');
        var $td = $checkbox.closest('td');
        if(isPolling){
            $checkbox.addClass('is-checked');
            $checkbox.find('.el-checkbox__input').addClass('is-checked');
            $td.find('.screen-config-polling-time').show();
            $td.find('.screen-config-polling-status-switch').show();
        }else{
            $checkbox.removeClass('is-checked');
            $checkbox.find('.el-checkbox__input').removeClass('is-checked');
            $td.find('.screen-config-polling-time').hide();
            $td.find('.screen-config-polling-status-switch').hide();
        }
    };

    //判断分屏是否是轮询状态
    var isPolling = function($td){
        return $td.find('.screen-config-polling-switch .el-checkbox').is('.is-checked');
    };

    //设置轮询时间
    var setPollingTime = function($td, time){
        time = parseInt(time);
        if(isNaN(time)) time = 5;
        time = time + '秒';
        $td.find('.screen-config-polling-time .el-input__inner').val(time);
    };

    //获取轮询时间
    var getPollingTime = function($td){
        return parseInt($td.find('.screen-config-polling-time .el-input__inner').val());
    };

    Vue.component(bvc2AutoLayout, {
        props: ['group', 'config', 'video', 'layoutmodes'],
        template: tpl,
        data:function(){
            return {
                layout:'',
                screenLayout:'',
                members:[],
                roles:[],
                values:[],
                roleValues:[],
                options:{
                    columnOptions:[
                        {
                            value:1,
                            label:1
                        },{
                            value:2,
                            label:2
                        },{
                            value:3,
                            label:3
                        },{
                            value:4,
                            label:4
                        }
                    ],
                    rowOptions_bak:[
                        {
                            value:1,
                            label:1
                        },{
                            value:2,
                            label:2
                        },{
                            value:3,
                            label:3
                        },{
                            value:4,
                            label:4
                        }
                    ],
                    rowOptions:[
                        {
                            value:1,
                            label:"1×1"
                        },{
                            value:2,
                            label:"2×2"
                        //},{
                        //    value:3,
                        //    label:3
                        //},{
                        //    value:4,
                        //    label:4
                        }
                    ]
                },
                style:{
                    play:{
                        isLoading:false
                    },
                    forward:{
                        isLoading:false
                    },
                    startRecord:{
                        isLoading:false
                    },
                    stopRecord:{
                        isLoading:false
                    },
                    reset:{
                        isLoading:false
                    },
                    saveScheme:{
                        isLoading:false
                    }
                }
            }
        },
        watch:{
            layout:function(){
                this.refreshLayout();
            },
            video:function(){
                var layout_instance = this;
                layout_instance.refreshData();
                layout_instance.screenLayout = layout_instance.video.layout;
            }
        },
        methods:{
            setLayout:function(layout){
                this.layout = layout;
            },

            refreshData:function(){
                var layout_instance = this;
                setTimeout(function(){
                    //向layout中设置数据
                    var srcs = layout_instance.video.srcs;
                    console.log(srcs);
                    if(srcs && srcs.length>0){
                        var $container = getLayoutContainer(layout_instance.$el);
                        var cells = $container['layout-auto']('getCells');
                        for(var i=0; i<cells.length; i++){
                            var cell = cells[i];
                            var d = {
                                pictureType:'STATIC',
                                pollingTime:null,
                                pollingStatus:null
                            };
                            for(var j=0; j<srcs.length; j++){
                                if(cell.serialNum == srcs[j].serialnum){
                                    var src = srcs[j];
                                    d.videoId = layout_instance.video.id;
                                    d.pictureType = src.pictureType;
                                    if(d.pictureType === 'POLLING') d.pollingTime = src.pollingTime;
                                    if(d.pictureType === 'POLLING') d.pollingStatus = src.pollingStatus;
                                    if(!d.src) d.src = [];
                                    if(src.type === 'CHANNEL'){
                                        d.src.push({
                                            id:src.memberChannelId,
                                            name:src.memberChannelName,
                                            //key:'CHANNEL@@' + srcs[j].memberChannelId,
                                            //type:'CHANNEL',
                                            //icon:'icon-exchange',
                                            param: $.toJSON({
                                                memberId:src.memberId,
                                                bundleId:src.bundleId,
                                                bundleName:src.bundleName,
                                                channelId:src.channelId,
                                                nodeUid:src.layerId,
                                                channelMemberId:src.memberChannelId,
                                                channelName:src.channelName
                                            })
                                        });
                                    }else if(src.type = 'ROLE'){
                                        d.src.push({
                                            id:src.roleChannelType,
                                            name:src.memberChannelName,
                                            param: $.toJSON({
                                                roleId:src.roleId,
                                                roleName:src.roleName,
                                                type:src.roleChannelType
                                            })
                                        });
                                    }
                                }
                            }
                            cell.$cell['layout-auto']('setData', d);
                            //开启轮询
                            setPolling(cell.$cell, d.pictureType==='STATIC'?false:true);
                            //设置轮询状态
                            if(d.pictureType === 'POLLING') setPollingStatus(cell.$cell, d.pollingStatus);
                            //设置轮询时间
                            if(d.pictureType === 'POLLING') setPollingTime(cell.$cell, d.pollingTime);
                            if(d.src && d.src.length > 0){
                                if(d.pictureType === 'POLLING'){
                                    cell.$cell.find('.screen-config-text').empty().append('<a class="bvc2-preview-layout-polling-button"><span class="el-icon-edit"></span>轮询（'+d.src.length+'）</a>');
                                }else if(d.pictureType === 'STATIC'){
                                    var param = $.parseJSON(d.src[0].param);
                                    if(param.roleId){
                                        cell.$cell.find('.screen-config-text').empty().append('<span style="color:#fff; font-size:14px;">'+param.roleName+ '（'+ d.src[0].name +'）</span>');
                                    }else{
                                        cell.$cell.find('.screen-config-text').empty().append('<span style="color:#fff; font-size:14px;">'+JSON.parse(d.src[0].param).bundleName+ '（'+ d.src[0].name+'）</span>');
                                    }
                                }
                            }
                        }
                    }
                }, 0);
            },

            refreshLayout:function(){
                var instance = this;
                var $container = getLayoutContainer(instance.$el);
                var options = instance.layout;
                if(options){
                    $container['layout-auto']('create', {
                        cell:{
                            column:options.column,
                            row:options.row,
                            html:tdHtml
                        },
                        cellspan:options.cellspan,
                        theme:'dark',
                        name:options.name,
                        editable:options.editable,
                        event:{
                            drop:function(e){
                                var data = $.parseJSON(e.dataTransfer.getData('data'));
                                console.log('播放对象：' + data);
                                var $cell = $(this);
                                //移除已有的播放器
                                $cell.find('.flowplayer').remove();
                                var param = $.parseJSON(data.param);
                                var previewPlayUrl = param.previewPlayUrl;
                                var playId = 'player' + '-' + $cell.parent()[0].rowIndex + '-'  + $cell[0].cellIndex;
                                $cell.append('<div id=' + playId + ' class="flowplayer" url="' + previewPlayUrl + '" style="width: 100%; height: 100%">');
                                setPlayer(playId, previewPlayUrl);
                                //填写名字
                                $cell.find('.el-checkbox__label')[0].innerText = data.name;
                                return;

                                var od = $cell['layout-auto']('getData');
                                if(!od){
                                    var _isPolling = isPolling($cell);
                                    od = {
                                        videoId:instance.video.id,
                                        pictureType:_isPolling?'POLLING':'STATIC',
                                        pollingTime:_isPolling?getPollingTime($cell):null,
                                        pollingStatus:_isPolling?'RUN':null,
                                        src:[]
                                    }
                                }
                                if(!od.src) od.src = [];
                                var param = $.parseJSON(data.param);
                                if(od.pictureType === 'POLLING'){
                                    //查重
                                    var finded = false;
                                    var channelMemberId = parseInt(param.channelMemberId);
                                    for(var i=0; i<od.src.length; i++){
                                        var scopeParam = $.parseJSON(od.src[i].param);
                                        if(channelMemberId === parseInt(scopeParam.channelMemberId)){
                                            finded = true;
                                            break;
                                        }
                                    }
                                    if(!finded){
                                        od.src.push(data);
                                        $cell.find('.screen-config-text').empty().append('<a class="bvc2-preview-layout-polling-button"><span class="el-icon-edit"></span>轮询（'+od.src.length+'）</a> ');
                                    }
                                }else if(od.pictureType === 'STATIC'){
                                    var outerName = '',
                                        innerName = '';
                                    if(param.roleId){
                                        outerName = param.roleName;
                                        innerName = data.name;
                                    }else{
                                        outerName = param.bundleName;
                                        innerName = data.name;
                                    }
                                    od.src = [data];
                                    $cell.find('.screen-config-text').empty().append('<span style="color:#fff; font-size:14px;">'+outerName+ '（'+ innerName +'）</span>');
                                }
                                $cell['layout-auto']('setData', od);
                            }
                        }
                    });
                }else{
                    $container['layout-auto']('destroy');
                }
            },

            columnChange:function(column){
                var layout_instance = this;
                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }
                //获取设置前的值
                var $container = getLayoutContainer(layout_instance.$el);
                var oldColumn = $container['layout-auto']('queryColumn');
                layout_instance.$confirm('此操作将清空屏幕上的配置，并且设置为：' + layout_instance.layout.row + '行' + column + '列, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    beforeClose:function(action, instance, done){
                        if(action === 'confirm'){
                            var newLayout = $.extend(true, {}, layout_instance.layout);
                            newLayout.column = column;
                            newLayout.cellspan = [];
                            layout_instance.layout = newLayout;
                        }else if(action === 'cancel'){
                            layout_instance.layout.column = oldColumn;
                        }
                        done();
                    }
                });
            },

            rowChange:function(row){
                var layout_instance = this;
                //if(!layout_instance.config){
                //    layout_instance.$message({
                //        type:'warning',
                //        message:'请先指定要操作的议程或方案！'
                //    });
                //    return;
                //}
                //if(!layout_instance.video){
                //    layout_instance.$message({
                //        type:'warning',
                //        message:'还没有要操作的视频！'
                //    });
                //    return;
                //}
                //获取设置前的值
                var $container = getLayoutContainer(layout_instance.$el);
                var oldRow = $container['layout-auto']('queryRow');
                if(row==oldRow){
                    return;
                }
                var newLayout = $.extend(true, {}, layout_instance.layout);
                newLayout.row = row;
                newLayout.column = row;
                newLayout.cellspan = [];
                layout_instance.layout = newLayout;


                //layout_instance.$confirm('此操作将清空屏幕上的配置，并且设置为：' + row + '行' + layout_instance.layout.column + '列, 是否继续?', '提示', {
                //    confirmButtonText: '确定',
                //    cancelButtonText: '取消',
                //    type: 'warning',
                //    beforeClose:function(action, instance, done){
                //        if(action === 'confirm'){
                //            var newLayout = $.extend(true, {}, layout_instance.layout);
                //            newLayout.row = row;
                //            newLayout.cellspan = [];
                //            layout_instance.layout = newLayout;
                //        }else{
                //            layout_instance.layout.row = oldRow;
                //        }
                //        done();
                //    }
                //});
            },

            play:function(){
                var layout_instance = this;
                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }
                if(layout_instance.style.play.isLoading) return;
                layout_instance.style.play.isLoading = true;
                ajax.post('/scheme/run/' + layout_instance.group.id + '/' + layout_instance.video.id, null, function(data, status){
                    layout_instance.style.play.isLoading = false;
                    if(status === 200){
                        layout_instance.$message({
                            type:'success',
                            message:'操作成功！'
                        });
                    }
                }, null, [403, 404, 408, 409]);
            },

            forward:function(){
                var layout_instance = this;
                var _video = layout_instance.video;
                var _values = layout_instance.values;
                var _roleValues = layout_instance.roleValues;

                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }

                ajax.get('/device/group/control/query/members/and/roles/' + layout_instance.group.id, null, function(data){

                    var members = data.members,
                        videoDecodeMembers = layout_instance.members;
                    videoDecodeMembers.splice(0, videoDecodeMembers.length);

                    for(var i=0;i<members.length;i++){
                        if(members[i].channelType == 'VenusVideoOut'){
                            videoDecodeMembers.push(members[i]);
                        }
                    }

                    //处理角色信息（过滤没绑定设备的角色）
                    var roles = data.roles,
                        roleMembers = layout_instance.roles;
                    roleMembers.splice(0, roleMembers.length);

                    for(var k=0;k<roles.length;k++) {
                        if(roles[k].channels.length >0){
                            for(var m=0;m<roles[k].channels.length;m++){
                                var roleMember = {
                                    id:roles[k].id + '-' + roles[k].channels[m].channelType,
                                    name:roles[k].name + '-' + roles[k].channels[m].name,
                                    roleChannelType:roles[k].channels[m].channelType,
                                    roleId:roles[k].id,
                                    roleName:roles[k].name,
                                    special:roles[k].special,
                                    type:roles[k].type
                                };
                                roleMembers.push(roleMember);
                                //设备列表添加可录制角色
                                if(roles[k].type == 'RECORDABLE'){
                                    videoDecodeMembers.push(roleMember);
                                }
                            }
                        }
                    }


                    if(_values) _values.splice(0, _values.length);
                    if(_roleValues) _roleValues.splice(0, _roleValues.length);

                    if(_video.dstsCache && _video.dstsCache.length > 0){
                        for(var i=0;i<_video.dstsCache.length;i++){
                            if(_video.dstsCache[i].channelId){
                                //已选设备value
                                _values.push(_video.dstsCache[i].id);
                            }else if(_video.dstsCache[i].roleId){
                                //已选角色value
                                _roleValues.push(_video.dstsCache[i].roleId + '-' + _video.dstsCache[i].roleChannelType);
                                if(_video.dstsCache[i].roleType == 'RECORDABLE'){
                                    _values.push(_video.dstsCache[i].roleId + '-' + _video.dstsCache[i].roleChannelType);
                                }
                            }
                        }
                    }else{
                        if(_video.dsts){
                            for(var i=0;i<_video.dsts.length;i++){
                                if(_video.dsts[i].channelId){
                                    //已选设备value
                                    _values.push(_video.dsts[i].id);
                                }else if(_video.dsts[i].roleId){
                                    //已选角色value
                                    _roleValues.push(_video.dsts[i].roleId + '-' + _video.dsts[i].roleChannelType);
                                    if(_video.dsts[i].roleType == 'RECORDABLE'){
                                        _values.push(_video.dsts[i].roleId + '-' + _video.dsts[i].roleChannelType);
                                    }
                                }
                            }
                        }
                    }

                    layout_instance.$refs.setForward.dialogVisible = true;
                });
            },

            startRecord:function(){
                var layout_instance = this;
                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }
                if(layout_instance.style.startRecord.isLoading) return;
                layout_instance.style.startRecord.isLoading = true;
                ajax.post('/scheme/start/record/' + layout_instance.group.id + '/' + layout_instance.video.id, null, function(data, status){
                    layout_instance.style.startRecord.isLoading = false;
                    if(status === 200){
                        layout_instance.$message({
                            type:'success',
                            message:'操作成功！'
                        });
                        layout_instance.video.record = true;
                    }
                }, null, [403, 404, 408, 409]);
            },

            stopRecord:function(){
                var layout_instance = this;
                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }
                if(layout_instance.style.stopRecord.isLoading) return;
                layout_instance.style.stopRecord.isLoading = true;
                ajax.post('/scheme/stop/record/' + layout_instance.group.id + '/' + layout_instance.video.id, null, function(data, status){
                    layout_instance.style.stopRecord.isLoading = false;
                    if(status === 200){
                        layout_instance.$message({
                            type:'success',
                            message:'操作成功！'
                        });
                        layout_instance.video.record = false;
                    }
                }, null, [403, 404, 408, 409]);
            },

            reset:function(){
                var layout_instance = this;
                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }
                if(layout_instance.style.reset.isLoading) return;
                layout_instance.style.reset.isLoading = true;
                var websiteDraw = $.parseJSON(layout_instance.video.websiteDraw);

                //重置布局
                layout_instance.setLayout({
                    column:websiteDraw.basic.column,
                    row:websiteDraw.basic.row,
                    cellspan:websiteDraw.cellspan,
                    editable:true
                });
                //恢复数据
                layout_instance.refreshData();

                layout_instance.style.reset.isLoading = false;
                layout_instance.$message({
                    type:'success',
                    message:'操作成功！'
                });

            },

            save:function(){
                var layout_instance = this;
                if(!layout_instance.config){
                    layout_instance.$message({
                        type:'warning',
                        message:'请先指定要操作的议程或方案！'
                    });
                    return;
                }
                if(!layout_instance.video){
                    layout_instance.$message({
                        type:'warning',
                        message:'还没有要操作的视频！'
                    });
                    return;
                }
                if(layout_instance.style.saveScheme.isLoading) return;
                layout_instance.style.saveScheme.isLoading = true;

                var done = function(){
                    layout_instance.style.saveScheme.isLoading = false;
                    layout_instance.$message({
                        type:'success',
                        message:'操作成功！'
                    });
                };

                //layout数据
                var $container = getLayoutContainer(layout_instance.$el);
                var tpl = $container['layout-auto']('generateTpl', false);
                var websiteDraw = {
                    basic:tpl.basic,
                    cellspan:tpl.cellspan
                };
                var position = tpl.layout;
                //var dst = layout_instance.$refs.setForward.dst;
                //var roleDst = layout_instance.$refs.setForward.roleDst;

                var dst = [], roleDst = [];
                if(layout_instance.video.dstsCache){
                    for(var i=0;i<layout_instance.video.dstsCache.length;i++){
                        if(layout_instance.video.dstsCache[i].channelId){
                            //设备
                            dst.push(layout_instance.video.dstsCache[i]);
                        }else if(layout_instance.video.dstsCache[i].roleId){
                            //角色
                            roleDst.push(layout_instance.video.dstsCache[i]);
                        }
                    }
                }else{
                    for(var i=0;i<layout_instance.video.dsts.length;i++){
                        if(layout_instance.video.dsts[i].channelId){
                            //设备
                            dst.push(layout_instance.video.dsts[i]);
                        }else if(layout_instance.video.dsts[i].roleId){
                            //角色
                            roleDst.push(layout_instance.video.dsts[i]);
                        }
                    }
                }

                //发射事件
                layout_instance.$emit('save-layout', layout_instance.config, layout_instance.video, websiteDraw, position, dst, roleDst, done, layout_instance.screenLayout);
            }

        },
        mounted:function(){
            this.refreshLayout();
        }
    });

    //小部件事件--复选框点击（由于事件冲突只能这么模拟）
    $(document).on('mousedown.polling.switch', '.screen-config-polling-switch .el-checkbox', function(e){
        e.stopPropagation();
        var $target = $(this);
        $target.on('mouseup', function(){
            var $checkbox = $(this);
            var $td = $checkbox.closest('td');
            var $pollingTime = $td.find('.screen-config-polling-time');
            var $pollingStatus = $td.find('.screen-config-polling-status-switch');
            var $el_checkbox__input = $checkbox.find('.el-checkbox__input');
            var od = $td['layout-auto']('getData');
            if(!od){
                od = {
                    pictureType:'STATIC',
                    pollingTime:null,
                    pollingStatus:null,
                    src:[]
                }
            }
            if($checkbox.is('.is-checked')){
                $checkbox.removeClass('is-checked');
                $el_checkbox__input.removeClass('is-checked');
                $pollingTime.hide();
                $pollingStatus.hide();

                //这个地方需要修改状态
                od.pictureType = 'STATIC';
                //od.pollingTime = null;
                //od.pollingStatus = null;
                if(od.src && od.src.length>0){
                    //只取第一个
                    od.src = [od.src[0]];
                    $td.find('.screen-config-text').empty().append('<span style="color:#fff; font-size:14px;">'+$.parseJSON(od.src[0].param).bundleName+ '（'+ od.src[0].name+'）</span>');
                }else{
                    $td.find('.screen-config-text').empty();
                }
                $td['layout-auto']('setData', od);
            }else{
                $checkbox.addClass('is-checked');
                $el_checkbox__input.addClass('is-checked');
                $pollingTime.show();
                $pollingStatus.show();

                //这个地方需要修改状态
                od.pictureType = 'POLLING';
                if(!od.pollingTime) od.pollingTime = 5;
                if(!od.pollingStatus) od.pollingStatus = 'RUN';
                $td.find('.screen-config-text').empty().append('<a class="bvc2-preview-layout-polling-button"><span class="el-icon-edit"></span>轮询（'+(od.src?od.src.length:0)+'）</a> ');
                $td['layout-auto']('setData', od);
            }
        });
        $(document).on('mouseup.polling.switch', function(e){
            $target.unbind('mouseup');
            $(document).unbind('mouseup.polling.switch');
        });
    });

    //小部件事件--时间间隔输入
    $(document).on('change.polling.time', '.screen-config-polling-time .el-input__inner', function(e){
        var $input = $(this);
        var $td = $input.closest('td');
        var od = $td['layout-auto']('getData');
        var value = parseInt($input.val());
        if(isNaN(value)){
            value = '5秒';
        }else{
            value = value + '秒';
        }
        $input.val(value);
        od.pollingTime = parseInt(value);
    });

    //小部件事件--清空按钮
    $(document).on('mousedown.config.empty', '.screen-config-empty', function(e){
        e.stopPropagation();
        var $button = $(this);
        $button.on('mouseup', function(){
            var $td = $button.closest('td');
            $td.find('.el-checkbox__label')[0].innerText = "";
            $td.find('.flowplayer').remove();
            return;



            var $td = $button.closest('td');
            var od = $td['layout-auto']('getData');
            od.src = [];
            $td['layout-auto']('setData', od);
            if(od.pictureType === 'POLLING'){
                $td.find('.screen-config-text').empty().append('<a class="bvc2-preview-layout-polling-button"><span class="el-icon-edit"></span>轮询（0）</a> ');
            }else if(od.pictureType === 'STATIC'){
                $td.find('.screen-config-text').empty();
            }
        });
        $(document).on('mouseup.config.empty', function(){
            $button.unbind('mouseup');
            $(document).unbind('mouseup.config.empty');
        });
    });

    //小部件事件--轮询列表
    $(document).on('mousedown.polling.list', '.bvc2-preview-layout-polling-button', function(e){
        e.stopPropagation();
        var $button = $(this);
        $button.on('mouseup', function(){
            var videoEncodeMembers = context.getProp('videoEncodeMembers');
            var d = [];
            if(videoEncodeMembers && videoEncodeMembers.length>0){
                for(var i=0; i<videoEncodeMembers.length; i++){
                    d.push($.extend(true, {key:videoEncodeMembers[i].id}, videoEncodeMembers[i]));
                }
            }

            var $td = $button.closest('td');
            var od = $td['layout-auto']('getData');
            var selected = [];
            if(od.src){
                for(var i=0; i<od.src.length; i++){
                    selected.push(parseInt($.parseJSON(od.src[i].param).channelMemberId));
                }
            }

            var $body = $('body');
            $body.append(dialog);
            var dialogVue = new Vue({
                el:'#bvc2-preview-layout-polling-list-dialog-wrapper',
                data:function(){
                    return {
                        dialogVisible:true,
                        data:d,
                        selected:selected
                    }
                },
                methods:{
                    handleClose:function(){
                        var instance = this;
                        instance.$destroy();
                        $('#bvc2-preview-layout-polling-list-dialog-wrapper').remove();
                    },
                    handleOkClick:function(){
                        var instance = this;
                        od.src = [];
                        if(instance.selected.length > 0){
                            for(var i=0; i<instance.selected.length; i++){
                                var targetChannel = null;
                                for(var j=0; j<instance.data.length; j++){
                                    if(instance.data[j].id == instance.selected[i]){
                                        targetChannel = instance.data[j];
                                        break;
                                    }
                                }
                                var src = {
                                    id:targetChannel.channelId,
                                    key:'CHANNEL@@' + targetChannel.channelId,
                                    name:targetChannel.channelName,
                                    type:'CHANNEL'
                                };
                                var param = {
                                    bundleId:targetChannel.bundleId,
                                    channelMemberId:targetChannel.id,
                                    bundleName:targetChannel.bundleName,
                                    nodeUid:targetChannel.layerId,
                                    channelName:targetChannel.channelName,
                                    channelType:targetChannel.channelType,
                                    channelId:targetChannel.channelId,
                                    memberId:targetChannel.memberId
                                };
                                src.param = $.toJSON(param);
                                od.src.push(src);
                            }
                        }
                        $td['layout-auto']('setData', od);
                        $td.find('.screen-config-text').empty().append('<a class="bvc2-preview-layout-polling-button"><span class="el-icon-edit"></span>轮询（'+od.src.length+'）</a>');
                        instance.dialogVisible = false;
                        instance.handleClose();
                    },
                    handleCancelClick:function(){
                        var instance = this;
                        instance.dialogVisible = false;
                        instance.handleClose();
                    },
                    renderFunc:function(h, option) {
                        return h('span', null, option.bundleName +' - '+ option.name);
                    },
                    filterMethod:function(query, item) {
                        var str = item.bundleName +' - '+ item.name;

                        return str.indexOf(query) > -1;
                    }
                }
            });
        });
        $(document).on('mouseup.polling.list', function(){
            $button.unbind('mouseup');
            $(document).unbind('mouseup.polling.list');
        });
    });

    //小部件事件--轮询状态
    $(document).on('mousedown.polling.status', '.screen-config-polling-status-switch', function(e){
        e.stopPropagation();
        var $button = $(this);
        var $td = $button.closest('td');
        var od = $td['layout-auto']('getData');
        $button.on('mouseup', function(){
            var pollingStatus = null;
            if($button.is('.is-checked')){
                pollingStatus = 'PAUSE';
            }else{
                pollingStatus = 'RUN';
            }
            var serialNum = $td['layout-auto']('getSerialNum');
            ajax.post('/config/set/polling/status/' + od.videoId, {
                serialNum:serialNum,
                pollingStatus:pollingStatus
            }, function(){
                if($button.is('.is-checked')){
                    $button.removeClass('is-checked');
                    $button.find('.el-switch__label--right').removeClass('is-active');
                    $button.find('.el-switch__label--left').addClass('is-active');
                    od.pollingStatus = 'PAUSE';
                }else{
                    $button.addClass('is-checked');
                    $button.find('.el-switch__label--left').removeClass('is-active');
                    $button.find('.el-switch__label--right').addClass('is-active');
                    od.pollingStatus = 'RUN';
                }
                $td['layout-auto']('setData', od);
            });
        });
        $(document).on('mouseup.polling.status', function(){
            $button.unbind('mouseup');
            $(document).unbind('mouseup.polling.status');
        });
    });

});