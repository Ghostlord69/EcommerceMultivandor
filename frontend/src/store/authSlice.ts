import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import api from '../api/client'

export type Role = 'CUSTOMER' | 'SELLER' | 'ADMIN'

export interface AuthState {
  token: string | null
  userId: number | null
  email: string | null
  fullName: string | null
  role: Role | null
  status: 'idle' | 'loading' | 'failed'
  error?: string
}

const saved = localStorage.getItem('emv_auth')
const initial: AuthState = saved
  ? { ...JSON.parse(saved), status: 'idle' }
  : { token: null, userId: null, email: null, fullName: null, role: null, status: 'idle' }

export const login = createAsyncThunk(
  'auth/login',
  async (payload: { email: string; password: string }, { rejectWithValue }) => {
    try {
      const { data } = await api.post('/api/v1/auth/login', payload)
      return data
    } catch (e: any) {
      return rejectWithValue(e.response?.data?.message || 'Login failed')
    }
  },
)

export const register = createAsyncThunk(
  'auth/register',
  async (payload: { email: string; password: string; fullName: string; role: Role }, { rejectWithValue }) => {
    try {
      const { data } = await api.post('/api/v1/auth/register', payload)
      return data
    } catch (e: any) {
      return rejectWithValue(e.response?.data?.message || 'Register failed')
    }
  },
)

const authSlice = createSlice({
  name: 'auth',
  initialState: initial,
  reducers: {
    logout(state) {
      state.token = null
      state.userId = null
      state.email = null
      state.fullName = null
      state.role = null
      localStorage.removeItem('emv_auth')
    },
  },
  extraReducers: (builder) => {
    const apply = (state: AuthState, payload: any) => {
      state.token = payload.token
      state.userId = payload.userId
      state.email = payload.email
      state.fullName = payload.fullName
      state.role = payload.role
      state.status = 'idle'
      state.error = undefined
      localStorage.setItem('emv_auth', JSON.stringify({
        token: payload.token,
        userId: payload.userId,
        email: payload.email,
        fullName: payload.fullName,
        role: payload.role,
      }))
    }
    builder
      .addCase(login.pending, (s) => { s.status = 'loading'; s.error = undefined })
      .addCase(login.fulfilled, (s, a) => apply(s, a.payload))
      .addCase(login.rejected, (s, a) => { s.status = 'failed'; s.error = String(a.payload) })
      .addCase(register.pending, (s) => { s.status = 'loading'; s.error = undefined })
      .addCase(register.fulfilled, (s, a) => apply(s, a.payload))
      .addCase(register.rejected, (s, a) => { s.status = 'failed'; s.error = String(a.payload) })
  },
})

export const { logout } = authSlice.actions
export default authSlice.reducer
