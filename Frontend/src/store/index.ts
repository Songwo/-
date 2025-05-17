import { createStore } from 'f:/NetGame_s/hello_vue3/node_modules/vuex';

// 定义角色类型
type UserRole = 'ROLE_ADMIN' | 'ROLE_USER';

interface State {
  user: string | null;
  id: string | null;
  avatar: string | null;
  token: string;
  roles: UserRole[];
}

export default createStore({
  state: {
    user: null,  // 存储用户信息
    id: null,
    avatar: null,
    token: '',  // 初始值为存储在 localStorage 中的 token
    roles: [],  // 存储用户角色
  } as State,
  mutations: {
    setUser(state: State, user: string) {
      state.user = user;
      localStorage.setItem('user', user);  // 存储到 localStorage
    },
    setToken(state: State, token: string) {
      state.token = token;
      localStorage.setItem('token', token);  // 存储到 localStorage
    },
    setId(state: State, id: string) {
      state.id = id;
      localStorage.setItem('id', id);  // 存储到 localStorage
    },
    setAvatar(state: State, avatar: string) {
      state.avatar = avatar;
      localStorage.setItem('avatar', avatar);  // 存储到 localStorage
    },
    setRoles(state: State, roles: UserRole[]) {
      state.roles = roles;
      localStorage.setItem('roles', JSON.stringify(roles));  // 存储到 localStorage
    },
    clearUserInfo(state: State) {
      state.token = "";
      state.user = null;
      state.id = null;
      state.avatar = null;
      state.roles = [];
      localStorage.removeItem('token');  
      localStorage.removeItem('user');  
      localStorage.removeItem('id');  
      localStorage.removeItem('avatar');
      localStorage.removeItem('roles');
    }
  },
  actions: {
    setUser({ commit }, user: string) {
      commit('setUser', user);
    },
    setToken({ commit }, token: string) {
      commit('setToken', token);
    },
    setId({ commit }, id: string) {
      commit('setId', id);
    },
    setAvatar({ commit }, avatar: string) {
      commit('setAvatar', avatar); 
    },
    setRoles({ commit }, roles: UserRole[]) {
      commit('setRoles', roles);
    },
    logout({ commit }) {
      commit('clearUserInfo');  // 清空用户信息和 token
    }
  },
  getters: {
    getUser: (state: State) => state.user,
    getToken: (state: State) => state.token,
    getId: (state: State) => state.id,
    getAvatar: (state: State) => state.avatar,
    getRoles: (state: State) => state.roles,
    isAdmin: (state: State) => state.roles.includes('ROLE_ADMIN')
  }
});
