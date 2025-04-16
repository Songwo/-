Page({

  /**
   * 页面的初始数据
   */
  data: {
    // 默认是0，0是训练
    TabCur: 0,
    scrollLeft: 0,
    challengeData: null,  // 用来保存获取的挑战数据
    challengeList: [
      '网络安全基础',
      '攻击与防御',
      '加密与身份认证',
      '网络协议安全类',
      '安全工具',
      '法律与合规',

    ]     // 用来保存所有的挑战项
  },

  // Tab选择切换
  tabSelect(e) {
    this.setData({
      TabCur: e.currentTarget.dataset.id,
      scrollLeft: e.currentTarget.dataset.id - 1
    });
    console.log(this.data.TabCur)
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onShow(options) {
    // 获取挑战数据
    this.get_challenge();
  },

  get_challenge() {
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
    // 调用 GET 请求获取挑战数据
    wx.request({
      url: `http://wacyg.fun/api/get_challenge`, // 替换为实际的 API URL
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用 Bearer token 作为认证
      },
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('题目类型:', res.data.data);
          
          // 提取挑战数据并处理
          const challengeData = res.data.data;
          const challengeList = [];

          // 将challenge1到challenge10添加到数组中
          for (let i = 1; i <= 10; i++) {
            const challengeKey = `challengel${i}`;
            if (challengeData[challengeKey]) {
              challengeList.push(challengeData[challengeKey]);
            }
          }

          // 更新页面数据
          that.setData({
            challengeList: challengeList, // 将有效的挑战数据设置到页面
          });
          console.log(this.data.challengeList)
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

  // 处理问题选择
  Question(e) {
    const index = e.currentTarget.dataset.index; 
    console.log(index);
    wx.navigateTo({
      url: './Question?c=' + e.currentTarget.dataset.c + '&t=' + this.data.TabCur +'&index=' + index, // c: 选择的题目类型，t: 0 为训练，1 为考试
    });
  }
});