<template>
    <el-dialog
            title="选择接入层"
            :visible.sync="visible"
            width="800px"
            :before-close="handleClose">
        <div style="height:500px; position:relative;">
            <div style="height:50px; width:100%;">
                <el-select v-model="filters.type" placeholder="接入层类型" style="margin-right:10px;">
                    <el-option value="">全部类型</el-option>
                    <el-option v-for="layerNodeType in layerNodeTypes" :label="layerNodeType.label" :value="layerNodeType.value"></el-option>
                </el-select>
                <el-input v-model="filters.name" placeholder="接入层名称" style="width:auto; margin-right:10px;"></el-input>
                <el-input v-model="filters.ip" placeholder="接入成ip" style="width:auto; margin-right:10px;"></el-input>
                <el-button @click="handleFilter">查询</el-button>
            </div>
            <div style="position:absolute; left:0; top:50px; right:0; bottom:0;">
                <el-scrollbar style="height:100%;">
                    <el-table
                            :data="table.filterData"
                            highlight-current-row
                            @current-change="currentRowChange"
                            style="width: 100%">
                        <el-table-column
                                prop="name"
                                label="名称">
                        </el-table-column>
                        <el-table-column
                                prop="ip"
                                label="ip">
                        </el-table-column>
                        <el-table-column
                                prop="typeName"
                                label="类型">
                        </el-table-column>
                    </el-table>
                </el-scrollbar>
            </div>
        </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="handleCommit">确定</el-button>
        </span>
    </el-dialog>
</template>
<script type="text/ecmascript-6">

import{
    getNodes
}from '../../api/api'

export default{
    name: 'selectLayerNode',
    data: function(){
        return {
            visible:false,
            table:{
                totalData:[],
                filterData:[],
                currentRow:''
            },
            layerNodeTypes:[{
                value:'ACCESS_JV230',
                label:'JV230接入'
            },{
                value:'ACCESS_TVOS',
                label:'机顶盒接入'
            },{
                value:'ACCESS_MIXER',
                label:'混合器接入'
            },{
                value:'ACCESS_MOBILE',
                label:'手机终端接入'
            },{
                value:'ACCESS_JV210',
                label:'音视频转发服务设备'
            },{
                value:'ACCESS_CDN',
                label:'录像存储服务单元A型'
            },{
                value:'ACCESS_VOD',
                label:'录像存储服务单元B型'
            },{
                value:'ACCESS_IPC',
                label:'监控资源汇接网关'
            },{
                value:'ACCESS_STREAMMEDIA',
                label:'流媒体管理服务设备'
            },{
                value:'ACCESS_NETWORK',
                label:'联网服务设备'
            },{
                value:'ACCESS_DISPLAYCTRL',
                label:'显控汇接网关'
            },{
                value:'ACCESS_S100',
                label:'流转发器接入'
            },{
                value:'ACCESS_VODPROXY',
                label:'点播代理服务设备'
            },{
                value:'ACCESS_LIANWANG',
                label:'联网接入'
            },{
                label : "LDAP目录资源服务设备",
                value : "ACCESS_LDAP"
            },{
                label : "综合运维服务设备",
                value : "ACCESS_OMMS"
            },{
              label : "ws接入",
              value : "ACCESS_WS"
            }],
            filters:{
                type:'',
                name:'',
                ip:''
            }
        }
    },
    methods:{
        show:function(){
            var self = this;
            self.visible = true;
            getNodes({
                type:'',
                keyword:''
            }).then(res => {
                self.table.totalData.splice(0, self.table.totalData.length);
                self.table.filterData.splice(0, self.table.filterData.length);
                if(!res.errMsg){
                    if(res.nodes && res.nodes.length>0){
                        for(var i=0; i<res.nodes.length; i++){
                            for(var j=0; j<self.layerNodeTypes.length; j++){
                                if(self.layerNodeTypes[j].value === res.nodes[i].type){
                                    res.nodes[i].typeName = self.layerNodeTypes[j].label;
                                    break;
                                }
                            }
                            self.table.totalData.push(res.nodes[i]);
                            self.table.filterData.push(res.nodes[i]);
                        }
                    }
                }else{
                    self.$message({
                        type:'error',
                        message:res.errMsg
                    });
                }
            });
        },
        handleClose:function(){
            var self = this;
            self.visible = false;
            self.table.totalData.splice(0, self.table.totalData.length);
            self.table.filterData.splice(0, self.table.filterData.length);
            self.table.currentRow = '';
            self.filters.type = '';
            self.filters.name = '';
            self.filters.ip = '';
        },
        handleCommit:function(){
            var self = this;
            self.$emit('layer-node-selected', self.table.currentRow, function(){
                self.handleClose();
            });
        },
        handleFilter:function(){
            var self = this;
            self.table.filterData.splice(0, self.table.filterData.length);
            for(var i=0; i<self.table.totalData.length; i++){
                var filtered = true;
                var row = self.table.totalData[i];
                if(self.filters.type && row.type!==self.filters.type){
                    filtered = false;
                }
                if(filtered && self.filters.name && row.name.toLowerCase().indexOf(self.filters.name.toLowerCase())<0){
                    filtered = false;
                }
                if(filtered && self.filters.ip && row.ip.indexOf(self.filters.ip)<0){
                    filtered = false;
                }
                if(filtered){
                    self.table.filterData.push(row);
                }
            }
        },
        currentRowChange:function(currentRow, oldRow){
            var self = this;
            self.table.currentRow = currentRow;
        }
    },
    mounted: function(){

    }
}

</script>
<style>

</style>
