import { Button, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Bar, BarChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import api from '../api/client'

export default function SellerPage() {
  const [dash, setDash] = useState<any>()
  const [shopName, setShopName] = useState('My Shop')
  const [products, setProducts] = useState<any[]>([])
  const [name, setName] = useState('')
  const [price, setPrice] = useState('999')
  const [stock, setStock] = useState('10')
  const [categoryId, setCategoryId] = useState<number>(1)
  const [msg, setMsg] = useState('')

  const refresh = async () => {
    try {
      const d = await api.get('/api/v1/seller/dashboard')
      setDash(d.data)
      const p = await api.get('/api/v1/seller/products')
      setProducts(p.data)
    } catch {
      /* shop may not exist yet */
    }
    const cats = await api.get('/api/v1/categories')
    if (cats.data[0]) setCategoryId(cats.data[0].id)
  }

  useEffect(() => { void refresh() }, [])

  return (
    <Stack spacing={3}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Seller dashboard</Typography>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
        <TextField label="Shop name" value={shopName} onChange={(e) => setShopName(e.target.value)} />
        <Button variant="contained" sx={{ bgcolor: '#14213d' }} onClick={async () => {
          await api.post('/api/v1/seller/shop', { name: shopName, description: 'Seller shop' })
          setMsg('Shop saved (needs admin approval if new)')
          void refresh()
        }}>Save shop</Button>
      </Stack>
      {msg && <Typography color="text.secondary">{msg}</Typography>}
      {dash && (
        <>
          <Typography>Pending orders: {dash.pendingOrders} · Low stock: {dash.lowStockProducts} · Earnings: ₹{dash.earnings}</Typography>
          <div style={{ width: '100%', height: 260, background: 'white', borderRadius: 12, padding: 8 }}>
            <ResponsiveContainer>
              <BarChart data={dash.salesByDay || []}>
                <XAxis dataKey="label" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="count" fill="#fca311" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      )}
      <Typography variant="h6">Add product</Typography>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={1}>
        <TextField label="Name" value={name} onChange={(e) => setName(e.target.value)} />
        <TextField label="Price" value={price} onChange={(e) => setPrice(e.target.value)} />
        <TextField label="Stock" value={stock} onChange={(e) => setStock(e.target.value)} />
        <Button variant="outlined" onClick={async () => {
          await api.post('/api/v1/seller/products', {
            name, description: name, price: Number(price), stock: Number(stock), categoryId, imageUrl: 'https://picsum.photos/seed/' + encodeURIComponent(name) + '/600/400',
          })
          setName(''); void refresh()
        }}>Create</Button>
      </Stack>
      {products.map((p) => <Typography key={p.id}>{p.name} · ₹{p.price} · stock {p.stock}</Typography>)}
    </Stack>
  )
}
