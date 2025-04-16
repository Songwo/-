// page/rank/rank.js
Page({
  /**
   * 页面的初始数据
   */
  data: {
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onShow(options) {
    this.get_User(options)
  },

  get_User() {
    let that = this;
    // 获取用户信息的token
    const userInfo = wx.getStorageSync('userInfo');

    if (!userInfo) {
      // 如果没有用户信息，显示提示框
      wx.showModal({
        title: '提示',
        content: '请先登录账号',
        showCancel: false, // 不显示取消按钮
        success(res) {
          if (res.confirm) {
            // 如果点击确定，跳转到登录页面
            wx.navigateTo({
              url: '/page/login/login', // 假设登录页面路径是 /page/my/login
            });
          }
        }
      });
      return;
    }

    // 发送 GET 请求，获取用户排行榜
    wx.request({
      url: 'http://wacyg.fun/user/rank', // 接口地址
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用用户token进行认证
      },
      success(res) {
        if (res.statusCode === 200) {
          console.log(res),
            // 请求成功，更新数据
            // 拼接头像的完整 URL
            res.data.data.forEach(user => {
              user.avatarUrl = `http://wacyg.fun/${user.avatar}`;
            });
          that.setData({
            list: res.data.data, // 假设返回的数据是rank列表
          });
          console.log(that.data.list)
        } else {
          wx.showToast({
            title: '请求失败',
            icon: 'none',
          },() => {
            // 在 setData 完成后打印 list
            console.log(that.data.list);
          });
        }
      },
      fail() {
        wx.showToast({
          title: '网络错误',
          icon: 'none',
        });
      },
    });
  },
  onRefresh: function () {
    //导航条加载动画
    wx.showNavigationBarLoading()
    //loading 提示框
    wx.showLoading({
      title: 'Loading...',
    })
    console.log("下拉刷新啦");
    setTimeout(function () {
      wx.hideLoading();
      wx.hideNavigationBarLoading();
      //停止下拉刷新
      wx.stopPullDownRefresh();
    }, 1000)
  },
  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    this.onRefresh();
  },
})