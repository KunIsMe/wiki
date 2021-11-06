<template>
    <a-layout-footer style="text-align: center">
      wiki电子书<span v-show="user.id" style="fontSize:13px;">，欢迎：</span>{{user.name}}
    </a-layout-footer>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue';
import store from '@/store';

export default defineComponent({
  name: 'TheFooter',
  setup () {
    const user = computed(() => store.state.user);

    return { user };
  }
});
</script>