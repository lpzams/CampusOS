<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const cells = ref<number[]>([])
const score = ref(0)
const over = ref(false)

function addTile() {
  const empty = cells.value.map((n, i) => n ? -1 : i).filter(i => i >= 0)
  if (empty.length) cells.value[empty[Math.floor(Math.random() * empty.length)]] = Math.random() < .9 ? 2 : 4
}
function reset() { cells.value = Array(16).fill(0); score.value = 0; over.value = false; addTile(); addTile() }
function slide(line: number[]) {
  const result = line.filter(Boolean)
  for (let i = 0; i < result.length - 1; i++) if (result[i] === result[i + 1]) {
    result[i] *= 2; score.value += result[i]; result.splice(i + 1, 1)
  }
  return [...result, ...Array(4 - result.length).fill(0)]
}
function move(direction: 'left' | 'right' | 'up' | 'down') {
  if (over.value) return
  const before = cells.value.join()
  const next = [...cells.value]
  for (let n = 0; n < 4; n++) {
    const indexes = direction === 'left' || direction === 'right'
      ? [0, 1, 2, 3].map(i => n * 4 + i)
      : [0, 1, 2, 3].map(i => i * 4 + n)
    if (direction === 'right' || direction === 'down') indexes.reverse()
    const values = slide(indexes.map(i => cells.value[i]))
    indexes.forEach((index, i) => next[index] = values[i])
  }
  cells.value = next
  if (cells.value.join() !== before) addTile()
  over.value = !cells.value.some(n => !n) && !cells.value.some((n, i) => (i % 4 < 3 && n === cells.value[i + 1]) || (i < 12 && n === cells.value[i + 4]))
}
function key(event: KeyboardEvent) {
  const direction = ({ ArrowLeft: 'left', ArrowRight: 'right', ArrowUp: 'up', ArrowDown: 'down' } as const)[event.key as 'ArrowLeft']
  if (direction) { event.preventDefault(); move(direction) }
}
const message = computed(() => cells.value.includes(2048) ? '已达成 2048！' : over.value ? '没有可移动方块' : '方向键或按钮移动')
onMounted(() => window.addEventListener('keydown', key))
onBeforeUnmount(() => window.removeEventListener('keydown', key))
reset()
</script>

<template>
  <div class="game">
    <header><div><b>2048</b><span>{{ message }}</span></div><div class="score">分数<br><strong>{{ score }}</strong></div><button @click="reset">新游戏</button></header>
    <div class="board"><div v-for="(cell, i) in cells" :key="i" :class="`n-${cell}`">{{ cell || '' }}</div></div>
    <div class="controls"><button @click="move('left')">←</button><button @click="move('up')">↑</button><button @click="move('down')">↓</button><button @click="move('right')">→</button></div>
  </div>
</template>

<style scoped>
.game { min-height: 100%; display: grid; align-content: center; justify-items: center; gap: 16px; padding: 20px; color: #443b55; background: linear-gradient(145deg, #fff8e9, #f4d9ad); }
header { width: min(100%, 390px); display: flex; align-items: center; gap: 12px; }
header > div:first-child { flex: 1; display: grid; }
header b { font-size: 35px; color: #765f43; }
header span { font-size: 12px; color: #8a745c; }
.score { padding: 6px 13px; border-radius: 8px; color: #fff; background: #a88e70; font-size: 10px; text-align: center; }
header button, .controls button { border: 0; border-radius: 8px; padding: 9px 12px; color: #fff; background: #8c7052; cursor: pointer; }
.board { display: grid; grid-template-columns: repeat(4, 82px); gap: 9px; padding: 10px; border-radius: 12px; background: #b9a68e; }
.board div { width: 82px; height: 82px; display: grid; place-items: center; border-radius: 7px; color: #735f49; background: #d8cbbb; font-size: 28px; font-weight: 800; }
.board .n-2 { background: #f1e6d5; }.board .n-4 { background: #ecd8b5; }.board .n-8 { color: #fff; background: #e9a66b; }.board .n-16 { color: #fff; background: #e4875e; }.board .n-32 { color: #fff; background: #db6554; }.board .n-64 { color: #fff; background: #c94739; }.board .n-128,.board .n-256,.board .n-512,.board .n-1024,.board .n-2048 { color: #fff; background: #d6ad39; font-size: 23px; }
.controls { display: flex; gap: 6px; }
.controls button { min-width: 45px; font-size: 18px; }
</style>
