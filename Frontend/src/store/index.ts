import { createStore } from 'f:/NetGame_s/hello_vue3/node_modules/vuex';

export default createStore({
  state: {
    user: null,  // 存储用户信息
    id:null,
    avatar:null,
    token:'',  // 初始值为存储在 localStorage 中的 token
  },
  mutations: {
    setUser(state, user) {
      state.user = user;
      localStorage.setItem('user', user);  // 存储到 localStorage
    },
    setToken(state, token) {
      state.token = token;
      localStorage.setItem('token', token);  // 存储到 localStorage
    },
    setId(state,id){
      state.id=id;
      localStorage.setItem('id', id);  // 存储到 localStorage
    },
    setAvatar(state,avatar){
      state.avatar=avatar;
      localStorage.setItem('avatar', avatar);  // 存储到 localStorage
    },
    clearUserInfo(state) {
      state.token = "";
      state.user = null;
      state.id=null;
      state.avatar=null;
      localStorage.removeItem('token');  
      localStorage.removeItem('user');  
      localStorage.removeItem('id');  
      localStorage.removeItem('avatar'); 
    }
  },
  actions: {
    setUser({ commit }, user) {
      commit('setUser', user);
    },
    setToken({ commit }, token) {
      commit('setToken', token);
    },
    setId({commit},id){
      commit('setId',id);
    },
    setAvatar({commit},avater){
      commit('setAvatar',avater); 
    },
    logout({ commit }) {
      commit('clearUserInfo');  // 清空用户信息和 token
    }
  },
  getters: {
    getUser: (state) => state.user,
    getToken: (state) => state.token,
    getId:(state)=>state.id,
    getAvatar:(state)=>state.avatar
  }
});
