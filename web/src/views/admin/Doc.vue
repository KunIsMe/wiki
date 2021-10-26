<template>
  <a-layout>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <a-row :gutter="24">
          <a-col :span="8">
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
            size="small"
            >
              <template #name="{ text, record }">
                {{record.sort}} {{text}}
              </template>
              <template v-slot:action="{ text, record }">
                <a-space size="small">
                  <a-button type="primary" @click="edit(record)" size="small">
                    编辑
                  </a-button>
                  <a-popconfirm
                    title="本文档连同子文档一起删除并不可恢复，确认删除？"
                    ok-text="是"
                    cancel-text="否"
                    @confirm="handleDelete(record.id)"
                  >
                    <a-button type="danger" size="small">
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table>
          </a-col>
          <a-col :span="16">
            <p>
              <a-form layout="inline" :model="param">
                <a-form-item>
                  <a-button type="primary" @click="handleSave()">
                    保存
                  </a-button>
                </a-form-item>
              </a-form>
            </p>
            <a-form :model="doc" layout="vertical">
              <a-form-item>
                <a-input v-model:value="doc.name" placeholder="名称" />
              </a-form-item>
              <a-form-item>
                <a-tree-select
                  v-model:value="doc.parent"
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  :tree-data="treeSelectData"
                  placeholder="请选择父文档"
                  tree-default-expand-all
                  :replaceFields="{title: 'name', key: 'id', value: 'id'}"
                >
                </a-tree-select>
              </a-form-item>
              <a-form-item>
                <a-input v-model:value="doc.sort" placeholder="顺序" />
              </a-form-item>
              <a-form-item>
                <div id="content"></div>
              </a-form-item>
            </a-form>
          </a-col>
        </a-row>
      </a-layout-content>
  </a-layout>
  <!-- <a-modal
   title="文档表单"
   v-model:visible="modalVisible"
   :confirm-loading="modalLoading"
   @ok="handleModalOk">
  </a-modal> -->
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { message } from 'ant-design-vue';
import E from 'wangeditor';
import { Tool } from '@/util/tool';

export default defineComponent({
  name: 'Doc',
  setup () {
    const route = useRoute();

    const docs = ref();
    const loading = ref(false);

    const modalVisible = ref(false);
    const modalLoading = ref(false);
    const doc = ref({});

    const param = ref();
    param.value = {};

    // 文档树定义
    const level1 = ref();

    // 树形选择组件内容
    const treeSelectData = ref();
    treeSelectData.value = [];

    const columns = [
      {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
        slots: { customRender: 'name' }
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

    // 将某节点及其子孙节点全部置为disabled
    const setDisable = (treeSelectData: any, id: any) => {
      // 遍历数组，即遍历某一层节点
      for (let i=0; i<treeSelectData.length; i++) {
        const node = treeSelectData[i];
        if (node.id === id) {
          // 将目标节点设置为disabled
          node.disabled = true;

          // 遍历所有子节点，将所有子节点全部都加上disabled
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            for (let j=0; j<children.length; j++) {
              setDisable(children, children[j].id);
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            setDisable(children, id);
          } 
        }
      }
    }

    let ids: Array<string> = [];
    // 将某节点及其子孙节点全部删除
    const getDeleteIds = (treeSelectData: any, id: any) => {
      // 遍历数组，即遍历某一层节点
      for (let i=0; i<treeSelectData.length; i++) {
        const node = treeSelectData[i];
        if (node.id === id) {
          // 将目标节点放入到删除数组中
          ids.push(node.id);

          // 遍历所有子节点，将所有子节点全部都放入到删除数组中
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            for (let j=0; j<children.length; j++) {
              getDeleteIds(children, children[j].id);
            }
          }
        } else {
          // 如果当前节点不是目标节点，则到其子节点再找找看
          const children = node.children;
          if (Tool.isNotEmpty(children)) {
            getDeleteIds(children, id);
          } 
        }
      }
    }

    // 编辑页面显示
    const edit = (record: any) => {
      doc.value = Tool.copy(record);

      // 不能选择当前节点及其所有子孙节点，作为父节点，会使树断开
      treeSelectData.value = Tool.copy(level1.value);
      setDisable(treeSelectData.value, record.id);

      // 为选择树添加一个“无”
      treeSelectData.value.unshift({id: 0, name: '无'});

      modalVisible.value = true;
    };

    // 新增页面显示
    const add = () => {
      doc.value = {
        ebookId: route.query.ebookId
      };

      treeSelectData.value = Tool.copy(level1.value);
      treeSelectData.value.unshift({id: 0, name: '无'});

      modalVisible.value = true;
    };

    // 文档删除
    const handleDelete = (id: number) => {
      getDeleteIds(level1.value, id);
      axios.delete(`/doc/delete/${ids.join(',')}`).then((response) => {
        const data = response.data;
        if(data.success){
          ids = [];
          // 重新加载列表
          handleQuery();
        }
      })
    }

    // 确认提交 编辑/新增
    const handleSave = () => {
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

      const editor = new E('#content');
      editor.config.zIndex = 0;
      editor.create();
    });

    return { level1, doc, columns, loading, modalVisible, modalLoading, param,  treeSelectData, handleQuery, handleSave, handleDelete, edit, add }
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
