Page({
  data: {
    zhanghao: '',
    password: '',
    clientHeight: '',
    email:''
  },
  // 实时获取输入框的数据
  getzhanghao(e) {
    const zhanghao = e.detail.value; // 获取用户输入的值
    this.setData({
      zhanghao: zhanghao, // 更新 data 中的 zhanghao 值
    });
    console.log('输入的账号是：', this.data.zhanghao); // 打印输入的账号
  },

  getemail(e){
    const email = e.detail.value; // 获取用户输入的值
    this.setData({
      email: email, // 更新 data 中的 zhanghao 值
    });
    console.log('输入的账号是：', this.data.email); // 打印输入的账号
  },

  getpassword(e) {
    const password = e.detail.value; // 获取用户输入的值
    this.setData({
      password: password, // 更新 data 中的 phone 值
    });
    console.log('输入的密码是：', this.data.password); // 打印输入的账号
  },
  //登录事件
  goadmin() {
    console.log(this.data.zhanghao)
    console.log(this.data.password)
    console.log(this.data.email)
    if (this.data.zhanghao == '') {
      wx.showToast({
        icon: 'none',
        title: '账号不能为空',
      })
      return
    } 
    if (this.data.password == '') {
      wx.showToast({
        icon: 'none',
        title: '密码不能为空',
      })
      return
    } 
    if (this.data.email == '') {
      wx.showToast({
        icon: 'none',
        title: '邮箱不能为空',
      })
      return
    } 
      console.log(this.data.password)
      console.log(this.data.zhanghao)
      let data = {
        username: this.data.zhanghao,
        password: this.data.password,
        email:this.data.email
      }
      wx.request({
        url: 'http://wacyg.fun/user/register', // 替换为你的 API 地址
        method: 'POST',
        data: data, // 请求的数据
        timeout:5000,
        success: function (res) {
          // 请求成功后的回调
          console.log('注册成功', res);
      
          // 显示注册成功的提示框
          wx.showToast({
            title: '注册成功', // 提示内容
            icon: 'success',  // 成功的图标
            duration: 20000,   // 显示时间，单位是毫秒
          });
      
          // 跳转回上一页
          wx.navigateBack({
           delta: 1, // 返回上一页面
          });
        },
        fail: function (err) {
          // 请求失败后的回调
          console.log('API请求失败', err);
          // 可以选择在失败时显示错误提示
          wx.showToast({
            title: '注册失败，请稍后再试',
            icon: 'none',
            duration: 2000,
          });
        }
      });
      

    
  },
  fanhui(){
    wx.navigateBack({
      delta: 1, // 返回上一页面
    })
  },
})