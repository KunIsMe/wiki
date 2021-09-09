<template>
  <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <div class="categorysSerch">
          <a-input-search
            v-model:value="param.name"
            placeholder="名称"
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
         :data-source="categorys"
         :pagination="pagination"
         :loading="loading"
         @change="handleTableChange"
        >
          <template #cover="{ text: cover }">
            <img v-if="cover" :src="cover" alt="avatar" />
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
   title="分类表单"
   v-model:visible="modalVisible"
   :confirm-loading="modalLoading"
   @ok="handleModalOk">
    <a-form :model="category" :label-col="{ span: 3 }" :wrapper-col="wrapperCol">
      <a-form-item label="名称">
        <a-input v-model:value="category.name" />
      </a-form-item>
      <a-form-item label="父分类">
        <a-input v-model:value="category.parent" />
      </a-form-item>
      <a-form-item label="顺序">
        <a-input v-model:value="category.sort" />
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
  name: 'Category',
  setup () {
    const categorys = ref();
    const pagination = ref({
      current: 1,
      pageSize: 10,
      total: 0
    });
    const loading = ref(false);

    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const category = ref({});

    const param = ref();
    param.value = {};

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name'
      },
      {
        title: '父分类',
        dataIndex: 'parent',
        key: 'parent'
      },
      {
        title: '顺序',
        dataIndex: 'sort',
        key: 'sort'
      },
      {
        title: 'Action',
        dataIndex: 'action',
        key: 'action',
        slots: { customRender: 'action' }
      }
    ];

    /**
     * 数据查询
     */
    const handleQuery = (params: any) => {
      loading.value = true;
      axios.get("/category/list", {
         params: {
           page: params.page,
           size: params.size,
           name: param.value.name
         }
        }).then((response) => {
        loading.value = false;
        const data = response.data;
        if(data.success) {
          categorys.value = data.content.list;

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
      category.value = Tool.copy(record);
      modalVisible.value = true;
    };

    // 新增页面显示
    const add = () => {
      category.value = {};
      modalVisible.value = true;
    };

    // 分类删除
    const handleDelete = (id: number) => {
      axios.delete(`/category/delete/${id}`).then((response) => {
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
      axios.post("/category/save", category.value).then((response) => {
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

    return { categorys, category, columns, pagination, loading, modalVisible, modalLoading, param, handleQuery, handleTableChange, handleModalOk, handleDelete, edit, add }
  }
})
</script>

<style scoped>
  img {
    width: 50px;
    height: 50px;
  }
  .categorysSerch {
    width: 380px;
    margin-bottom: 24px;
  }
  .ant-input-search {
    width: 70%;
    margin-right: 20px;
  }
</style>
