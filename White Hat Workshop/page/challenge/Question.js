Page({
  data: {
    HiddenExplanation: false, // 隐藏解析
    showxuanti: false, // 显示选题弹窗
    questionNumber: 0, // 当前题号
    questions: null, // 所有的题目数据
    nowQuestion: 0, // 当前的题目数据
    nowOption: 4, // 当前选择的选项（0-3，4表示未选择）
    showSubmit: false, // 是否显示提交按钮
    category: null, // 题目的类型
    type: null, // 1表示考试，0表示训练
    questionLength: null, // 题目总数
    answers: {}, // 结构化的用户答案
    pm: 0, // 分数
    token: wx.getStorageSync('userInfo'),
    number: null,
    answered: [], // 记录每个题目的回答状态
  },

  // 显示模态框
  showModal(e) {
    this.setData({
      modalName: e.currentTarget.dataset.target
    });
  },

  // 隐藏模态框
  hideModal(e) {
    this.setData({
      modalName: null
    });
  },

  // 显示/隐藏解析
  ckda() {
    this.setData({
      HiddenExplanation: !this.data.HiddenExplanation
    });
  },

  // 点击选题
  xt(e) {
    const questionIndex = e.currentTarget.dataset.n-1;
    console.log(`点击了第 ${questionIndex} 题`);

    // 获取点击的题目
    const selectedQuestion = this.data.questions[questionIndex];

    // 获取用户之前选择的答案
    const prevAnswer = this.data.answers[selectedQuestion.id];

    // 如果用户之前答过该题，则设置相应的选项
    let selectedOption = 4; // 默认没有选择
    if (prevAnswer !== undefined) {
      // 将答案映射为选项索引（0: A, 1: B, 2: C, 3: D）
      selectedOption = ["A", "B", "C", "D"].indexOf(prevAnswer);
    }

    // 设置当前的题目和选项
    this.setData({
      questionNumber: questionIndex,
      nowQuestion: selectedQuestion,
      nowOption: selectedOption, // 设置为用户之前选择的答案
    });

    this.hideModal();
  },

  // 页面加载时获取问题
  onLoad(options) {
    const userInfo = wx.getStorageSync('userInfo'); // 从存储中获取用户信息
    this.setData({
      type: options.t,
      category: options.c,
      examId: 'challengel' + options.index.toString(),
      userInfo: userInfo,
    });
    console.log(this.data.category);

    // 根据类型选择获取问题的方法
    if (options.t == 1) {
      this.get_Question_s10(options);
    } else {
      this.get_Question(options);
    }
  },

  // 获取考试问题（随机10道）
  get_Question_s10(options) {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo) {
      this.showToast('用户未登录');
      return;
    }

    wx.request({
      url: 'http://wacyg.fun/api/questions',
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`,
      },
      data: {
        category: options.c
      },
      success: this.handleQuestionResponse.bind(this),
      fail: () => this.showToast('获取问题失败'),
    });
  },

  // 获取训练问题
  get_Question(options) {
    const userInfo = wx.getStorageSync('userInfo');
    if (!userInfo) {
      wx.showModal({
        title: '提示',
        content: '请先登录账号',
        showCancel: false,
        success(res) {
          if (res.confirm) {
            wx.navigateTo({
              url: '/page/login/login'
            });
          }
        },
      });
      return;
    }

    wx.request({
      url: 'http://wacyg.fun/api/questions_category',
      method: 'GET',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`,
      },
      data: {
        category: options.c
      },
      success: this.handleQuestionResponse.bind(this),
      fail: () => this.showToast('获取问题失败'),
    });
  },

  // 处理获取问题的响应
  handleQuestionResponse(res) {
    if (res.statusCode === 200 && res.data) {
      this.setData({
        questions: res.data.data,
        nowQuestion: res.data.data[0],
        questionLength: res.data.data.length,
        number: Array.from({
          length: res.data.data.length
        }, (_, i) => i + 1),
      });
      // console.log()
    } else {
      this.showToast('获取问题失败');
    }
  },

  // 选择答案
  xz(event) {
    const index = event.currentTarget.dataset.i;
    this.setData({
      nowOption: index,
    });
    const answerMapping = {
      "0": "A",
      "1": "B",
      "2": "C",
      "3": "D"
    };
    const questionId = this.data.nowQuestion.id;
    const nowOption = this.data.nowOption;
    // 更新用户答案
    const updatedAnswers = {
      [questionId]: answerMapping[nowOption],
    };
    this.setData({
      answers: {
        ...this.data.answers,
        ...updatedAnswers
      },
    });
    console.log(this.data.answers)
    // 新用户已经答了这道题
    let answered = this.data.answered; // 获取当前的 answered 数组
    answered[this.data.questionNumber] = true; // 更新特定题目的回答状态
    console.log(answered)
    this.setData({
      answered: answered // 传递更新后的 answered 数组
    });
    console.log(answered)
  },

  // 选题后更新答案
  xyt() {
    this.setData({
      HiddenExplanation: false
    });

    let questionNumber = this.data.questionNumber + 1;
    // 设置下一题，并加载用户之前的答案
    if (questionNumber < this.data.questions.length) {
      const nextQuestion = this.data.questions[questionNumber];
      // 获取用户之前的答案
      const prevAnswer = this.data.answers[nextQuestion.id];

      // 如果之前已经答过，设置选中的答案
      let nextOption = 4; // 默认为没有选择
      if (prevAnswer !== undefined) {
        nextOption = ["A", "B", "C", "D"].indexOf(prevAnswer);
      }
      this.setData({
        questionNumber: questionNumber,
        nowQuestion: nextQuestion,
        nowOption: nextOption, // 设置为用户之前选择的答案
      });
    } else {
      // 如果是最后一题，显示提示
      wx.showToast({
        title: '已经到了最后一题！',
        icon: 'none',
      });
    }
  },

  // 提交答案
  submit() {
    // 检查用户是否选择了所有题目的答案
    const unansweredQuestions = this.data.questions.filter(question =>
      !this.data.answers[question.id]
    );
    if (unansweredQuestions.length > 0) {
      this.showToast('请完成所有题目！');
      return;
    }
    const userInfo = wx.getStorageSync('userInfo');
    const examId = this.data.examId;
    const answerMapping = { "0": "A", "1": "B", "2": "C", "3": "D" };

    const updatedAnswers = {
      ...this.data.answers,
      [this.data.nowQuestion.id]: answerMapping[this.data.nowOption],
    };

    wx.request({
      url: 'http://wacyg.fun/api/submit',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userInfo.token}`,
      },
      data: {
        examId,
        answers: updatedAnswers
      },
      success(res) {
        const pm = res.data.data.scoreDelta;
        console.log(res.data)
        console.log(pm);
        wx.showModal({
          title: '考试结果',
          content: `您的总得分为: ${pm}`,
          showCancel: false,
          confirmText: '确定',
          success(res) {
            if (res.confirm) {
              wx.navigateBack();
            }
          }
        });
      },
      fail: () => this.showToast('提交失败，请重试'),
    });
  },

  // 提示弹框
  showToast(message) {
    wx.showToast({
      title: message,
      icon: 'none',
    });
  },
});