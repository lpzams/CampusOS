<script setup lang="ts">
defineProps<{
  eyebrow: string
  title: string
  description: string
  icon: string
  tone?: 'violet' | 'blue' | 'orange' | 'green'
  metrics?: Array<{ label: string; value: string | number; hint?: string }>
}>()
</script>

<template>
  <section class="service-hero" :class="`tone-${tone || 'violet'}`">
    <div class="hero-copy">
      <span class="eyebrow">{{ eyebrow }}</span>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
      <div v-if="$slots.actions" class="hero-actions"><slot name="actions" /></div>
    </div>
    <div class="hero-side">
      <div class="hero-icon" aria-hidden="true">{{ icon }}</div>
      <div v-if="metrics?.length" class="metrics">
        <div v-for="metric in metrics" :key="metric.label" class="metric">
          <strong>{{ metric.value }}</strong>
          <span>{{ metric.label }}</span>
          <small v-if="metric.hint">{{ metric.hint }}</small>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.service-hero { --hero: #7c5cd6; --hero-dark: #51329d; position: relative; display: grid; grid-template-columns: 1.35fr 1fr; gap: 30px; overflow: hidden; padding: 30px 34px; margin-bottom: 18px; color: #fff; border-radius: 22px; background: linear-gradient(125deg, var(--hero-dark), var(--hero)); box-shadow: 0 18px 40px color-mix(in srgb, var(--hero) 25%, transparent); }
.service-hero::before, .service-hero::after { content: ''; position: absolute; border-radius: 50%; background: #ffffff12; }
.service-hero::before { width: 260px; height: 260px; right: -90px; top: -150px; }
.service-hero::after { width: 150px; height: 150px; left: 45%; bottom: -115px; }
.tone-blue { --hero: #4b82e8; --hero-dark: #2454a6; }
.tone-orange { --hero: #ef9a52; --hero-dark: #c35b45; }
.tone-green { --hero: #42a987; --hero-dark: #217260; }
.hero-copy, .hero-side { position: relative; z-index: 1; }
.eyebrow { font-size: 12px; letter-spacing: 3px; opacity: .75; }
h1 { margin: 7px 0 8px; font-size: 28px; line-height: 1.2; }
p { max-width: 610px; margin: 0; line-height: 1.7; color: #ffffffd9; font-size: 14px; }
.hero-actions { display: flex; gap: 10px; margin-top: 20px; }
.hero-actions :deep(.el-button) { border: 0; }
.hero-side { display: flex; flex-direction: column; align-items: flex-end; justify-content: space-between; gap: 18px; }
.hero-icon { font-size: 50px; line-height: 1; filter: drop-shadow(0 8px 12px #0002); }
.metrics { width: 100%; display: grid; grid-template-columns: repeat(auto-fit, minmax(90px, 1fr)); gap: 8px; }
.metric { padding: 11px 12px; border: 1px solid #ffffff20; border-radius: 12px; background: #ffffff12; backdrop-filter: blur(8px); }
.metric strong, .metric span, .metric small { display: block; }
.metric strong { font-size: 19px; }
.metric span { margin-top: 2px; font-size: 12px; opacity: .88; }
.metric small { margin-top: 2px; font-size: 10px; opacity: .62; }
@media (max-width: 700px) { .service-hero { grid-template-columns: 1fr; padding: 24px; } .hero-side { align-items: flex-start; } .hero-icon { display: none; } h1 { font-size: 24px; } }
</style>
