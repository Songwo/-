Page({
  data: {
    zhanghao: '',
    password: '',
    clientHeight: ''
  },
  // 实时获取输入框的数据
  getzhanghao(e) {
    const zhanghao = e.detail.value; // 获取用户输入的值
    this.setData({
      zhanghao: zhanghao, // 更新 data 中的 zhanghao 值
    });
    console.log('输入的账号是：', this.data.zhanghao); // 打印输入的账号
  },

  getpassword(e) {
    const password = e.detail.value; // 获取用户输入的值
    this.setData({
      password: password, // 更新 data 中的 phone 值
    });
    console.log('输入的密码是：', this.data.password); // 打印输入的账号
  },
  // 登录事件
  goadmin() {
    console.log(this.data.zhanghao)
    console.log(this.data.password)

    if (this.data.zhanghao == '') {
      wx.showToast({
        icon: 'none',
        title: '账号不能为空',
      })
    } else if (this.data.password == '') {
      wx.showToast({
        icon: 'none',
        title: '密码不能为空',
      })
    } else {
      let data = {
        username: this.data.zhanghao,
        password: this.data.password
      }
      wx.request({
        url: 'http://wacyg.fun/user/login',
        method: 'POST',
        data: data, // 传递的数据
        header: {
          'Content-Type': 'application/json', // 设置请求头，如果是 POST 请求通常需要设置
        },
        success: function (res) {
          console.log(res.data)
          // 请求成功后的回调
          if (res.data.code === 200) {
            console.log(res.data); // 打印返回的数据
            // 合并用户信息和 Token 并存储到 userInfo 缓存中
            const userInfo = {
              username: data.username,
              token: res.data.data
            };
            wx.setStorageSync('userInfo', userInfo); // 存储用户名和token到同一个userInfo缓存中
            const username = userInfo.username
            // 调用接口获得用户信息
            // 调用接口获得用户信息
            wx.request({
              url: `http://wacyg.fun/user/mes/${username}`, // 替换为实际的 API URL
              method: 'GET',
              header: {
                'Authorization': `Bearer ${userInfo.token}`, // 使用 Bearer token 作为认证
              },
              success: (res) => {
                console.log(res)
                if (res.statusCode === 200) {
                  console.log('用户信息:', res.data.data.avatar);

                  // 在这里直接更新 userInfo
                  const updatedUserInfo = {
                    id: res.data.data.id,
                    username: data.username,
                    token: userInfo.token, // 保持之前存储的 token
                    avatar: "http://wacyg.fun/" + res.data.data.avatar, // 新的头像 URL
                  };

                  // 存储新的 userInfo
                  wx.setStorageSync('userInfo', updatedUserInfo);
                  // 打印更新后的 userInfo
                  console.log(updatedUserInfo); // 打印更新后的用户信息
                  wx.navigateBack({
                    delta: 1,
                  })

                } else {
                  console.log(res)
                  wx.showToast({
                    title: '获取信息失败',
                    icon: 'none',
                  });
                }
              }
            });;
          } else {
            // 如果请求状态不是 200，显示错误提示
            wx.showToast({
              title: '登录失败',
              icon: 'none',
              duration: 2000,
            });
          }
        },
        fail: function (error) {
          // 请求失败的回调
          console.error('请求失败', error);
          wx.showToast({
            title: '请求失败，请稍后重试',
            icon: 'none',
            duration: 2000,
          });
        },
      })
    }
  },
  register() {
    wx.navigateTo({
      url: './register',
    })
  },
  fanhui() {
    wx.navigateBack()({
      detail: 1
    })
  }
})