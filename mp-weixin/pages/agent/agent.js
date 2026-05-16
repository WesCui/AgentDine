var BASE_URL = 'http://localhost:8080';

Page({
  data: {
    messages: [],
    inputText: '',
    loading: false,
    scrollTop: 0,
    sessionId: '',
    quickActions: [
      { text: '今天吃什么', icon: '🍽️' },
      { text: '推荐辣的菜', icon: '🌶️' },
      { text: '帮我看看订单', icon: '📦' },
      { text: '我要催单', icon: '⏰' }
    ]
  },

  onLoad: function() {
    var sessionId = 'wx-' + Date.now() + '-' + Math.random().toString(36).substr(2, 9);
    this.setData({ sessionId: sessionId });

    // 欢迎消息
    this.addMessage('assistant', '你好！我是苍穹外卖智能助手 🤖\n\n我可以帮你：\n• 🍜 推荐菜品和套餐\n• 📦 查询订单状态\n• ⏰ 催单提醒\n• 💬 解答问题\n\n试试下面的快捷操作，或直接告诉我你想吃什么吧～');
  },

  onInput: function(e) {
    this.setData({ inputText: e.detail.value });
  },

  // 发送消息
  sendMessage: function() {
    var text = this.data.inputText.trim();
    if (!text || this.data.loading) return;

    this.addMessage('user', text);
    this.setData({ inputText: '', loading: true });
    this.scrollToBottom();

    var that = this;
    wx.request({
      url: BASE_URL + '/user/agent/chat',
      method: 'POST',
      header: {
        'Content-Type': 'application/json',
        'authentication': wx.getStorageSync('token') || ''
      },
      data: {
        message: text
      },
      success: function(res) {
        if (res.data && res.data.code === 1) {
          that.addMessage('assistant', res.data.data);
        } else {
          that.addMessage('assistant', '抱歉，我暂时无法处理您的请求，请稍后重试。');
        }
      },
      fail: function() {
        // fallback: try without token
        wx.request({
          url: BASE_URL + '/user/agent/chat',
          method: 'POST',
          header: { 'Content-Type': 'application/json' },
          data: { message: text },
          success: function(res2) {
            if (res2.data && res2.data.code === 1) {
              that.addMessage('assistant', res2.data.data);
            } else {
              that.addMessage('assistant', '抱歉，AI 服务暂不可用，请稍后重试。');
            }
          },
          fail: function() {
            that.addMessage('assistant', '网络连接失败，请检查网络后重试。');
          }
        });
      },
      complete: function() {
        that.setData({ loading: false });
        that.scrollToBottom();
      }
    });
  },

  // 快捷操作
  onQuickAction: function(e) {
    var text = e.currentTarget.dataset.text;
    this.setData({ inputText: text });
    this.sendMessage();
  },

  // 添加消息
  addMessage: function(role, content) {
    var msg = {
      role: role,
      content: content,
      time: this.formatTime(new Date())
    };
    var messages = this.data.messages;
    messages.push(msg);
    this.setData({ messages: messages });
  },

  // 滚动到底部
  scrollToBottom: function() {
    var that = this;
    setTimeout(function() {
      that.setData({ scrollTop: 99999 });
    }, 100);
  },

  // 格式化时间
  formatTime: function(date) {
    var h = date.getHours();
    var m = date.getMinutes();
    return (h < 10 ? '0' : '') + h + ':' + (m < 10 ? '0' : '') + m;
  }
});
