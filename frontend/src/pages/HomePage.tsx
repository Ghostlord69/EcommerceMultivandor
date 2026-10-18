import { Box, Button, Card, CardActions, CardContent, CardMedia, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'
import { addToCart } from '../store/cartSlice'
import { useAppDispatch, useAppSelector } from '../store/hooks'

interface Product {
  id: number; name: string; description: string; price: number; stock: number; imageUrl?: string; categoryName: string; shopName: string
}

export default function HomePage() {
  const [products, setProducts] = useState<Product[]>([])
  const [q, setQ] = useState('')
  const auth = useAppSelector((s) => s.auth)
  const dispatch = useAppDispatch()

  const load = async (query = '') => {
    const { data } = await api.get('/api/v1/products', { params: query ? { q: query } : {} })
    setProducts(data)
  }

  useEffect(() => { void load() }, [])

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h3" sx={{ fontWeight: 800 }}>BazaarHub</Typography>
        <Typography color="text.secondary" sx={{ maxWidth: 560 }}>
          Multi-vendor marketplace with JWT auth, Razorpay/Stripe checkout, and a rule-based shopping assistant.
        </Typography>
      </Box>
      <Stack direction="row" spacing={1}>
        <TextField size="small" fullWidth placeholder="Search products" value={q} onChange={(e) => setQ(e.target.value)} />
        <Button variant="contained" sx={{ bgcolor: '#14213d' }} onClick={() => void load(q)}>Search</Button>
      </Stack>
      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr' } }}>
        {products.map((p) => (
          <Card key={p.id} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
            <CardMedia component="img" height="160" image={p.imageUrl || 'https://picsum.photos/600/400'} alt={p.name} />
            <CardContent sx={{ flexGrow: 1 }}>
              <Typography variant="overline">{p.categoryName} · {p.shopName}</Typography>
              <Typography variant="h6" component={Link} to={`/products/${p.id}`}>{p.name}</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>{p.description}</Typography>
              <Typography sx={{ mt: 1.5, fontWeight: 700 }}>₹{p.price}</Typography>
            </CardContent>
            <CardActions>
              <Button size="small" component={Link} to={`/products/${p.id}`}>Details</Button>
              {auth.token && (
                <Button size="small" onClick={() => void dispatch(addToCart({ productId: p.id, quantity: 1 }))}>Add to cart</Button>
              )}
            </CardActions>
          </Card>
        ))}
      </Box>
    </Stack>
  )
}
