/**
 * Created by Administrator on 2019/2/25 0025.
 */
define([
    'text!' + window.APPPATH + 'cms/article/layout-editor.html',
    'restfull',
    'jquery',
    'juicer',
    'vue',
    'element-ui',
    'mi-image-dialog',
    'mi-txt-dialog',
    'mi-video-dialog',
    'mi-audio-dialog',
    'css!' + window.APPPATH + 'cms/article/layout-editor.css'
], function(tpl, ajax, $, juicer, Vue){

    var pluginName = 'article-layout-editor';

    Vue.component(pluginName, {
        props:['id'],
        template: tpl,
        data:function(){
            return {
                visible:false,
                article:'',
                templates:[],
                modules:[],
                loading:{
                    save:false
                },
                drag:{
                    ongoing:false,
                    index:0,
                    precise:5,
                    y:0,
                    template:''
                },
                dialog:{
                    editContent:{
                        visible:false,
                        module:'',
                        variables:[]
                    },
                    viewportSize:{
                        visible:false,
                        width:'100%',
                        height:'100%'
                    }
                },
                editors:{
                    time:{
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
                    },
                    arraySimple:{
                        visible:false,
                        value:''
                    }
                },
                toolbar:{
                    status:'mini'
                },
                previewViewPort:{
                    status:'',
                    width:'414px',
                    height:'738px'
                }
            }
        },
        methods:{
            doPreview:function(){
                var self = this;
                var html = '';
                for(var i=0; i<self.modules.length; i++){
                    html += self.modules[i].render;
                }
                return html;
            },
            show:function(article){
                var self = this;
                self.visible = true;
                self.article = article;
                ajax.post('/cms/article/query/modules/' + article.id, null, function(data){
                    if(data && data.length>0){
                        for(var i=0; i<data.length; i++){
                            var json = self.translateJSON(data[i].template.js);
                            data[i].render = juicer(data[i].template.html).render(json);
                            data[i].mousein = false;
                            self.modules.push(data[i]);
                        }
                    }
                });
            },
            hide:function(){
                var self = this;
                self.visible = false;
                self.article = '';
                self.modules.splice(0, self.modules.length);
            },
            save:function(){
                var self = this;
                var html = '';
                var c_modules = [];
                for(var i=0; i<self.modules.length; i++){
                    html += self.modules[i].render;
                    var c_module = $.extend(true, {}, self.modules[i]);
                    c_module.render = null;
                    c_module.mousein = null;
                    c_modules.push(c_module);
                }
                var modules = $.toJSON(c_modules);
                var endLoading = function(){
                    self.loading.save = false;
                };
                self.loading.save = true;
                self.$emit('save-article', self.article, html, modules, endLoading);
            },
            templateDragstart:function(template, e){
                var self = this;
                self.drag.y = e.clientY;
                self.drag.ongoing = true;
                self.$nextTick(function(){
                    var $el = $(self.$el);
                    $el.find('.article-modules .el-scrollbar__view').append($el.find('.drop-area'));
                    self.updateDragIndex();
                });
                self.drag.template = template;
            },
            templateDrop:function(e){
                var self = this;
                var template = self.drag.template;
                if(!template.html || !template.js){
                    ajax.post('/cms/template/content/' + template.id, null, function(data){
                        var html = data.html;
                        var js = data.js;
                        template.html = html;
                        template.js = js;
                        var json = self.translateJSON(template.js);
                        var module = {
                            id:'module_'+new Date().getTime(),
                            template: $.extend(true, {}, template),
                            render:juicer(template.html).render(json)
                        };
                        self.modules.splice(self.drag.index, 0, module);
                    });
                }else{
                    var json = self.translateJSON(template.js);
                    var module = {
                        id:'module_'+new Date().getTime(),
                        template:$.extend(true, {}, template),
                        render:juicer(template.html).render(json)
                    };
                    self.modules.splice(self.drag.index, 0, module);
                }
            },
            translateJSON:function(js){
                var json = {};
                if(js){
                    var variables = $.parseJSON(js);
                    if(variables && variables.length>0){
                        for(var i=0; i<variables.length; i++){
                            var variable = variables[i];
                            if(variable.editor === 'ARRAY_IMAGE'){
                                json[variable.key] = variable.value;
                            }else if(variable.editor === 'ARRAY_SIMPLE'){
                                json[variable.key] = variable.value;
                            }else if(variable.editor === 'ARRAY_OBJECT'){

                            }else{
                                json[variable.key] = variable.value;
                            }
                        }
                    }
                }
                return json;
            },
            updateDragIndex:function(){
                var self = this;
                var $el = $(self.$el);
                var $modules = $el.find('.article-modules .el-scrollbar__view');
                var index = 0;
                $modules.children().each(function(){
                    var $this = $(this);
                    if($this.is('.drop-area')){
                        return false;
                    }
                    index += 1;
                });
                self.drag.index = index;
            },
            moduleDragOver:function(e){
                var self = this;
                var $dropArea = $(self.$el).find('.drop-area');
                var $target = $(e.target);
                if(!$target.is('.article-module')){
                    $target = $target.closest('.article-module');
                }
                if((e.clientY-self.drag.y) > self.drag.precise){
                    //往下拽
                    $target.after($dropArea);
                    self.updateDragIndex();
                }else if((self.drag.y-e.clientY) > self.drag.precise){
                    //往上拽
                    $target.before($dropArea);
                    self.updateDragIndex();
                }
                self.drag.y = e.clientY;
            },
            allowDrop:function(e){
                e.preventDefault();
            },
            dragEnd:function(e){
                var self = this;
                self.drag.ongoing = false;
                self.drag.template = '';
            },
            moduleMousemove:function(module, e){
                Vue.set(module, 'mousein', true);
            },
            moduleMouseout:function(module, e){
                Vue.set(module, 'mousein', false);
            },
            moduleEdit:function(module){
                var self = this;
                self.dialog.editContent.visible = true;
                self.dialog.editContent.module = module;
                self.dialog.editContent.variables.splice(0, self.dialog.editContent.variables.length);
                if(module.template.js){
                    var variables = $.parseJSON(module.template.js);
                    if(variables.length > 0){
                        for(var i=0; i<variables.length; i++){
                            self.dialog.editContent.variables.push(variables[i]);
                        }
                    }
                }
            },
            moduleRemove:function(module){
                var self = this;
                var h = self.$createElement;
                self.$msgbox({
                    title:'危险操作',
                    message:h('div', null, [
                        h('div', {class:'el-message-box__status el-icon-warning'}, null),
                        h('div', {class:'el-message-box__message'}, [
                            h('p', null, ['是否要删除此模板?'])
                        ])
                    ]),
                    type:'wraning',
                    showCancelButton: true,
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    beforeClose:function(action, instance, done){
                        if(action === 'confirm'){
                            for(var i=0; i<self.modules.length; i++){
                                if(self.modules[i].id === module.id){
                                    self.modules.splice(i, 1);
                                    break;
                                }
                            }
                        }
                        done();
                    }
                }).catch(function(){});
            },
            handleEditContentClose:function(){
                var self = this;
                self.dialog.editContent.visible = false;
                self.dialog.editContent.module = '';
            },
            handleEditContentCommit:function(){
                var self = this;
                var module = self.dialog.editContent.module;
                var variables = self.dialog.editContent.variables;
                module.template.js = $.toJSON(variables);
                var json = self.translateJSON(module.template.js);
                module.render =juicer(module.template.html).render(json);
                self.handleEditContentClose();
            },
            editContentTitle:function(){
                var self = this;
                return self.dialog.editContent.module?'编辑'+self.dialog.editContent.module.template.name:'';
            },
            handleArraySimpleRemove:function(values, simple){
                var self = this;
                for(var i=0; i<values.length; i++){
                    if(values[i] === simple){
                        values.splice(i, 1);
                        break;
                    }
                }
            },
            handleArraySimpleAdd:function(values){
                var self = this;
                if(values.indexOf(self.editors.arraySimple.value) < 0) values.push(self.editors.arraySimple.value);
                self.editors.arraySimple.visible = false;
                self.editors.arraySimple.value = '';
            },
            handleArraySimpleEdit:function(){
                var self = this;
                self.editors.arraySimple.visible = true;
                self.$nextTick(function(){
                    self.$refs.arraySimpleAddInput[0].$refs.input.focus();
                });
            },
            selectImage:function(variable){
                var self = this;
                self.$refs.selectImage.setBuffer(variable);
                self.$refs.selectImage.open();
            },
            selectedImage:function(url, buff, startLoading, endLoading, done){
                Vue.set(buff, 'value', url);
                done();
            },
            selectTxt:function(variable){
                var self = this;
                self.$refs.selectTxt.setBuffer(variable);
                self.$refs.selectTxt.open();
            },
            selectedTxt:function(txt, buff, startLoading, endLoading, done){
                Vue.set(buff, 'value', txt);
                done();
            },
            selectVideo:function(variable){
                var self = this;
                self.$refs.selectVideo.setBuffer(variable);
                self.$refs.selectVideo.open();
            },
            selectedVideo:function(url, buff, startLoading, endLoading, done){
                Vue.set(buff, 'value', url);
                done();
            },
            selectAudio:function(variable){
                var self = this;
                self.$refs.selectAudio.setBuffer(variable);
                self.$refs.selectAudio.open();
            },
            selectedAudio:function(url, buff, startLoading, endLoading, done){
                Vue.set(buff, 'value', url);
                done();
            },
            editorToolbarShow:function(){
                var self = this;
                self.toolbar.status = 'max';
            },
            editorToolbarHide:function(){
                var self = this;
                self.toolbar.status = 'mini';
            },
            previewViewportMax:function(){
                var self = this;
                self.previewViewPort.status = 'max';
            },
            previewViewportMini:function(){
                var self  = this;
                self.previewViewPort.status = '';
            },
            editViewportSize:function(){
                var self = this;
                self.dialog.viewportSize.visible = true;
                self.dialog.viewportSize.width = self.previewViewPort.width;
                self.dialog.viewportSize.height = self.previewViewPort.height;
            },
            handleViewportSizeClose:function(){
                var self = this;
                self.dialog.viewportSize.width = '';
                self.dialog.viewportSize.height = '';
                self.dialog.viewportSize.visible = false;
            },
            handleViewportSizeCommit:function(){
                var self = this;
                if(typeof self.dialog.viewportSize.width==='string' && self.dialog.viewportSize.width.indexOf('%')>=0){
                    self.previewViewPort.width = self.dialog.viewportSize.width;
                }else{
                    self.previewViewPort.width = self.dialog.viewportSize.width + 'px';
                }
                if(typeof self.dialog.viewportSize.height==='string' && self.dialog.viewportSize.height.indexOf('%')>=0){
                    self.previewViewPort.height = self.dialog.viewportSize.height;
                }else{
                    self.previewViewPort.height = self.dialog.viewportSize.height + 'px';
                }
                self.handleViewportSizeClose();
            },
            handleViewportMaximize:function(){
                var self = this;
                self.dialog.viewportSize.width = '100%';
                self.dialog.viewportSize.height = '100%';
            },
            handleViewportSD:function(){
                var self = this;
                self.dialog.viewportSize.width = '1280';
                self.dialog.viewportSize.height = '720';
            },
            handleViewportHD:function(){
                var self = this;
                self.dialog.viewportSize.width = '1920';
                self.dialog.viewportSize.height = '1080';
            },
            handleViewportPhone:function(){
                var self = this;
                self.dialog.viewportSize.width = '414';
                self.dialog.viewportSize.height = '738';
            },
            variableEditorMousemove:function(variable){
                var self = this;
                Vue.set(variable, '__hover', true);
            },
            variableEditorMouseout:function(variable){
                var self = this;
                Vue.set(variable, '__hover', null);
            }
        },
        created:function(){
            var self = this;
            ajax.post('/cms/template/query/article/templates', null, function(data){
                self.templates.splice(0, self.templates.length);
                if(data && data.length>0){
                    for(var i=0; i<data.length; i++){
                        self.templates.push(data[i]);
                    }
                }
            });
        },
        mounted:function(){

        }
    });

});