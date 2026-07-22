<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { LOCATION_CATEGORIES, getLocations } from '@/api/location'
import type { LocationItem } from '@/api/location'

const mapElement = ref<HTMLElement | null>(null)
const allLocations = ref<LocationItem[]>([])
const current = ref<LocationItem | null>(null)
const filter = reactive({ category: '', keyword: '' })
const routeForm = reactive({ fromId: null as number | null, toId: null as number | null })
const routing = ref(false)
const routeInfo = ref<{ distance: number; duration: number; fallback: boolean } | null>(null)

let map: any
let markerLayer: any
let routeLayer: any
let resizeObserver: ResizeObserver | undefined
const retriedTiles = new WeakSet<HTMLImageElement>()

const visibleLocations = computed(() => allLocations.value.filter(location => {
  const categoryOk = !filter.category || location.categoryCode === filter.category
  const search = filter.keyword.trim().toLowerCase()
  const keywordOk = !search || `${location.name} ${location.address} ${location.building}`.toLowerCase().includes(search)
  return categoryOk && keywordOk
}))

async function fetchLocations() {
  allLocations.value = await getLocations()
  current.value = allLocations.value[0] ?? null
  routeForm.fromId = current.value?.id ?? null
  syncMarkers()
}

function selectLocation(location: LocationItem) {
  current.value = location
  const lat = Number(location.latitude); const lng = Number(location.longitude)
  if (map && Number.isFinite(lat) && Number.isFinite(lng)) map.flyTo([lat, lng], 17, { duration: .45 })
}

function syncMarkers() {
  if (!map) return
  markerLayer?.clearLayers()
  for (const location of visibleLocations.value) {
    const lat = Number(location.latitude); const lng = Number(location.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    L.circleMarker([lat, lng], { radius: 7, color: '#ffffff', weight: 2, fillColor: '#167f81', fillOpacity: 1 })
      .bindTooltip(location.name, { direction: 'top', offset: [0, -7] })
      .on('click', () => selectLocation(location)).addTo(markerLayer)
  }
}

function chooseCategory(code: string) { filter.category = filter.category === code ? '' : code }
function swapRoute() { [routeForm.fromId, routeForm.toId] = [routeForm.toId, routeForm.fromId] }
function categoryName(location: LocationItem) { return LOCATION_CATEGORIES.find(item => item.code === location.categoryCode)?.name || location.category || '校园地点' }
function distanceMeters(a: LocationItem, b: LocationItem) {
  const r = 6371000; const dLat = (Number(b.latitude) - Number(a.latitude)) * Math.PI / 180; const dLng = (Number(b.longitude) - Number(a.longitude)) * Math.PI / 180
  const s = Math.sin(dLat / 2) ** 2 + Math.cos(Number(a.latitude) * Math.PI / 180) * Math.cos(Number(b.latitude) * Math.PI / 180) * Math.sin(dLng / 2) ** 2
  return Math.round(2 * r * Math.asin(Math.sqrt(s)))
}

async function planOnMap() {
  const from = allLocations.value.find(location => location.id === routeForm.fromId)
  const to = allLocations.value.find(location => location.id === routeForm.toId)
  if (!from || !to) return ElMessage.warning('请选择起点和终点')
  if (from.id === to.id) return ElMessage.warning('起点和终点不能相同')
  routing.value = true; routeLayer?.clearLayers()
  const start: [number, number] = [Number(from.longitude), Number(from.latitude)]
  const end: [number, number] = [Number(to.longitude), Number(to.latitude)]
  try {
    const response = await fetch(`https://router.project-osrm.org/route/v1/driving/${start.join(',')};${end.join(',')}?overview=full&geometries=geojson`)
    if (!response.ok) throw new Error('route unavailable')
    const result = await response.json(); const route = result.routes?.[0]
    if (!route?.geometry?.coordinates?.length) throw new Error('empty route')
    const points = route.geometry.coordinates.map(([lng, lat]: [number, number]) => [lat, lng])
    L.polyline(points, { color: '#f05b4f', weight: 6, opacity: .9 }).addTo(routeLayer)
    routeInfo.value = { distance: Math.round(route.distance), duration: Math.max(1, Math.round(route.duration / 60)), fallback: false }
    map.fitBounds(routeLayer.getBounds(), { padding: [46, 46] })
  } catch {
    const points: [number, number][] = [[Number(from.latitude), Number(from.longitude)], [Number(to.latitude), Number(to.longitude)]]
    L.polyline(points, { color: '#f0a34f', weight: 5, dashArray: '8 8' }).addTo(routeLayer)
    routeInfo.value = { distance: distanceMeters(from, to), duration: Math.max(1, Math.ceil(distanceMeters(from, to) / 75)), fallback: true }
    map.fitBounds(routeLayer.getBounds(), { padding: [46, 46] })
    ElMessage.warning('路径服务暂不可用，已显示两点连线')
  } finally { routing.value = false }
}

watch(visibleLocations, locations => {
  if (current.value && !locations.some(item => item.id === current.value?.id)) current.value = locations[0] ?? null
  syncMarkers()
})

onMounted(async () => {
  try {
    await nextTick()
    map = L.map(mapElement.value!, { zoomControl: true }).setView([30.7661, 103.9845], 15)
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      keepBuffer: 4,
      attribution: '&copy; OpenStreetMap contributors',
    }).on('tileerror', ({ tile }: { tile: HTMLImageElement }) => {
      if (retriedTiles.has(tile)) return
      retriedTiles.add(tile)
      window.setTimeout(() => { tile.src = `${tile.src}${tile.src.includes('?') ? '&' : '?'}retry=1` }, 500)
    }).addTo(map)
    markerLayer = L.layerGroup().addTo(map); routeLayer = L.layerGroup().addTo(map)
    resizeObserver = new ResizeObserver(() => map.invalidateSize({ pan: false }))
    resizeObserver.observe(mapElement.value!)
    map.whenReady(() => map.invalidateSize({ pan: false }))
    await fetchLocations()
  } catch (error) { ElMessage.error((error as Error).message || '地图加载失败') }
})
onBeforeUnmount(() => { resizeObserver?.disconnect(); map?.remove() })
</script>

