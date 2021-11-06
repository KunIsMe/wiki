<template>
    <a-layout-header class="header">
      <div class="logo" />
      <a class="loginMenu" v-show="!user.id" @click="showLoginModal">
        <span>登录</span>
      </a>
      <a-popconfirm
        title="确认退出登录？"
        ok-text="是"
        cancel-text="否"
        @confirm="logout"
      >
        <a class="loginMenu" v-show="user.id">
          <span>退出登录</span>
        </a>
      </a-popconfirm>
      <a class="loginMenu" v-show="user.id">
        <span style="fontSize:13px;">您好：</span>{{user.name}}
      </a>
      <a-menu
        theme="dark"
        mode="horizontal"
        v-model:selectedKeys="selectedKeys1"
        :style="{ lineHeight: '64px' }"
      >
        <a-menu-item key="/">
          <router-link to="/">首页</router-link>
        </a-menu-item>
        <a-menu-item key="/admin/user" v-if="user.id">
          <router-link to="/admin/user">用户管理</router-link>
        </a-menu-item>
        <a-menu-item key="/admin/ebook" v-if="user.id">
          <router-link to="/admin/ebook">电子书管理</router-link>
        </a-menu-item>
        <a-menu-item key="/admin/category" v-if="user.id">
          <router-link to="/admin/category">分类管理</router-link>
        </a-menu-item>
        <a-menu-item key="/about">
          <router-link to="/about">关于我们</router-link>
        </a-menu-item>
      </a-menu>
    </a-layout-header>
    <a-modal
      title="登录"
      v-model:visible="loginModalVisible"
      :confirm-loading="loginModalLoading"
      @ok="login"
    >
      <a-form :model="loginUser" :label-col="{ span: 3 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="登录名">
          <a-input v-model:value="loginUser.loginName"/>
        </a-form-item>
        <a-form-item label="密码">
          <a-input v-model:value="loginUser.password" type="password"/>
        </a-form-item>
      </a-form>
    </a-modal>
</template>

<script lang="ts">
import { computed, defineComponent, ref } from 'vue';
import store from '@/store';
import axios from 'axios';
import { message } from 'ant-design-vue';

declare let hexMd5: any;
declare let KEY: any;

export default defineComponent({
  name: 'TheHeader',
  setup () {
    const loginUser = ref();
    loginUser.value = {
      loginName: '',
      password: ''
    };

    const user = computed(() => store.state.user);

    const loginModalVisible = ref(false);
    const loginModalLoading = ref(false);

    const showLoginModal = () => {
      loginModalVisible.value = true;
    };

    const login = () => {
      console.log("开始登录");
      loginModalLoading.value = true;
      loginUser.value.password = hexMd5(loginUser.value.password + KEY);
      axios.post('/user/login', loginUser.value).then((response) => {
        loginModalLoading.value = false;
        const data = response.data;
        if (data.success) {
          store.commit("setUser", data.content);
          loginModalVisible.value = false;
          message.success("登录成功！");
        } else {
          message.error(data.message);
        }
      });
    };

    const logout = () => {
      console.log("开始退出登录");
      axios.get('/user/logout/' + user.value.token).then((response) => {
        const data = response.data;
        if (data.success) {
          store.commit("setUser", {});
          message.success("退出登录成功！");
        } else {
          message.error(data.message);
        }
      });
    };

    return { loginUser, loginModalVisible, loginModalLoading, user, showLoginModal, login, logout };
  }
});
</script>

<style scoped>
.loginMenu {
  float: right;
  color: white;
  margin-left: 14px;
}
a:hover {
  color: lightskyblue;
}
</style>
