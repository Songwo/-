Page({
  data: {
    hidden:false,
    unlockedRewards:null,
    count:null,
    showReword:[],
  },
  onLoad(options) {
    this.getinfo();
  },
  getinfo(){
    const userInfo = wx.getStorageSync('userInfo');
    console.log(userInfo.token)
    wx.request({
      url: `http://wacyg.fun/user/reward`, 
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用 Bearer token 作为认证
      },
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('奖励:', res.data.data.unlockedRewards);
          this.setData({
            unlockedRewards:res.data.data.unlockedRewards,
            count:res.data.data.unlockedRewards.length,
          })
          console.log(this.data.unlockedRewards)
          console.log(this.data.count)
        } else {
          wx.showToast({
            title: '获取信息失败',
            icon: 'none',
          });
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        wx.showToast({
          title: '网络请求失败',
          icon: 'none',
        });
      },
    });
  },
  showRewordChange(e) {
    const index = e.currentTarget.dataset.index;  // 获取点击的奖励项的索引
    const showReword = this.data.showReword;
    // 切换当前项的显示状态
    showReword[index] = !showReword[index];
    this.setData({
      showReword: showReword,
    });
  },
})