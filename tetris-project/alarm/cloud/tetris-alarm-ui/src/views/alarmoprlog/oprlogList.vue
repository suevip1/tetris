<template>
    <section>
        <!--查询工具条-->
        <el-form :inline="true" :model="filters" ref="filters" label-width="80px" size="mini">
            <el-col :span="24" class="toolbar" style="padding-bottom: 0px; margin:0px">
                <el-form-item label="来源服务" prop="sourceService"  :span="6">
                            <el-input v-model="filters.sourceService"></el-input>
                </el-form-item>
                <el-form-item label="操作用户" prop="userName" :span="6">
                            <el-input v-model="filters.userName"></el-input>
                </el-form-item>
                 <el-form-item label="操作名称" prop="oprName"  :span="6">
                            <el-input v-model="filters.oprName"></el-input>
                </el-form-item>
            </el-col>
            <el-col :span="24" class="toolbar" style="padding-top: 0px; padding-bottom: 5px; margin:0px" >
                <el-form-item label="操作时间" prop="oprTimeRange" :span="12">
                    <el-date-picker v-model="filters.oprTimeRange" type="datetimerange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
                            </el-form-item>
                    <el-form-item :span="3">
                                <el-button type="primary" v-on:click="queryOprlogList">查询</el-button>
                            </el-form-item>
                    <el-form-item :span="3">
                    <el-button  v-on:click="resetForm('filters')">重置</el-button>
                </el-form-item>
            </el-col>
        </el-form>
        <!--查询工具条结束-->

        <!--列表-->
        <el-table :data="oprlogVOs" highlight-current-row v-loading="listLoading" @selection-change="selsChange" style="width: 100%;">
            <el-table-column prop="id" v-if="false" width="60"></el-table-column>
            <el-table-column prop="userName" label="用户名" width="100"></el-table-column>
            <el-table-column prop="oprName" label="操作名称" width="200"></el-table-column>
            <el-table-column prop="sourceService" label="来源服务" width="200"></el-table-column>
            <el-table-column prop="oprTime" label="操作时间" :formatter="dateFormat" width="200" sortable></el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <el-button size="small" @click="handleShowDetail(scope.$index, scope.row)">详情</el-button>
                    <el-button type="danger" size="small" @click="handleDel(scope.$index, scope.row)">删除</el-button>
                  </template>
            </el-table-column>
        </el-table>

        <!--表底工具条-->
        <el-col :span="24" class="toolbar">
            <!--<el-button type="danger" @click="batchRemove" :disabled="this.sels.length===0">批量删除</el-button>-->
            <el-pagination layout="prev, pager, next" @current-change="handleCurrentChange" :page-size="20" :total="total" style="float:right;">
            </el-pagination>
        </el-col>
        <!--表底工具条结束-->

        <!--详情弹出框-->
        <el-dialog title="操作详情" :visible.sync="detailFormVisible" :close-on-click-modal="false">

            <el-table :data="tableData" style="width: 100%">
                <el-table-column prop="dataName"  width="180"></el-table-column>
                <el-table-column prop="dataContent" width="180">
                </el-table-column>
            </el-table>
            <!--
            <el-form :model="detailForm" label-position="left" label-width="160px">
               <el-form-item label="序号">
                   <span>{{ detailForm.id }}</span>
               </el-form-item>
               <el-form-item label="用户名">
                   <span>{{ detailForm.userName }}</span>
               </el-form-item>
               <el-form-item label="操作名称">
                   <span>{{ detailForm.oprName }}</span>
               </el-form-item>
               <el-form-item label="操作详情">
                   <span>{{ detailForm.oprDetail }}</span>
               </el-form-item>
               <el-form-item label="操作时间">
                   <span>{{ detailForm.oprTime }}</span>
               </el-form-item>
               <el-form-item label="来源服务">
                   <span>{{ detailForm.sourceService }}</span>
               </el-form-item>
               <el-form-item label="来源IP">
                   <span>{{ detailForm.ip }}</span>
               </el-form-item>
            </el-form>
            -->

        </el-dialog>
        <!--详情弹出框结束-->

    </section>
