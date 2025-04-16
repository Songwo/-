Page({
  data: {
    isEditing: false, // 用于控制是否可以编辑
    userInfo: null,  // 保存用户信息
    isLogin: false,
    avatar:null,
    accuracy: null,  // 保存准确率
  },

  onLoad() {
    // 页面首次加载时不做操作
  },

  onShow() {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo || !userInfo.token) {
      wx.showToast({
        title: '请重新登录',
        icon: 'none'
      });
      return; // 不继续执行请求
    }
    this.setData({ userInfo:userInfo });  // 将 userInfo 保存在 data 中
    this.getuserinfo();
  },
  toEdit(){
    this.setData({
      isEditing:true,
    })
  },
  outEdit(){
    this.setData({
      isEditing:false,
    })
  },
  getuserinfo() {
    const userInfo = this.data.userInfo;  // 从 data 中获取 userInfo
    const username = userInfo ? userInfo.username : null;  // 防止 null 值
    if (!username) {
      wx.showToast({
        title: '请先登录账号',
        icon: 'none'
      });
      return;
    }
    // 调用 GET 请求获取用户信息
    wx.request({
      url: `http://wacyg.fun/user/mes/${username}`, // 替换为实际的 API URL
      method: 'GET',
      header: {
        'Authorization': `Bearer ${userInfo.token}`, // 使用 Bearer token 作为认证
      },
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('用户信息:', res.data.data);
          this.setData({
            info: res.data.data, // 将返回的数据设置到页面
          });

          // 计算准确率
          const correctCount = res.data.data.correctCount; // 获取正确答案数量
          const totalQuestions = res.data.data.completedQuestions.length; // 获取完成的题目数量
          
          if (totalQuestions > 0) {
            const accuracy = (correctCount / totalQuestions * 100).toFixed(2); // 计算准确率并保留两位小数
            this.setData({
              accuracy: accuracy // 设置准确率
            });
          } else {
            this.setData({
              accuracy: '0.00' // 如果没有完成题目，则准确率为 0
            });
          }

          console.log("准确率:", this.data.accuracy);
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

  // 读取input
  getemail(e) {
    const email = e.detail.value; // 获取用户输入的值
    this.setData({
      email: email, // 更新 data 中的 email 值
    });
    console.log('输入的邮箱是：', this.data.email); // 打印输入的邮箱
  },

  // 取消修改
  qxxg() {
    wx.navigateBack({
     delta: 1,
    })
  },

  // 确定修改
  qdxg() {
    const userInfo = this.data.userInfo; // 从 data 中获取当前用户信息
    if (!userInfo || !userInfo.token) {
      wx.showToast({
        title: '请重新登录',
        icon: 'none'
      });
      return; // 如果没有用户信息或token，返回
    }

    const updatedUserInfo = {
      username: userInfo.username, // 从 userInfo 中获取用户名
      email: this.data.email, // 从 data 中获取 email
      id:this.data.info.id
    };
    console.log(updatedUserInfo)
    wx.request({
      url: 'http://wacyg.fun/user/update', // 替换为实际的 API URL
      method: 'PUT',
      header: {
        'Authorization': `Bearer ${userInfo.token}`, // 使用 Bearer token 作为认证
      },
      data: updatedUserInfo, // 将更新的用户信息作为请求体发送
      success: (res) => {
        console.log("fanhuid xiaos", res)
        if (res.statusCode === 200) {
          wx.showToast({
            title: '用户信息更新成功',
            icon: 'success'
          });
          // 这里可以更新页面上的用户信息显示
        } else {
          wx.showToast({
            title: '更新失败: ' + res.data.message,
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
      },
    });
    this.setData({
      isEditing:false,
    })
  }
});
