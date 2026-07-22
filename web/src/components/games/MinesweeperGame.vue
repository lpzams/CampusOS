<script setup lang="ts">
import { computed, ref } from 'vue'

type Cell = { mine: boolean; open: boolean; flag: boolean; around: number }
const size = 9
const mineCount = 10
const board = ref<Cell[]>([])
const state = ref<'playing' | 'won' | 'lost'>('playing')
const started = ref(false)

const neighbors = (index: number) => {
  const row = Math.floor(index / size), col = index % size
  const result: number[] = []
  for (let dr = -1; dr <= 1; dr++) for (let dc = -1; dc <= 1; dc++) {
    const r = row + dr, c = col + dc
    if ((dr || dc) && r >= 0 && r < size && c >= 0 && c < size) result.push(r * size + c)
  }
  return result
}

function reset() {
  board.value = Array.from({ length: size * size }, () => ({ mine: false, open: false, flag: false, around: 0 }))
  state.value = 'playing'
  started.value = false
}

function plant(first: number) {
  const safe = new Set([first, ...neighbors(first)])
  const spots = board.value.map((_, i) => i).filter(i => !safe.has(i))
  for (let i = spots.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1)); [spots[i], spots[j]] = [spots[j], spots[i]]
  }
  spots.slice(0, mineCount).forEach(i => board.value[i].mine = true)
  board.value.forEach((cell, i) => cell.around = neighbors(i).filter(n => board.value[n].mine).length)
  started.value = true
}

function open(index: number) {
  if (state.value !== 'playing' || board.value[index].flag || board.value[index].open) return
  if (!started.value) plant(index)
  const cell = board.value[index]
  cell.open = true
  if (cell.mine) {
    state.value = 'lost'
    board.value.forEach(item => { if (item.mine) item.open = true })
    return
  }
  if (!cell.around) neighbors(index).forEach(open)
  if (board.value.filter(item => item.open).length === size * size - mineCount) state.value = 'won'
}

function flag(index: number) {
  if (state.value === 'playing' && !board.value[index].open) board.value[index].flag = !board.value[index].flag
}

const flagsLeft = computed(() => mineCount - board.value.filter(cell => cell.flag).length)
reset()
</script>

<template>
  <div class="game">
    <header><b>💣 {{ flagsLeft }}</b><span>{{ state === 'won' ? '🎉 你赢了' : state === 'lost' ? '💥 游戏结束' : '扫雷' }}</span><button @click="reset">重新开始</button></header>
    <div class="board">
      <button v-for="(cell, i) in board" :key="i" :class="{ open: cell.open, mine: cell.open && cell.mine }" @click="open(i)" @contextmenu.prevent="flag(i)">
        {{ cell.flag ? '🚩' : cell.open && cell.mine ? '💣' : cell.open && cell.around ? cell.around : '' }}
      </button>
    </div>
    <small>左键翻开，右键插旗；第一次点击一定安全。</small>
  </div>
</template>

<style scoped>
.game { min-height: 100%; display: grid; align-content: center; justify-items: center; gap: 14px; padding: 20px; color: #302951; background: linear-gradient(145deg, #f7f3ff, #e9e0ff); }
header { width: min(100%, 390px); display: flex; align-items: center; justify-content: space-between; }
header span { font-size: 18px; font-weight: 800; }
header button { border: 0; border-radius: 8px; padding: 7px 11px; color: #fff; background: #7659c7; cursor: pointer; }
.board { display: grid; grid-template-columns: repeat(9, 38px); gap: 3px; padding: 9px; border-radius: 12px; background: #afa1d0; box-shadow: 0 12px 35px #4d377d33; }
.board button { width: 38px; height: 38px; border: 0; border-radius: 5px; color: #57428e; background: #faf8ff; font-weight: 800; cursor: pointer; box-shadow: inset 0 -3px #d5caee; }
.board button.open { background: #e4def0; box-shadow: none; }
.board button.mine { background: #ff9eae; }
small { color: #7f7594; }
</style>
