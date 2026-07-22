<script setup lang="tsx">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { createRoot, type Root } from 'react-dom/client'
import CanvasApp from './CanvasApp'
const mount = ref<HTMLElement>(); let root: Root | undefined
const emit = defineEmits<{ back: [] }>()
onMounted(() => {
  if (!mount.value) return
  const mountedRoot = createRoot(mount.value)
  root = mountedRoot
  mountedRoot.render(<CanvasApp onBack={() => emit('back')} />)
})
onBeforeUnmount(() => root?.unmount())
</script>
<template><div ref="mount" class="canvas-mount" /></template>
<style scoped>.canvas-mount{position:fixed;z-index:3000;inset:0}</style>
