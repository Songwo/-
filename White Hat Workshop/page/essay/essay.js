Page({
  /**
   * 页面的初始数据
   */
  data: {
    post: null,
    show: false,
    InputBottom: 0
  },

  onLoad(options) {
    this.setData({
      postid: options.c
    });
    console.log(this.data.postid);
    this.getpost();
    this.getreplyCount();
  },

  getpost() {
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
    // 发送 GET 请求，获取文章
    wx.request({
      url: `http://wacyg.fun/post/findById?id=${that.data.postid}`, // 将 id 作为 URL 查询参数
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用用户token进行认证
      },
      success(res) {
        if (res.statusCode === 200) {
          console.log(res);
          // 设置 post 数据
          that.setData({
            post: res.data.data // 假设后端返回的数据在 res.data 中
          });
          that.setData({
            'post.avatarUrl': `http://47.117.70.79/${res.data.data.avatar}`
          });
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none',
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
  
  getreplyCount(){
    let that = this;
    // 获取用户信息的token
    const userInfo = wx.getStorageSync('userInfo');
    // 发送 GET 请求，获取文章
    wx.request({
      url: `http://47.117.70.79/comments/find/${that.data.postid}`, // 将 id 作为 URL 查询参数
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用用户token进行认证
      },
      success(res) {
        if (res.statusCode === 200) {
          // 设置 replyCounts 数据
          that.setData({
            replyCounts:res.data.data
          });
          console.log(that.data.replyCounts);
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none',
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
  showModal(e) {
    this.setData({
      modalName: e.currentTarget.dataset.target
    })
  },
  hideModal(e) {
    this.setData({
      modalName: null
    })
  },
  
  InputFocus(e) {
    this.setData({
      InputBottom: e.detail.height
    })
  },
  InputBlur(e) {
    this.setData({
      InputBottom: 0
    })
  },
  getInputValue(e){
    console.log(e.detail.value)
    this.setData({
      comment:e.detail.value
    })
  },
  postComment(){
    let that = this;
    const userInfo = wx.getStorageSync('userInfo');
    let data = {
      "authorId":this.data.post.authorId,
      "username":userInfo.username,
      "postId":this.data.postid,
      "content":this.data.comment,
      "avatar":this.data.post.avatar,
      "token":userInfo.token,
      'Authorization': `Bearer ${userInfo.token}`,
    };
    console.log(data)
    wx.request({
      url: `http://47.117.70.79/comments/insert`, // 将 id 作为 URL 查询参数
      method: 'post',
      data:data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, // 使用用户token进行认证
      },
      success(res) {
        if (res.statusCode === 200) {
          // 设置 replyCounts 数据
          console.log(that.data.replyCounts);
        } else {
          wx.showToast({
            title: '加载失败',
            icon: 'none',
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

  }




});