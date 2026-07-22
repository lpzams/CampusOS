<script>
// 应用级生命周期（整个小程序启动/切前后台），页面逻辑不要写在这里。
export default {
  onLaunch() {
    console.log('CampusOS 小程序启动')
  },
  onShow() {},
  onHide() {},
}
</script>

<style>
page {
  min-height: 100%;
  background: linear-gradient(180deg, #f6f2fd 0%, #fbf6fb 58%, #fff8f4 100%);
  font-size: 28rpx;
  color: #2c2350;
}

view, text, input, textarea, button, image, scroll-view, picker {
  box-sizing: border-box;
}

.screen { min-height: 100vh; padding: 28rpx 24rpx 56rpx; }
.panel { margin-bottom: 24rpx; padding: 30rpx; border: 1rpx solid rgba(220,210,247,.78); border-radius: 24rpx; background: rgba(255,255,255,.94); box-shadow: 0 12rpx 36rpx rgba(79,54,145,.08); }
.section-title { margin-bottom: 24rpx; color: #2c2350; font-size: 32rpx; font-weight: 700; letter-spacing: 1rpx; }
.muted { color: #9a92b8; }
.field { width: 100%; height: 88rpx; margin-bottom: 20rpx; padding: 0 26rpx; border: 1rpx solid #e3daf7; border-radius: 18rpx; background: #fbf9ff; color: #2c2350; transition: border-color .2s, background .2s; }
.textarea { width: 100%; min-height: 190rpx; margin-bottom: 20rpx; padding: 24rpx 26rpx; border: 1rpx solid #e3daf7; border-radius: 18rpx; background: #fbf9ff; color: #2c2350; }
.primary-btn, .secondary-btn, .danger-btn { height: 84rpx; border-radius: 18rpx; font-size: 28rpx; font-weight: 600; line-height: 84rpx; }
.primary-btn { background: linear-gradient(120deg, #7c5cd6, #b06fd0); color: #fff; box-shadow: 0 8rpx 20rpx rgba(124,92,214,.24); }
.secondary-btn { border: 1rpx solid #dcd2f7; background: #f8f5fd; color: #6247ab; }
.danger-btn { border: 1rpx solid #f56c6c; background: #fff; color: #f56c6c; }
.primary-btn::after, .secondary-btn::after, .danger-btn::after { border: 0; }
.row { display: flex; align-items: center; gap: 16rpx; }
.row > .primary-btn, .row > .secondary-btn, .row > .danger-btn { flex: 1; }
.tabs { display: flex; margin-bottom: 24rpx; padding: 8rpx; overflow-x: auto; border: 1rpx solid #ece4fb; border-radius: 20rpx; background: rgba(255,255,255,.9); white-space: nowrap; box-shadow: 0 8rpx 28rpx rgba(124,92,214,.06); }
.tab { flex: 1; min-width: 130rpx; padding: 20rpx 18rpx; border-radius: 15rpx; text-align: center; color: #6a628c; }
.tab.active { background: #f0eafa; color: #6247ab; font-weight: 700; box-shadow: none; }
.list-item { margin-bottom: 18rpx; padding: 28rpx; border: 1rpx solid #ece4fb; border-radius: 22rpx; background: rgba(255,255,255,.96); box-shadow: 0 8rpx 28rpx rgba(124,92,214,.07); }
.item-title { font-size: 30rpx; font-weight: 600; line-height: 1.45; }
.item-meta { display: flex; flex-wrap: wrap; gap: 12rpx 20rpx; margin-top: 12rpx; color: #9a92b8; font-size: 24rpx; }
.empty-state { padding: 140rpx 0; text-align: center; color: #9a92b8; }
button[disabled] { opacity: .58; }
</style>
