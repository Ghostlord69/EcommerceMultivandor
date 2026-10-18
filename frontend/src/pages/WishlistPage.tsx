import { Button, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

export default function WishlistPage() {
  const [items, setItems] = useState<any[]>([])
  const load = () => api.get('/api/v1/wishlist').then((r) => setItems(r.data))
  useEffect(() => { void load() }, [])

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Wishlist</Typography>
      {items.map((p) => (
        <Stack key={p.id} direction="row" sx={{ justifyContent: 'space-between', bgcolor: 'white', p: 2, borderRadius: 2 }}>
          <Typography component={Link} to={`/products/${p.id}`}>{p.name}</Typography>
          <Button size="small" onClick={async () => { await api.delete(`/api/v1/wishlist/${p.id}`); void load() }}>Remove</Button>
        </Stack>
      ))}
    </Stack>
  )
}
