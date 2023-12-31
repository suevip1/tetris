/**
 * Created by lvdeyang on 2018/12/24 0024.
 */
define([
    'text!' + window.APPPATH + 'cms/article/page-cms-article.html',
    'config',
    'jquery',
    'restfull',
    'context',
    'commons',
    'vue',
    'element-ui',
    'mi-frame',
    'layout-editor',
    'date',
    'region-dialog',
    'classify-dialog',
    'column-dialog',
    'css!' + window.APPPATH + 'cms/article/page-cms-article.css'
], function(tpl, config, $, ajax, context, commons, Vue){

    var pageId = 'page-cms-article';

    var init = function(){

        //设置标题
        commons.setTitle(pageId);

        var $page = document.getElementById(pageId);
        $page.innerHTML = tpl;

        //查询文章类型、地区、分类
        var articleType = [];
        var articleRegion = [];
        var articleClassify = [];
        ajax.post('/cms/article/query/type', null, function(data) {
            var types = data.type;
            var regions = data.region;
            var classies = data.classify;
            if (types && types.length > 0) {
                for (var j = 0; j < types.length; j++) {
                    var type = {
                        value: types[j],
                        label: types[j]
                    };
                    articleType.push(type);
                }
            }
            if(regions && regions.length > 0){
                for(var i = 0; i<regions.length; i++){
                    articleRegion.push(regions[i]);
                }
            }
            if(classies && classies.length > 0){
                for(var k = 0; k<classies.length; k++){
                    var classify = {
                        value: classies[k],
                        label: classies[k]
                    };
                    articleClassify.push(classify);
                }
            }
        });

        new Vue({
            el: '#' + pageId + '-wrapper',
            data: {
                menus: context.getProp('menus'),
                user: context.getProp('user'),
                groups: context.getProp('groups'),
                table:{
                    data:[],
                    page:{
                        currentPage:1,
                        sizes:[20, 50, 100, 200, 500],
                        size:20,
                        total:0
                    }
                },
                popover:{
                    visible: false
                },
                param:{
                    region:articleRegion,
                    classify:articleClassify
                },
                search:{
                    name:'',
                    author:'',
                    type:'',
                    region:'',
                    classify:'',
                    time:''
                },
                dialog:{
                    type:articleType,
                    addArticle:{
                        visible:false,
                        name:'',
                        author:'',
                        publishTime:new Date().format('yyyy-MM-DD HH:mm:ss'),
                        thumbnail:'',
                        classify:[],
                        region:[],
                        remark:'',
                        loading:false,
                        value:'',
                        timeOption:{
                            shortcuts: [{
                                text: '今天',
                                onClick:function(picker) {
                                    picker.$emit('pick', new Date());
                                }
                            }, {
                                text: '昨天',
                                onClick:function(picker) {
                                    var date = new Date();
                                    date.setTime(date.getTime() - 3600 * 1000 * 24);
                                    picker.$emit('pick', date);
                                }
                            }, {
                                text: '一周前',
                                onClick:function(picker) {
                                    const date = new Date();
                                    date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
                                    picker.$emit('pick', date);
                                }
                            }]
                        }
                    },
                    editArticle:{
                        visible:false,
                        id:'',
                        name:'',
                        author:'',
                        publishTime:'',
                        thumbnail:'',
                        classify:[],
                        region:[],
                        remark:'',
                        loading:false,
                        value:'',
                        timeOption:{
                            shortcuts: [{
                                text: '今天',
                                onClick:function(picker) {
                                    picker.$emit('pick', new Date());
                                }
                            }, {
                                text: '昨天',
                                onClick:function(picker) {
                                    var date = new Date();
                                    date.setTime(date.getTime() - 3600 * 1000 * 24);
                                    picker.$emit('pick', date);
                                }
                            }, {
                                text: '一周前',
                                onClick:function(picker) {
                                    const date = new Date();
                                    date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
                                    picker.$emit('pick', date);
                                }
                            }]
                        }
                    },
                    bindColumn:{
                        visible:false,
                        id:'',
                        name:'',
                        loading:false,
                        column:[]
                    }
                }
            },
            computed:{

            },
            watch:{

            },
            methods:{
                handleSelectionChange:function(){
                    var self = this;
                },
                doPreview:function(scope){
                    var self = this;
                    var row = scope.row;
                    window.open(window.BASEPATH + row.previewUrl, row.name, "status=no,menubar=yes,toolbar=no,width=414,height=738,left:100,top:100");
                },
                rowEdit:function(scope){
                    var self = this;
                    var row = scope.row;
                    self.dialog.editArticle.id = row.id;
                    self.dialog.editArticle.name = row.name;
                    self.dialog.editArticle.author = row.author;
                    self.dialog.editArticle.publishTime = row.publishTime;
                    self.dialog.editArticle.thumbnail = row.thumbnail;
                    self.dialog.editArticle.remark = row.remark;
                    self.dialog.editArticle.visible = true;

                    self.dialog.editArticle.region.splice(0, self.dialog.editArticle.region.length);
                    self.dialog.editArticle.classify.splice(0, self.dialog.editArticle.classify.length);

                    for(var i=0;i<row.regions.length;i++){
                        self.dialog.editArticle.region.push(row.regions[i]);
                    }
                    for(var j=0;j<row.classifies.length;j++){
                        self.dialog.editArticle.classify.push(row.classifies[j]);
                    }

                    self.dialog.editArticle.value = row.articleType;
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
                                h('p', null, ['此操作将永久删除文章，且不可恢复，是否继续?'])
                            ])
                        ]),
                        type:'wraning',
                        showCancelButton: true,
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        beforeClose:function(action, instance, done){
                            instance.confirmButtonLoading = true;
                            if(action === 'confirm'){
                                ajax.post('/cms/article/remove/' + row.id, null, function(data, status){
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
                                }, null, ajax.NO_ERROR_CATCH_CODE);
                            }else{
                                instance.confirmButtonLoading = false;
                                done();
                            }
                        }
                    }).catch(function(){});
                },
                handleSizeChange:function(val){
                    var self = this;
                    self.table.page.size = parseInt(val);
                    self.onSubmit(self.table.page.currentPage);
                },
                handleCurrentChange:function(val){
                    var self = this;
                    self.table.page.currentPage = parseInt(val);
                    self.onSubmit(self.table.page.currentPage);
                },
                loadArticle :function(currentPage){
                    var self = this;
                    self.table.data.splice(0, self.table.data.length);
                    ajax.post('/cms/article/list', {
                        currentPage:currentPage,
                        pageSize:self.table.page.size,
                    }, function(data){
                        var rows = data.rows;
                        var total = data.total;
                        if(rows && rows.length>0){
                            for(var i=0; i<rows.length; i++){
                                self.table.data.push(rows[i]);
                            }
                        }
                        self.table.page.total = total;
                        self.table.page.currentPage = currentPage;
                    });
                },
                handleSelectThumbnail:function(buff){
                    var self = this;
                    self.$refs.selectThumbnail.setBuffer(buff);
                    self.$refs.selectThumbnail.open();
                },
                selectedThumbnail:function(url, buff, startLoading, endLoading, done){
                    buff.thumbnail = url;
                    done();
                },
                handleClassifyRemove:function(classify, value){
                    for(var i=0; i<classify.length; i++){
                        if(classify[i] === value){
                            classify.splice(i, 1);
                            break;
                        }
                    }
                },
                handleClassifyAdd:function(){
                    var self = this;
                    var checkedClassifies = [];
                    var classify = self.dialog.addArticle.classify;
                    if(classify.length > 0){
                        for(var i=0;i<classify.length;i++){
                            checkedClassifies.push(classify[i].id);
                        }
                    }
                    self.$refs.classifyDialog.open('/cms/classify/list/with/except', checkedClassifies, classify);
                },
                handleRegionAdd:function(){
                    var self = this;
                    var checkedRegions = [];
                    var region = self.dialog.addArticle.region;
                    if(region.length > 0){
                        for(var i=0;i<region.length;i++){
                            checkedRegions.push(region[i].id);
                        }
                    }
                    self.$refs.regionDialog.open('/cms/region/list/tree', checkedRegions, region);
                },
                handleClassifyEdit:function(){
                    var self = this;
                    var checkedClassifies = [];
                    var classify = self.dialog.editArticle.classify;
                    if(classify.length > 0){
                        for(var i=0;i<classify.length;i++){
                            checkedClassifies.push(classify[i].id);
                        }
                    }
                    self.$refs.classifyDialog.open('/cms/classify/list/with/except', checkedClassifies, classify);
                },
                handleRegionEdit:function(){
                    var self = this;
                    var checkedRegions = [];
                    var region = self.dialog.editArticle.region;
                    if(region.length > 0){
                        for(var i=0;i<region.length;i++){
                            checkedRegions.push(region[i].id);
                        }
                    }
                    self.$refs.regionDialog.open('/cms/region/list/tree', checkedRegions, region);
                },
                handleAddArticleClose:function(){
                    var self = this;
                    self.dialog.addArticle.name = '';
                    self.dialog.addArticle.author = '';
                    self.dialog.addArticle.publishTime = new Date().format('yyyy-MM-DD HH:mm:ss');
                    self.dialog.addArticle.thumbnail = '';
                    self.dialog.addArticle.remark = '';
                    self.dialog.addArticle.classify = [];
                    self.dialog.addArticle.region = [];
                    self.dialog.addArticle.visible = false;
                    self.dialog.addArticle.value = '';
                },
                handleAddArticleCommit:function(){
                    var self = this;
                    self.dialog.addArticle.loading = true;
                    ajax.post('/cms/article/add', {
                        name:self.dialog.addArticle.name,
                        author:self.dialog.addArticle.author,
                        publishTime:self.dialog.addArticle.publishTime,
                        thumbnail:self.dialog.addArticle.thumbnail,
                        remark: self.dialog.addArticle.remark,
                        classify: $.toJSON(self.dialog.addArticle.classify),
                        region: $.toJSON(self.dialog.addArticle.region),
                        type: self.dialog.addArticle.value
                    }, function(data, status){
                        self.dialog.addArticle.loading = false;
                        if(status !== 200) return;
                        self.table.data.push(data);
                        self.table.page.total += 1;
                        self.handleAddArticleClose();
                    }, null, ajax.NO_ERROR_CATCH_CODE);
                },
                handleEditArticleClose:function(){
                    var self = this;
                    self.dialog.editArticle.id = '';
                    self.dialog.editArticle.name = '';
                    self.dialog.editArticle.author = '';
                    self.dialog.editArticle.publishTime = '';
                    self.dialog.editArticle.thumbnail = '';
                    self.dialog.editArticle.remark = '';
                    self.dialog.editArticle.region = [];
                    self.dialog.editArticle.classify = [];
                    self.dialog.editArticle.visible = false;
                    self.dialog.editArticle.value = '';
                },
                handleEditArticleCommit:function(){
                    var self = this;
                    self.dialog.editArticle.loading = true;
                    ajax.post('/cms/article/edit/' + self.dialog.editArticle.id, {
                        name:self.dialog.editArticle.name,
                        author:self.dialog.editArticle.author,
                        publishTime:self.dialog.editArticle.publishTime,
                        thumbnail:self.dialog.editArticle.thumbnail,
                        remark:self.dialog.editArticle.remark,
                        classify: $.toJSON(self.dialog.editArticle.classify),
                        region: $.toJSON(self.dialog.editArticle.region),
                        type: self.dialog.editArticle.value
                    }, function(data, status){
                        self.dialog.editArticle.loading = false;
                        if(status !== 200) return;
                        for(var i=0; i<self.table.data.length; i++){
                            if(self.table.data[i].id === data.id){
                                self.table.data.splice(i, 1, data);
                                break;
                            }
                        }
                        self.handleEditArticleClose();
                    }, null, ajax.NO_ERROR_CATCH_CODE)
                },
                editLayout:function(scope){
                    var self = this;
                    var row = scope.row;
                    self.$refs.articleLayoutEditor.show(row);
                },
                saveArticle:function(article, html, modules, endLoading){
                    var self = this;
                    ajax.post('/cms/article/save/' + article.id, {
                        html:html,
                        modules:modules
                    }, function(data, status){
                        endLoading();
                        if(status !== 200) return;
                        self.$message({
                            type:'success',
                            message:'保存成功'
                        })
                    }, null, ajax.NO_ERROR_CATCH_CODE);
                },
                selectedClassifies:function(classifies, buff, startLoading, endLoading, close){
                    var self = this;
                    var classifyIds = [];
                    startLoading();
                    for(var i=0; i<classifies.length; i++){
                        classifyIds.push(classifies[i].id);
                        buff.push(classifies[i]);
                    }
                    endLoading();
                    close();
                },
                selectedRegions:function(regions, buff, startLoading, endLoading, close){
                    var self = this;
                    var regionIds = [];
                    startLoading();
                    buff.splice(0,buff.length);
                    for(var i=0; i<regions.length; i++){
                        regionIds.push(regions[i].id);
                        buff.push(regions[i]);
                    }
                    endLoading();
                    close();
                },
                rowBindColumn:function(scope){
                    var self = this;
                    var row = scope.row;
                    self.dialog.bindColumn.id = row.id;
                    self.dialog.bindColumn.name = row.name;
                    self.dialog.bindColumn.visible = true;

                    self.dialog.bindColumn.column.splice(0, self.dialog.bindColumn.column.length);
                    ajax.post('/cms/column/relation/article/load/column', {articleId: row.id}, function(data, status){
                        if(status !== 200) return;
                        for(var i=0; i<data.length; i++){
                            self.dialog.bindColumn.column.push(data[i]);
                        }
                    });

                },
                handleBindColumnClose:function(){
                    var self = this;
                    self.dialog.bindColumn.id = '';
                    self.dialog.bindColumn.name = '';
                    self.dialog.bindColumn.column = [];
                    self.dialog.bindColumn.visible = false;
                },
                handleColumnEdit: function(){
                    var self = this;
                    var checkedColumns = [];
                    var column = self.dialog.bindColumn.column;
                    if(column.length > 0){
                        for(var i=0;i<column.length;i++){
                            checkedColumns.push(column[i].id);
                        }
                    }
                    self.$refs.columnDialog.open('/cms/column/list/tree', checkedColumns, column);
                },
                handleColumnRemove: function(column, value){
                    for(var i=0; i<column.length; i++){
                        if(column[i] === value){
                            column.splice(i, 1);
                            break;
                        }
                    }
                },
                handleBindColumnCommit: function(){
                    var self = this;
                    var columnIds = [];
                    var columns = self.dialog.bindColumn.column;
                    for(var i=0; i<columns.length; i++){
                        columnIds.push(columns[i].id);
                    }
                    self.dialog.bindColumn.loading = true;
                    ajax.post('/cms/article/bind/column' , {
                        articleId: self.dialog.bindColumn.id,
                        columnIds: $.toJSON(columnIds)
                    }, function(data, status){
                        self.dialog.bindColumn.loading = false;
                        if(status !== 200) return;
                        self.handleBindColumnClose();
                    }, null, ajax.NO_ERROR_CATCH_CODE)
                },
                selectedColumns:function(columns, buff, startLoading, endLoading, close){
                    var self = this;
                    var columnIds = [];
                    startLoading();
                    buff.splice(0,buff.length);
                    for(var i=0; i<columns.length; i++){
                        columnIds.push(columns[i].id);
                        buff.push(columns[i]);
                    }
                    endLoading();
                    close();
                },
                onSubmit:function(currentPage){
                    var self = this;
                    self.table.page.currentPage = currentPage;
                    self.table.data.splice(0, self.table.data.length);
                    ajax.post('/cms/article/search', {
                        name:self.search.name,
                        author:self.search.author,
                        region:self.search.region,
                        classify:self.search.classify,
                        beginTime:(self.search.time == null || self.search.time == "")?"":self.search.time[0],
                        endTime:(self.search.time == null || self.search.time == "") ?"":self.search.time[1],
                        currentPage:self.table.page.currentPage,
                        pageSize:self.table.page.size
                    }, function(data){
                        var rows = data.rows;
                        var total = data.total;
                        if(rows && rows.length>0){
                            for(var i=0; i<rows.length; i++){
                                self.table.data.push(rows[i]);
                            }
                        }
                        self.table.page.total = total;
                    });
                },
                onClear:function(){
                    var self = this;
                    self.search.name = "";
                    self.search.author = "";
                    self.search.region = "";
                    self.search.classify = "";
                    self.search.time = null;
                    self.onSubmit(1);
                },
                clickTag:function(value){
                    var self = this;
                    self.popover.visible = false;
                    self.search.region = value;
                }
            },
            created:function(){
                var self = this;
            },
            mounted:function(){
                var self = this;
                self.loadArticle(1);
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