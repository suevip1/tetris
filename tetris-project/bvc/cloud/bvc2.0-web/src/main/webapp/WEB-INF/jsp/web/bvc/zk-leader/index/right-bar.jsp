<%@ page language="java" contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
 <%
    String host=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
    String scheme=request.getContextPath();
    String basePath = host +scheme +"/";
    String token = request.getParameter("token");
    String scope =request.getParameter("scope");
%>
<!DOCTYPE HTML>
<html lang="zh-cmn-Hans">
<head>

    <!-- 声明文档使用的字符编码 -->
    <meta charset='utf-8'>
    <!-- 优先使用IE 最新版本和Chrome-->
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 页面描述 -->
    <meta name="description" content=""/>
    <!-- 页面关键词 -->
    <meta name="keywords" content=""/>
    <!-- 网页坐着 -->
    <meta name="author" content="name,email@gmail.com"/>
    <!-- 搜索引擎抓取 -->
    <meta name="robots" content="index,follow"/>
    <!-- 为移动设备添加视窗 -->
    <meta name="viewport" content="initial-scale=1,maximum-scale=3,minimum-scale=1,user-scalable=no"/>
    <!-- `width=device-width` 会导致 iPhone 5 添加到主屏后以 WebApp 全屏模式打开页面时出现黑边 http://bigc.at/ios-webapp-viewport-meta.orz -->

    <!-- ios 设备 begin -->
    <meta name="apple-mobile-web-app-title" content="标题"/>
    <!-- 添加到主屏后的标题（ios6新增）-->
    <meta name="apple-mobile-web-app-capable" content="yes"/>
    <!-- 是否启用 WebApp 全屏模式，删除苹果默认的工具栏和菜单栏 -->
    <meta name="apple-itunes-app" content="app-id=myAppStoreID,affiliate-data=myAffiliateData,app-argument=myURL"/>
    <!-- 添加智能 App 广告条 Smart App Banner (ios 6+ Safari) -->
    <meta name="apple-mobile-web-app-status-bar-style" content="black"/>
    <!-- 设置苹果工具栏颜色 -->
    <meta name="format-detection" content="telphone=no,email=no"/>
    <!-- 忽略页面中的数字识别为电话，忽略email识别 -->
    <!-- 启用360浏览器的极速模式(webkit) -->
    <meta name="renderer" content="webkit"/>
    <!-- 避免IE使用兼容模式 -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <!-- 不让百度转码 -->
    <meta http-equiv="Cache-Control" content="no-siteapp"/>
    <!-- 针对手持设备优化，主要是针对一些老的不识别viewport的浏览器，比如黑莓 -->
    <meta name="HandleFriendly" content="true"/>
    <!-- 微软的老式浏览器 -->
    <meta name="MobileOptimized" content="320"/>
    <!-- uc强制竖屏-->
    <meta name="screen-orientation" content="portrait"/>
    <!-- QQ强制竖屏-->
    <meta name="x5-orientation" content="portrait"/>
    <!-- UC强制全屏 -->
    <meta name="full-screen" content="yes">
    <!-- QQ强制全屏 -->
    <meta name="x5-fullscreen" content="true">
    <!-- UC应用模式 -->
    <meta name="browsermode" content="application">
    <!-- QQ应用模式 -->
    <meta name="x5-page-mode" content="app">
    <!-- windows phone 点击无高光 -->
    <meta name="msapplication-tap-highlight" content="no">

    <link rel="stylesheet" type="text/css" href="<%=basePath %>web/lib/ui/element-ui/element-ui-2.4.3.min.css"/>
    <!-- icons -->
    <link rel="stylesheet" type="text/css" href="<%=basePath %>web/lib/icon/Font-Awesome-3.2.1/css/font-awesome.css"/>

    <script type="text/javascript">
    window.HOST = '<%=host%>';
    window.SCHEMA = '<%=scheme%>';
    window.BASEPATH = '<%=basePath%>';
    window.LIBPATH = 'web/lib/';
    window.APPPATH = 'web/app/';
    window.COMMONSPATH = 'web/commons/';
    window.TOKEN = '<%=token%>';
    </script>

    <style type="text/css">
    /* reset */
    *{font-family:'Microsoft YaHei';}
    html,body{width:100%; height:100%; margin:0; padding:0;}
    .page-wrapper{width:100%; height:100%; padding:0; border:0; position:relative;}
    .page-body>.el-row{width:100%!important;}
    .page-body>.el-row,
    .page-body>.el-row>.el-col{height:100%!important;}
    .el-loading-mask{transition:none !important;}
    </style>

    <title>侧边栏</title>
</head>
<body onselectstart="return false;" unselectable="on">
    <div id="app" class="page-wrapper">
        <leader-rightbar ref="rightBar"></leader-rightbar>
    </div>
</body>
<script type="text/javascript" src="qrc:///qtwebchannel/qwebchannel.js"></script>
<script type="text/javascript" src="<%=basePath %>web/commons/qt/QtContext.js"></script>
<script type="text/javascript" src="<%=basePath %>web/lib/frame/requireJS/require.js" defer async="true" data-main="<%=basePath %>web/app/zk-leader/index/main"></script>
</html>