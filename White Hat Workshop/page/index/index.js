// page/index/index.js
Page({
  /**
   * 页面的初始数据
   */
  data: {
    TabCur: 0,
    scrollLeft:0,//上面的导航栏
    post:null,
    showPost:null,
    nav:["全部","安全漏洞讨论","网络攻击案例","网络工具推荐","行业动态"]
  },

  tabSelect(e) {
    console.log(this.data.post)
    this.setData({
      TabCur: e.currentTarget.dataset.id,
      scrollLeft: (e.currentTarget.dataset.id-1)*60
    })
    this.selectNav(this.data.nav[e.currentTarget.dataset.id])
    console.log(this.data.nav[e.currentTarget.dataset.id])

  },
  selectNav(navName){
    let that = this;
    if(navName==='全部'){
      this.setData({
        showPost:this.data.post
      })
      return ;
    }
    if (!that.data.post || that.data.post.length === 0) {
      wx.showToast({
        title: '没有帖子数据',
        icon: 'none',
      });
      return;
    }
    let filteredPosts = that.data.post.filter(post => post.section === navName);
    this.setData({
      showPost: filteredPosts
    });
    console.log(this.data.showPost);
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
    // 发送 GET 请求，获取帖子
    wx.request({
      // ip一个是[54.153.18.78]//一个47.117.70.79
      url: 'http://wacyg.fun/post/findAll', // 接口地址
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用用户 token进行认证
      },
      success(res) { 
        if (res.statusCode === 200) {
          that.setData({
            post:res.data.data,
            showPost:res.data.data
          })
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none',
          },() => {
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
  gotoessay(e){
    console.log(e.currentTarget.dataset.c.id)
    wx.navigateTo({
      url: '../essay/essay?c='+ e.currentTarget.dataset.c.id ,
    })
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
  gotocheatai(){
    wx.navigateTo({
      url: '../cheatai/cheatai',
    })
  },
  gotosendPost(){
    wx.navigateTo({
      url: '../sendPost/sendPost',
    })
  }
})