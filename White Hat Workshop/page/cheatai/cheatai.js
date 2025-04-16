Page({
  data: {
    InputBottom: 0,
    userInfo: null,
    aiavatar: "http://wacyg.fun/avatar/6a103249-10fc-422d-883a-c5cf4bca4364_信息.png",
    history: [],
    inputValue: "请输入问题",
    scrollTop: 0 // 滚动位置初始化
  },

  onLoad() {
    this.setData({
      userInfo: wx.getStorageSync('userInfo')
    });
    this.getHistory(); // 页面加载时获取历史记录
  },

  onShow() {
    this.scrollToBottom(); // 页面显示时自动滚动到底部
  },

  // 获取聊天历史记录
  getHistory() {
    let that = this;
    wx.request({
      url: `http://wacyg.fun:9000/api/chat/history`, // 获取历史聊天记录接口
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${that.data.userInfo.token}`, // 用户身份认证
      },
      success(res) {
        console.log(res.data)
        if (res.data.history) {
          // 更新历史记录
          that.setData({
            history: res.data.history
          });
          that.scrollToBottom(); // 获取完历史记录后滚动到底部
        }
      },
      fail(error) {
        console.log("请求失败", error);
      }
    });
  },

  // 获取输入框中的值
  getInputValue(e) {
    this.setData({
      inputValue: e.detail.value // 保存输入框中的值到 data 中
    });
  },

  // 输入框聚焦时，调整底部输入框位置
  InputFocus(e) {
    this.setData({
      InputBottom: e.detail.height, // 键盘弹出时调整输入框位置
      inputValue: "" // 清空输入框
    });
  },

  // 输入框失去焦点时，恢复底部输入框位置
  InputBlur(e) {
    this.setData({
      InputBottom: 0
    });
  },

  // 发送消息
  postinputValue() {
    let that = this;
    let userMessage = that.data.inputValue;

    if (!userMessage.trim()) {
      wx.showToast({
        title: '请输入消息',
        icon: 'none',
      });
      return;
    }

    // 清空输入框
    that.setData({
      inputValue: ""
    });
    // 将用户消息添加到历史记录
    let newHistory = that.data.history.concat({
      content: userMessage,
      role: "user"
    });
    that.scrollToBottom();

    // 更新历史记录
    that.setData({
      history: newHistory
    }, () => {
      // 请求 AI 回复
      wx.request({
        url: `http://wacyg.fun:9000/api/chat`, // AI 聊天接口
        method: 'POST',
        header: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${that.data.userInfo.token}`,
        },
        data: {
          "message": userMessage
        },
        success(res) {
          console.log(res.data)
          if (res.statusCode === 200 && res.data && res.data.reply) {
            // 添加 AI 回复到历史记录
            newHistory = newHistory.concat({
              content: res.data.reply,
              role: "assistant"
            });

            // 更新历史记录
            that.setData({
              history: newHistory
            }, () => {
              that.scrollToBottom(); // 在更新历史记录后滚动到底部
            });
          }
        },
        fail(error) {
          console.log("请求失败", error);
          wx.showToast({
            title: '消息发送失败，请稍后再试。',
            icon: 'none',
          });
        }
      });
    });
  },

  // 滚动到底部
  scrollToBottom() {
    let query = wx.createSelectorQuery();
    query.select('.cu-chat').boundingClientRect((rect) => {
      this.setData({
        scrollTop: 99999 // 更新 scrollTop 使其滚动到底部
      });
    }).exec();
  }
});