import { Button, Stack, Typography } from '@mui/material'
import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { fetchCart } from '../store/cartSlice'
import { useAppDispatch, useAppSelector } from '../store/hooks'

export default function CartPage() {
  const cart = useAppSelector((s) => s.cart)
  const dispatch = useAppDispatch()
  useEffect(() => { void dispatch(fetchCart()) }, [dispatch])

  return (
    <Stack spacing={2}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Cart</Typography>
      {cart.items.length === 0 && <Typography>Your cart is empty.</Typography>}
      {cart.items.map((i) => (
        <Stack key={i.productId} direction="row" sx={{ justifyContent: 'space-between', bgcolor: 'white', p: 2, borderRadius: 2 }}>
          <Typography>{i.productName} × {i.quantity}</Typography>
          <Typography sx={{ fontWeight: 700 }}>₹{i.lineTotal}</Typography>
        </Stack>
      ))}
      <Typography variant="h6">Total: ₹{cart.total}</Typography>
      {cart.itemCount > 0 && <Button component={Link} to="/checkout" variant="contained" sx={{ bgcolor: '#fca311', color: '#14213d', alignSelf: 'start' }}>Checkout</Button>}
    </Stack>
  )
}
