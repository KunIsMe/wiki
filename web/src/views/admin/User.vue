<template>
  <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <div class="usersSerch">
          <a-input-search
            v-model:value="param.loginName"
            placeholder="登录名"
            enter-button
            @search="handleQuery({page: 1, size: pagination.pageSize})"
          />
          <a-button type="primary" @click="add">
            新增
          </a-button>
        </div>
        <a-table
         :columns="columns"
         :row-key="record => record.id"
         :data-source="users"
         :pagination="pagination"
         :loading="loading"
         @change="handleTableChange"
        >
          <template #cover="{ text: cover }">
            <img v-if="cover" :src="cover" alt="avatar" />
          </template>
          <template v-slot:category="{ text, record }">
            <span>{{ getCategoryName(record.category1Id) }} / {{ getCategoryName(record.category2Id) }}</span>
          </template>
          <template v-slot:action="{ text, record }">
            <a-space size="small">
              <a-button type="primary" @click="resetPassword(record)">
                重置密码
              </a-button>
              <a-button type="primary" @click="edit(record)">
                编辑
              </a-button>
              <a-popconfirm
                title="删除后不可恢复，确认删除？"
                ok-text="是"
                cancel-text="否"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="danger">
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </a-table>
      </a-layout-content>
  </a-layout>
  <a-modal
   title="用户表单"
   v-model:visible="modalVisible"
   :confirm-loading="modalLoading"
   @ok="handleModalOk">
    <a-form :model="user" :label-col="{ span: 3 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="登录名">
        <a-input v-model:value="user.loginName" :disabled="!!user.id"/>
      </a-form-item>
      <a-form-item label="昵称">
        <a-input v-model:value="user.name" />
      </a-form-item>
      <a-form-item label="密码" v-show="!user.id">
        <a-input v-model:value="user.password" type="password"/>
      </a-form-item>
    </a-form>
  </a-modal>

  <a-modal
   title="重置密码"
   v-model:visible="resetModalVisible"
   :confirm-loading="resetModalLoading"
   @ok="handleResetModalOk">
    <a-form :model="user" :label-col="{ span: 3 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="新密码">
        <a-input v-model:value="user.password"/>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';
import { Tool } from '@/util/tool';

declare let hexMd5: any;
declare let KEY: any;

export default defineComponent({
  name: 'User',
  setup () {
    const users = ref();
    const pagination = ref({
      current: 1,
      pageSize: 10,
      total: 0
    });
    const loading = ref(false);

    const modalVisible = ref(false);
    const modalLoading = ref(false);

    const resetModalVisible = ref(false);
    const resetModalLoading = ref(false);

    const user = ref();
    user.value = {};

    const param = ref();
    param.value = {};

    const level1 = ref();

    const columns = [
        {
          title: '登录名',
          dataIndex: 'loginName'
        },
        {
          title: '名称',
          dataIndex: 'name'
        },
        {
          title: '密码',
          dataIndex: 'password'
        },
        {
          title: 'Action',
          key: 'action',
          slots: { customRender: 'action' }
        }
    ];

    /**
     * 数据查询
     */
    const handleQuery = (params: any) => {
      loading.value = true;
      axios.get("/user/list", {
         params: {
           page: params.page,
           size: params.size,
           loginName: param.value.loginName
         }
        }).then((response) => {
          loading.value = false;
          const data = response.data;
          if(data.success) {
            users.value = data.content.list;

            // 重置分页按钮
            pagination.value.current = params.page;
            pagination.value.total = data.content.total;
          } else {
            message.error(data.message);
          }
        })
    };

    /**
     * 表格点击页码时触发
     */
    const handleTableChange = (pagination: any) => {
      // console.log("看看自带的分页参数都有啥：" + pagination);
      handleQuery({
        page: pagination.current,
        size: pagination.pageSize
      });
    };

    // 编辑页面显示
    const edit = (record: any) => {
      user.value = Tool.copy(record);
      modalVisible.value = true;
    };

    // 新增页面显示
    const add = () => {
      user.value = {};
      modalVisible.value = true;
    };

    // 用户删除
    const handleDelete = (id: number) => {
      axios.delete(`/user/delete/${id}`).then((response) => {
        const data = response.data;
        if(data.success){
          // 重新加载列表
          handleQuery({
            page: pagination.value.current,
            size: pagination.value.pageSize
          });
        }
      })
    }

    // 确认提交 编辑/新增
    const handleModalOk = () => {
      if(/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,32}$/.test(user.value.password)) {
        modalLoading.value = true;

        // md5 加密传输
        user.value.password = hexMd5(user.value.password + KEY);

        axios.post("/user/save", user.value).then((response) => {
          modalLoading.value = false;
          const data = response.data;
          if(data.success){
            modalVisible.value = false;

            // 重新加载列表
            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize
            });
          } else {
            message.error(data.message);
          }
        })
      } else {
        message.error("【密码】至少包含 数字和英文，长度为6~32");
      }
    };

    // 重置密码页面显示
    const resetPassword = (record: any) => {
      user.value = Tool.copy(record);
      user.value.password = null;
      resetModalVisible.value = true;
    };

    // 确认提交 重置密码
    const handleResetModalOk = () => {
      if(/^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,32}$/.test(user.value.password)) {
        resetModalLoading.value = true;
        // md5 加密传输
        user.value.password = hexMd5(user.value.password + KEY);

        axios.post("/user/reset-password", user.value).then((response) => {
          resetModalLoading.value = false;
          const data = response.data;
          if(data.success){
            resetModalVisible.value = false;

            // 重新加载列表
            handleQuery({
              page: pagination.value.current,
              size: pagination.value.pageSize
            });
          } else {
            message.error(data.message);
          }
        })
      } else {
        message.error("【密码】至少包含 数字和英文，长度为6~32");
      }
    };

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
    });

    return { users, user, columns, pagination, loading, modalVisible, modalLoading, param, level1, resetModalVisible, resetModalLoading, handleQuery, handleTableChange, handleModalOk, handleDelete, edit, add, handleResetModalOk, resetPassword }
  }
})
</script>

<style scoped>
  img {
    width: 50px;
    height: 50px;
  }
  .usersSerch {
    width: 380px;
    margin-bottom: 24px;
  }
  .ant-input-search {
    width: 70%;
    margin-right: 20px;
  }
</style>
