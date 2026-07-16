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
/* 全局样式：每个页面都会带上。小程序里 page 相当于网页的 body。 */
page {
  background-color: #f5f6fa;
  font-size: 28rpx;
  color: #303133;
}
</style>
