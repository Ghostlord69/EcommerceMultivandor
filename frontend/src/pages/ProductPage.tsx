import { Button, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import api from '../api/client'
import { addToCart } from '../store/cartSlice'
import { useAppDispatch, useAppSelector } from '../store/hooks'

export default function ProductPage() {
  const { id } = useParams()
  const [product, setProduct] = useState<any>()
  const dispatch = useAppDispatch()
  const auth = useAppSelector((s) => s.auth)

  useEffect(() => {
    void api.get(`/api/v1/products/${id}`).then((r) => setProduct(r.data))
  }, [id])

  if (!product) return <Typography>Loading…</Typography>

  return (
    <Stack spacing={2} sx={{ maxWidth: 720 }}>
      <img src={product.imageUrl} alt={product.name} style={{ width: '100%', maxHeight: 360, objectFit: 'cover', borderRadius: 12 }} />
      <Typography variant="h4" sx={{ fontWeight: 800 }}>{product.name}</Typography>
      <Typography color="text.secondary">{product.description}</Typography>
      <Typography variant="h5">₹{product.price}</Typography>
      <Typography variant="body2">Stock: {product.stock} · Shop: {product.shopName}</Typography>
      {auth.token && (
        <Stack direction="row" spacing={1}>
          <Button variant="contained" sx={{ bgcolor: '#fca311', color: '#14213d' }} onClick={() => void dispatch(addToCart({ productId: product.id, quantity: 1 }))}>
            Add to cart
          </Button>
          <Button variant="outlined" onClick={() => void api.post(`/api/v1/wishlist/${product.id}`)}>Wishlist</Button>
        </Stack>
      )}
    </Stack>
  )
}
