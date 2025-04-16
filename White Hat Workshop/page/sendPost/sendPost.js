Page({

  /**
   * 页面的初始数据
   */
  data: {
    inputTitle: "请输入标题（5~100字）",
    inputContent: "请输入正文",
    picker: ["安全漏洞讨论", "网络攻击案例", "网络工具推荐", "行业动态"],
    pickerIndex: 0,
  },

  onLoad(options) {
    this.loadDataFromCache();
  },

  onShow() {},

  loadDataFromCache() {
    const cachedTitle = wx.getStorageSync('inputTitle');
    const cachedContent = wx.getStorageSync('inputContent');
    const cachedPickerIndex = wx.getStorageSync('pickerIndex');

    if (cachedTitle) {
      this.setData({
        inputTitle: cachedTitle
      });
    }
    if (cachedContent) {
      this.setData({
        inputContent: cachedContent
      });
    }
    if (cachedPickerIndex !== undefined) {
      this.setData({
        pickerIndex: cachedPickerIndex
      });
    }
  },

  sendPost() {
    let that = this;
    const userInfo = wx.getStorageSync('userInfo');
    let data = {
      "authorId": userInfo.id,
      "avatar": userInfo.avatar,
      "content": that.data.inputContent,
      "replyCount": 0,
      "section": that.data.picker[that.data.pickerIndex],
      "username": userInfo.username,
      "title": that.data.inputTitle,
      'Authorization': `Bearer ${userInfo.token}`,
    };

    console.log(data);

    wx.request({
      url: `http://wacyg.fun/post/insertPost`,
      method: 'post',
      data: data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`, 
      },
      success(res) {
        console.log(res);
        if (res.statusCode === 200) {
          wx.showToast({
            title: '发布成功！',
            icon: 'success',
            duration: 10000, 
          });
          that.setData({
            inputTitle: '请输入标题（5~100字）',
            inputContent: '请输入正文',
          });
          wx.removeStorageSync('inputTitle');
          wx.removeStorageSync('inputContent');
          wx.removeStorageSync('pickerIndex');
          wx.navigateBack();
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

  getInputTitle(e) {
    console.log(e.detail.value); 
    this.setData({
      inputTitle: e.detail.value, 
    });
    wx.setStorageSync('inputTitle', e.detail.value);
  },

  onFocusTitle: function () {
    if (this.data.inputTitle === '请输入标题（5~100字）') {
      this.setData({
        inputTitle: ''
      });
    }
  },

  getInputContent(e) {
    console.log(e.detail.value); 
    this.setData({
      inputContent: e.detail.value, 
    });
    wx.setStorageSync('inputContent', e.detail.value);
  },

  onFocusContent: function () {
    if (this.data.inputContent === '请输入正文') {
      this.setData({
        inputContent: ''
      });
    }
  },

  PickerChange: function (e) {
    this.setData({
      pickerIndex: e.detail.value,
    });
    wx.setStorageSync('pickerIndex', e.detail.value);
  },

  onCancel() {
    const that = this;
    wx.showModal({
      title: '提示',
      content: '是否保存当前内容？',
      cancelText: '不保存',
      confirmText: '保存',
      success(res) {
        if (res.confirm) {
          wx.setStorageSync('inputTitle', that.data.inputTitle);
          wx.setStorageSync('inputContent', that.data.inputContent);
          wx.setStorageSync('pickerIndex', that.data.pickerIndex);
        } else {
          wx.removeStorageSync('inputTitle');
          wx.removeStorageSync('inputContent');
          wx.removeStorageSync('pickerIndex');
        }
        wx.navigateBack();
      },
    });
  },
});
