import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import api from '../api/client'

export interface CartItem {
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

interface CartState {
  items: CartItem[]
  total: number
  itemCount: number
}

const initialState: CartState = { items: [], total: 0, itemCount: 0 }

export const fetchCart = createAsyncThunk('cart/fetch', async () => {
  const { data } = await api.get('/api/v1/cart')
  return data
})

export const addToCart = createAsyncThunk('cart/add', async (payload: { productId: number; quantity: number }) => {
  const { data } = await api.post('/api/v1/cart/items', payload)
  return data
})

const cartSlice = createSlice({
  name: 'cart',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    const set = (state: CartState, payload: CartState) => {
      state.items = payload.items
      state.total = payload.total
      state.itemCount = payload.itemCount
    }
    builder.addCase(fetchCart.fulfilled, (s, a) => set(s, a.payload))
    builder.addCase(addToCart.fulfilled, (s, a) => set(s, a.payload))
  },
})

export default cartSlice.reducer
