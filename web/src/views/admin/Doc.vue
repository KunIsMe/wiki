<template>
  <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <div class="docsSerch">
          <a-input-search
            v-model:value="param.name"
            placeholder="名称"
            enter-button
            @search="handleQuery()"
          />
          <a-button type="primary" @click="add">
            新增
          </a-button>
        </div>
        <a-table
         :columns="columns"
         :row-key="record => record.id"
         :data-source="level1"
         :pagination="false"
         :loading="loading"
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
   title="文档表单"
   v-model:visible="modalVisible"
   :confirm-loading="modalLoading"
   @ok="handleModalOk">
    <a-form :model="doc" :label-col="{ span: 3 }" :wrapper-col="wrapperCol">
      <a-form-item label="名称">
        <a-input v-model:value="doc.name" />
      </a-form-item>
      <a-form-item label="父文档">
        <a-select
          ref="select"
          v-model:value="doc.parent"
        >
          <a-select-option
           :value="0"
          >
            无
          </a-select-option>
          <template
           v-for="c in level1"
           :key="c.id"
          >
            <a-select-option
              :value="c.id"
              v-if="doc.id !== c.id"
            >
              {{c.name}}
            </a-select-option>
          </template>
        </a-select>
      </a-form-item>
      <a-form-item label="顺序">
        <a-input v-model:value="doc.sort" />
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
  name: 'Doc',
  setup () {
    const docs = ref();
    const loading = ref(false);

    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const doc = ref({});

    const param = ref();
    param.value = {};

    // 文档树定义
    const level1 = ref();

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name'
      },
      {
        title: '父文档',
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
    const handleQuery = () => {
      loading.value = true;
      axios.get("/doc/all", {
         params: {
           name: param.value.name
         }
        }).then((response) => {
        loading.value = false;
        const data = response.data;
        if(data.success) {
          docs.value = data.content;
          level1.value = [];
          level1.value = Tool.array2Tree(docs.value, 0);
        } else {
          message.error(data.message);
        }
      })
    };

    // 编辑页面显示
    const edit = (record: any) => {
      doc.value = Tool.copy(record);
      modalVisible.value = true;
    };

    // 新增页面显示
    const add = () => {
      doc.value = {};
      modalVisible.value = true;
    };

    // 文档删除
    const handleDelete = (id: number) => {
      axios.delete(`/doc/delete/${id}`).then((response) => {
        const data = response.data;
        if(data.success){
          // 重新加载列表
          handleQuery();
        }
      })
    }

    // 确认提交 编辑/新增
    const handleModalOk = () => {
      modalLoading.value = true;
      axios.post("/doc/save", doc.value).then((response) => {
        modalLoading.value = false;
        const data = response.data;
        if(data.success){
          modalVisible.value = false;

          // 重新加载列表
          handleQuery();
        } else {
          message.error(data.message);
        }
      })
    };

    onMounted(() => {
      handleQuery();
    });

    return { level1, doc, columns, loading, modalVisible, modalLoading, param, handleQuery, handleModalOk, handleDelete, edit, add }
  }
})
</script>

<style scoped>
  img {
    width: 50px;
    height: 50px;
  }
  .docsSerch {
    width: 380px;
    margin-bottom: 24px;
  }
  .ant-input-search {
    width: 70%;
    margin-right: 20px;
  }
</style>
