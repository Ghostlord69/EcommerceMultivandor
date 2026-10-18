import { Button, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import api from '../api/client'

export default function OrdersPage() {
  const [orders, setOrders] = useState<any[]>([])
  const load = () => api.get('/api/v1/orders').then((r) => setOrders(r.data))
  useEffect(() => { void load() }, [])

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Orders</Typography>
      {orders.map((o) => (
        <Stack key={o.id} spacing={1} sx={{ bgcolor: 'white', p: 2, borderRadius: 2 }}>
          <Typography sx={{ fontWeight: 700 }}>#{o.id} · {o.status} · ₹{o.total}</Typography>
          <Typography variant="body2">{o.items.map((i: any) => `${i.productName}×${i.quantity}`).join(', ')}</Typography>
          {(o.status === 'PENDING' || o.status === 'PAID') && (
            <Button size="small" onClick={async () => { await api.post(`/api/v1/orders/${o.id}/cancel`); void load() }}>Cancel</Button>
          )}
        </Stack>
      ))}
    </Stack>
  )
}
