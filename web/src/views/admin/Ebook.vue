<template>
  <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <div class="ebooksSerch">
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
         :data-source="ebooks"
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
   title="电子书表单"
   v-model:visible="modalVisible"
   :confirm-loading="modalLoading"
   @ok="handleModalOk">
    <a-form :model="ebook" :label-col="{ span: 3 }" :wrapper-col="wrapperCol">
      <a-form-item label="封面">
        <a-input v-model:value="ebook.cover" />
      </a-form-item>
      <a-form-item label="名称">
        <a-input v-model:value="ebook.name" />
      </a-form-item>
      <a-form-item label="分类">
        <a-cascader
         v-model:value="categoryIds"
         :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
         :options="level1"
        />
      </a-form-item>
      <a-form-item label="描述">
        <a-input v-model:value="ebook.description" />
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
  name: 'Ebook',
  setup () {
    const ebooks = ref();
    const pagination = ref({
      current: 1,
      pageSize: 10,
      total: 0
    });
    const loading = ref(false);

    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const ebook = ref();
    ebook.value = {};

    const param = ref();
    param.value = {};

    // 数组 [100, 101] ，对应：前端开发 / Vue
    const categoryIds = ref();
    const level1 = ref();

    const columns = [
      {
        title: '封面',
        dataIndex: 'cover',
        key: 'cover',
        slots: { customRender: 'cover' }
      },
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name'
      },
      {
        title: '分类',
        slots: { customRender: 'category' }
      },
      {
        title: '文档数',
        dataIndex: 'docCount',
        key: 'docCount'
      },
      {
        title: '阅读数',
        dataIndex: 'viewCount',
        key: 'viewCount'
      },
      {
        title: '点赞数',
        dataIndex: 'voteCount',
        key: 'voteCount'
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
      axios.get("/ebook/list", {
         params: {
           page: params.page,
           size: params.size,
           name: param.value.name
         }
        }).then((response) => {
          loading.value = false;
          const data = response.data;
          if(data.success) {
            ebooks.value = data.content.list;

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
      ebook.value = Tool.copy(record);
      categoryIds.value = [ebook.value.category1Id, ebook.value.category2Id];
      modalVisible.value = true;
    };

    // 新增页面显示
    const add = () => {
      ebook.value = {};
      categoryIds.value = [];
      modalVisible.value = true;
    };

    // 电子书删除
    const handleDelete = (id: number) => {
      axios.delete(`/ebook/delete/${id}`).then((response) => {
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
      ebook.value.category1Id = categoryIds.value[0];
      ebook.value.category2Id = categoryIds.value[1];
      axios.post("/ebook/save", ebook.value).then((response) => {
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

    let categorys: any;
    // 级联分类操作
    const handleQueryCategory = () => {
      loading.value = true;
      axios.get("/category/all", {
         params: {
           name: param.value.name
         }
        }).then((response) => {
        loading.value = false;
        const data = response.data;
        if(data.success) {
          categorys = data.content;
          level1.value = [];
          level1.value = Tool.array2Tree(categorys, 0);
        } else {
          message.error(data.message);
        }
      })
    };

    const getCategoryName = (cid: number) => {
      let result = "";
      categorys.forEach((item: any) => {
        if (item.id == cid) {
          result = item.name;
        }
      });
      return result;
    }

    onMounted(() => {
      handleQuery({
        page: 1,
        size: pagination.value.pageSize
      });
      handleQueryCategory();
    });

    return { ebooks, ebook, columns, pagination, loading, modalVisible, modalLoading, param, categoryIds, level1, handleQuery, handleTableChange, handleModalOk, handleDelete, edit, add, getCategoryName }
  }
})
</script>

<style scoped>
  img {
    width: 50px;
    height: 50px;
  }
  .ebooksSerch {
    width: 380px;
    margin-bottom: 24px;
  }
  .ant-input-search {
    width: 70%;
    margin-right: 20px;
  }
</style>
