<template>
  <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <div class="usersSerch">
          <a-input-search
            v-model:value="param.loginName"
            placeholder="登陆名"
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
      <a-form-item label="登陆名">
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
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';
import { Tool } from '@/util/tool';

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
    const user = ref();
    user.value = {};

    const param = ref();
    param.value = {};

    // 数组 [100, 101] ，对应：前端开发 / Vue
    const categoryIds = ref();
    const level1 = ref();

    const columns = [
        {
          title: '登陆名',
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
      categoryIds.value = [user.value.category1Id, user.value.category2Id];
      modalVisible.value = true;
    };

    // 新增页面显示
    const add = () => {
      user.value = {};
      categoryIds.value = [];
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
      modalLoading.value = true;
      user.value.category1Id = categoryIds.value[0];
      user.value.category2Id = categoryIds.value[1];
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
    };

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
    });

    return { users, user, columns, pagination, loading, modalVisible, modalLoading, param, categoryIds, level1, handleQuery, handleTableChange, handleModalOk, handleDelete, edit, add }
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