<template>
  <div class="campus-map-page">
    <header class="map-heading">
      <div>
        <span class="eyebrow">CAMPUS NAVIGATION</span>
        <h1>犀浦校区地图</h1>
        <p>搜索校内地点，快速规划到达路线</p>
      </div>
      <div class="map-status"><i /> 西南交通大学 · 犀浦校区</div>
    </header>

    <section class="map-shell">
      <aside class="map-panel">
        <div class="search-box">
          <el-input v-model="filter.keyword" placeholder="搜索教学楼、食堂、宿舍…" clearable size="large" />
        </div>
        <div class="filters">
          <button :class="{ active: !filter.category }" @click="filter.category = ''">全部</button>
          <button v-for="category in LOCATION_CATEGORIES" :key="category.code" :class="{ active: filter.category === category.code }" @click="chooseCategory(category.code)">{{ category.name }}</button>
        </div>
        <div class="list-heading"><b>校内地点</b><span>{{ visibleLocations.length }} 个结果</span></div>
        <div class="location-list">
          <button v-for="location in visibleLocations" :key="location.id" :class="{ active: current?.id === location.id }" @click="selectLocation(location)">
            <span class="location-mark">{{ location.name.slice(0, 1) }}</span>
            <span class="location-copy"><b>{{ location.name }}</b><small>{{ location.address || location.building || '犀浦校区' }}</small></span>
            <em>{{ categoryName(location) }}</em>
          </button>
          <div v-if="!visibleLocations.length" class="empty-state">没有找到匹配的地点</div>
        </div>
      </aside>

      <div class="map-stage">
        <div ref="mapElement" class="map-canvas" />
        <div v-if="current" class="place-card">
          <span>{{ categoryName(current) }}</span>
          <b>{{ current.name }}</b>
          <small>{{ current.address || current.building || '西南交通大学犀浦校区' }}</small>
          <button @click="routeForm.toId = current.id">设为终点</button>
        </div>
        <div class="route-card">
          <div class="route-title">
            <div><b>路线规划</b><small>选择起点和终点</small></div>
            <div v-if="routeInfo" class="route-info"><b>{{ routeInfo.distance }} 米</b><span>约 {{ routeInfo.duration }} 分钟</span><small v-if="routeInfo.fallback">直线参考</small></div>
          </div>
          <div class="route-fields">
            <el-select v-model="routeForm.fromId" placeholder="选择起点" filterable><el-option v-for="location in allLocations" :key="location.id" :label="location.name" :value="location.id" /></el-select>
            <button class="swap-button" title="交换起终点" aria-label="交换起终点" @click="swapRoute">⇄</button>
            <el-select v-model="routeForm.toId" placeholder="选择终点" filterable><el-option v-for="location in allLocations" :key="location.id" :label="location.name" :value="location.id" /></el-select>
            <el-button type="primary" :loading="routing" @click="planOnMap">开始导航</el-button>
          </div>
        </div>
      </div>
    </section>
    <p class="map-credit">地图数据 © OpenStreetMap contributors · 路线由 OSRM 提供</p>
  </div>
