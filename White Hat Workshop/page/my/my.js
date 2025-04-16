Page({
  data: {
    userInfo: null,
    isLogin: false,
  },

  onLoad() {
    // 页面首次加载时不做操作
  },

  onShow() {
    // 每次页面显示时重新获取缓存中的数据
    this.refreshData();
    console.log(this.data.userInfo)
  },

  refreshData() {
    const userInfo = wx.getStorageSync('userInfo');

    if (userInfo) {
      this.setData({
        userInfo: userInfo,
        isLogin: true,
      });
    } else {
      this.setData({
        isLogin: false,
      });
    }
  },

  goLogin() {
    wx.navigateTo({
      url: '/page/login/login'
    });
  },

  exit() {
    // 清除用户信息并更新页面
    wx.setStorageSync('userInfo', null);  // 清除缓存中的用户信息
    this.setData({
      userInfo: null,  // 更新页面数据，清空 userInfo
      isLogin: false   // 根据需要，可能还需要设置 isLogin 为 false
    });
    this.refreshData();  // 刷新页面数据
  },
  
  gotomyinfo(){
    wx.navigateTo({
      url: '/page/myinfo/myinfo',
    })
  },
  gotoreword(){
    wx.navigateTo({
      url: '/page/reword/reword',
    })
  },
});