</template>

<script>
import util from '../../common/js/util'
// import NProgress from 'nprogress'
import { queryOprlogListPage, delOprlog } from '../../api/api'

export default {
  data () {
    return {
      filters: {},
      oprlogVOs: [],
      total: 0,
      page: 1,
      listLoading: false,
      sels: [], // 列表选中列

      detailFormVisible: false, // 详情弹出框是否显示
      editLoading: false,

      alarmLevelSel: [],

      // 详情界面数据
      detailForm: {
        id: 0,
        userName: '',
        oprName: '',
        oprDetail: '',
        oprTime: '',
        sourceService: '',
        ip: ''
      },

      tableData: [],

      bindRoleVisible: false,
      currentUserId: 0
    }
  },

  methods: {
    // 获取告警信息列表
    queryOprlogList () {
      var timeStart, timeEnd

      if (this.filters.oprTimeRange != undefined) {
        if (typeof (this.filters.oprTimeRange[0]) !== undefined) {
          timeStart = util.formatDate.format(this.filters.oprTimeRange[0], 'yyyy-MM-dd hh:mm:ss')
        };

        if (typeof (this.filters.oprTimeRange[1]) !== undefined) {
          timeEnd = util.formatDate.format(this.filters.oprTimeRange[1], 'yyyy-MM-dd hh:mm:ss')
        };
      };

      let para = {
        sourceService: this.filters.sourceService,
        userName: this.filters.userName,
        oprName: this.filters.oprName,
        queryTimeStart: timeStart,
        queryTimeEnd: timeEnd,
        pageIndex: this.page - 1,
        pageSize: 20
      }

      console.info(JSON.stringify(para))

      this.listLoading = true
      // NProgress.start();
      queryOprlogListPage(para).then(res => {
        if (res.errMsg !== null && res.errMsg !== '') {
          this.$message({
            message: res.errMsg,
            type: 'error',
            duration: 3000
          })
        } else {
          this.total = res.total
          this.oprlogVOs = res.oprlogVOs
        }

        this.listLoading = false
        // NProgress.done();
      })
    },

    // 重置表单
    resetForm (formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
      }
    },

    selsChange: function (sels) {
      this.sels = sels
    },

    handleCurrentChange (val) {
      this.page = val
      this.queryOprlogList()
    },

    dateFormat: function (row, column) {
      var dateString = row[column.property]
      if (dateString == undefined) {
        return ''
      }

      return util.formatDate.format(new Date(Date.parse(dateString)), 'yyyy-MM-dd hh:mm:ss')
    },

    // 获取操作日志详情
    handleShowDetail: function (index, row) {
      this.resetForm('detailForm')
      this.detailFormVisible = true
      this.detailForm = Object.assign({}, row)

      var json = []

      json.push({ dataName: 'id', dataContent: this.detailForm.id })
      json.push({ dataName: '用户名', dataContent: this.detailForm.userName })
      json.push({ dataName: '操作名称', dataContent: this.detailForm.oprName })
      json.push({ dataName: '操作时间', dataContent: util.formatDate.formatString(this.detailForm.oprTime, 'yyyy-MM-dd hh:mm:ss') })
      json.push({ dataName: '操作详情', dataContent: this.detailForm.oprDetail })
      json.push({ dataName: '来源服务', dataContent: this.detailForm.sourceService })
      json.push({ dataName: '来源IP', dataContent: this.detailForm.ip })

      this.tableData = json
    },

    // 删除告警基本信息
    handleDel: function (index, row) {
      this.$confirm('删除告警基本信息会删除所有此类告警，确认？', '提示', {}).then(() => {
        let para = {
          ids: row.id
        }

        delOprlog(para).then(res => {
          if (res.errMsg !== null && res.errMsg !== '') {
            this.$message({
              message: res.errMsg,
              type: 'error',
              duration: 3000
            })
          } else {
            this.queryOprlogList()
          }
        })
      })
    },
  },

  mounted () {
    this.queryOprlogList()
  }
}
</script>
