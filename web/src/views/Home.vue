<template>
    <a-layout>
      <a-layout-sider width="200" style="background: #fff">
        <a-menu
          mode="inline"
          :openKeys="openKeys"
          v-model:selectedKeys="selectedKeys"
          @click="handleClick"
          :style="{ height: '100%', borderRight: 0 }"
        >
          <a-menu-item key="welcome">
            <router-link to="/">
              <UserOutlined />
              <span>欢迎</span>
            </router-link>
          </a-menu-item>
          <a-sub-menu v-for="item in level1" :key="item.id">
            <template #title>
              <span><user-outlined />{{item.name}}</span>
            </template>
            <a-menu-item v-for="child in item.children" :key="child.id">
              <MailOutlined />
              <span>{{child.name}}</span>
            </a-menu-item>
          </a-sub-menu>
        </a-menu>
      </a-layout-sider>
      <a-layout-content
        :style="{ background: '#fff', padding: '24px', margin: 0, minHeight: '280px' }"
      >
        <div class="welcome" v-show="isShowWelcome">
          <h1>欢迎使用 wiki 知识库</h1>
        </div>
        <a-list item-layout="vertical" size="large" :grid="{ gutter: 20, column: 3 }" :data-source="ebooks1" v-show="!isShowWelcome">
          <template #renderItem="{ item }">
            <a-list-item key="item.name">
              <template #actions>
                <span v-for="{ type, text } in actions" :key="type">
                  <component v-bind:is="type" style="margin-right: 8px" />
                  {{ text }}
                </span>
              </template>
              <a-list-item-meta :description="item.description">
                <template #title>
                  <a :href="item.href">{{ item.name }}</a>
                </template>
                <template #avatar><a-avatar :src="item.cover" /></template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
      </a-layout-content>
    </a-layout>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref, reactive, toRefs } from 'vue';
import axios from 'axios';
import { message } from 'ant-design-vue';
import { Tool } from '@/util/tool';

export default defineComponent({
  name: 'Home',
  setup () {
    const ebooks1 = ref();
    const ebooks2 = reactive({ ebooks: [] });

    const level1 = ref();

    const isShowWelcome = ref(true);

    const handleQueryCategory = () => {
      axios.get("/category/all").then((response) => {
        const data = response.data;
        if(data.success) {
          const categorys = data.content;
          level1.value = [];
          level1.value = Tool.array2Tree(categorys, 0);
        } else {
          message.error(data.message);
        }
      })
    };

    onMounted(() => {
      handleQueryCategory(),
      axios.get("/ebook/list", {
        params: {
          page: 1,
          size: 1000
        }
      }).then((response) => {
        const data = response.data;
        ebooks1.value = data.content.list;
        ebooks2.ebooks = data.content.list;
      })
    });

    const handleClick = (value: any) => {
      if (value.key === 'welcome') {
        isShowWelcome.value = true;
      } else {
        isShowWelcome.value = false;
      }
    }

    const actions: Record<string, string>[] = [
      { type: 'StarOutlined', text: '156' },
      { type: 'LikeOutlined', text: '156' },
      { type: 'MessageOutlined', text: '2' },
    ];

    const { ebooks } = toRefs(ebooks2);

    return { ebooks1, ebooks, actions, level1, isShowWelcome, handleClick }
  }
});
</script>

<style scoped>
.ant-avatar {
  width: 50px;
  height: 50px;
  line-height: 50px;
  border-radius: 8%;
  margin: 5px 0;
}
</style>