</template>

<style scoped>
.campus-map-page { display: grid; gap: 18px; color: #2d2940; }
.map-heading { display: flex; align-items: end; justify-content: space-between; padding: 4px 2px; }
.map-heading h1 { margin: 6px 0 5px; font-size: clamp(26px, 3vw, 34px); line-height: 1.1; letter-spacing: -.5px; }
.map-heading p { margin: 0; color: #89849b; font-size: 14px; }
.eyebrow { color: #7c5cd6; font-size: 11px; font-weight: 800; letter-spacing: 2px; }
.map-status { padding: 9px 14px; border: 1px solid #e8e0f6; border-radius: 999px; color: #625d74; background: #fff; font-size: 12px; box-shadow: 0 5px 18px #7c5cd610; }
.map-status i { display: inline-block; width: 7px; height: 7px; margin-right: 6px; border-radius: 50%; background: #46b68d; box-shadow: 0 0 0 4px #46b68d1c; }
.map-shell { display: grid; grid-template-columns: 320px minmax(0, 1fr); height: min(680px, calc(100vh - 210px)); min-height: 540px; overflow: hidden; border: 1px solid #e7e0f1; border-radius: 18px; background: #fff; box-shadow: 0 18px 50px #493a7317; }
.map-panel { display: grid; grid-template-rows: auto auto auto minmax(0, 1fr); min-width: 0; padding: 18px 14px 14px; border-right: 1px solid #ece6f4; overflow: hidden; }
.search-box { padding: 0 2px 14px; }
.search-box :deep(.el-input__wrapper) { border-radius: 11px; background: #f7f5fb; box-shadow: 0 0 0 1px #eee8f6 inset; }
.filters { display: flex; gap: 7px; padding: 0 2px 16px; overflow-x: auto; scrollbar-width: none; }
.filters button { flex: 0 0 auto; padding: 6px 11px; border: 1px solid #e7e0f1; border-radius: 999px; color: #777186; background: #fff; font-size: 12px; cursor: pointer; }
.filters button.active { border-color: #7c5cd6; color: #fff; background: #7c5cd6; box-shadow: 0 5px 12px #7c5cd630; }
.list-heading { display: flex; justify-content: space-between; padding: 12px 4px 10px; border-top: 1px solid #f0ecf5; font-size: 13px; }
.list-heading span { color: #aaa5b5; font-size: 11px; }
.location-list { display: grid; align-content: start; gap: 7px; padding: 2px 2px 8px; overflow-y: auto; }
.location-list > button { display: grid; grid-template-columns: 36px minmax(0, 1fr) auto; align-items: center; gap: 10px; width: 100%; padding: 10px; border: 1px solid transparent; border-radius: 11px; color: #332e45; background: transparent; text-align: left; cursor: pointer; transition: .18s ease; }
.location-list > button:hover { background: #f8f6fb; }
.location-list > button.active { border-color: #ded2f3; background: #f3eefc; }
.location-mark { display: grid; width: 36px; height: 36px; place-items: center; border-radius: 10px; color: #7554c8; background: #eae3f8; font-size: 13px; font-weight: 800; }
.location-copy { display: grid; gap: 4px; min-width: 0; }
.location-copy b, .location-copy small { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.location-copy b { font-size: 13px; }.location-copy small { color: #9791a5; font-size: 11px; }
.location-list em { padding: 3px 6px; border-radius: 5px; color: #847c95; background: #f2eff6; font-size: 10px; font-style: normal; }
.empty-state { padding: 42px 10px; color: #aaa5b5; font-size: 13px; text-align: center; }
.map-stage { position: relative; min-width: 0; overflow: hidden; }
.map-canvas { width: 100%; height: 100%; background: #e9e8e5; }
.place-card { position: absolute; z-index: 500; top: 18px; right: 18px; display: grid; gap: 5px; width: min(230px, calc(100% - 36px)); padding: 15px; border: 1px solid #ffffffc7; border-radius: 14px; background: #ffffffed; box-shadow: 0 12px 32px #392d5b24; backdrop-filter: blur(10px); }
.place-card > span { color: #7c5cd6; font-size: 10px; font-weight: 800; letter-spacing: 1px; }.place-card > b { font-size: 16px; }.place-card > small { overflow: hidden; color: #8a8498; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.place-card button { justify-self: start; margin-top: 4px; padding: 5px 10px; border: 0; border-radius: 7px; color: #fff; background: #7c5cd6; font-size: 11px; cursor: pointer; }
.route-card { position: absolute; z-index: 500; right: 18px; bottom: 18px; left: 18px; padding: 14px 16px 16px; border: 1px solid #ffffffc7; border-radius: 15px; background: #ffffffed; box-shadow: 0 12px 34px #392d5b28; backdrop-filter: blur(12px); }
.route-title { display: flex; align-items: center; justify-content: space-between; margin-bottom: 11px; }.route-title > div:first-child { display: grid; gap: 2px; }.route-title b { font-size: 14px; }.route-title small { color: #9993a5; font-size: 10px; }
.route-fields { display: grid; grid-template-columns: minmax(0, 1fr) 30px minmax(0, 1fr) auto; align-items: center; gap: 8px; }
.route-fields :deep(.el-select) { width: 100%; }.route-fields :deep(.el-select__wrapper) { border-radius: 9px; }
.swap-button { width: 30px; height: 30px; padding: 0; border: 1px solid #e6dff0; border-radius: 50%; color: #7c5cd6; background: #fff; cursor: pointer; }
.route-info { display: flex; align-items: baseline; gap: 7px; color: #6f687d; }.route-info b { color: #7c5cd6; font-size: 15px; }.route-info span { font-size: 11px; }.route-info small { color: #b18136; }
.map-credit { margin: -8px 4px 0; color: #aaa5b5; font-size: 10px; text-align: right; }
:global(.leaflet-container) { font-family: Inter, "PingFang SC", "Microsoft YaHei", sans-serif; }
:global(.leaflet-control-attribution) { display: none; }
:global(.leaflet-control-zoom) { overflow: hidden; border: 0 !important; border-radius: 10px !important; box-shadow: 0 5px 18px #342a4d26 !important; }
@media (max-width: 800px) {
  .map-heading { align-items: start; flex-direction: column; gap: 12px; }.map-status { display: none; }
  .map-shell { grid-template-columns: 1fr; height: auto; }.map-panel { max-height: 340px; border-right: 0; border-bottom: 1px solid #ece6f4; }.map-stage { height: 540px; }
  .route-fields { grid-template-columns: minmax(0, 1fr) 28px minmax(0, 1fr); }.route-fields :deep(.el-button) { grid-column: 1 / -1; }.route-info { display: none; }
}
@media (max-width: 520px) { .map-stage { height: 500px; }.place-card { display: none; }.route-card { right: 10px; bottom: 10px; left: 10px; }.route-title { margin-bottom: 8px; } }
</style>
