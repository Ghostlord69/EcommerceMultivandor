import { Alert, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import { fetchCart } from '../store/cartSlice'
import { useAppDispatch, useAppSelector } from '../store/hooks'

export default function CheckoutPage() {
  const cart = useAppSelector((s) => s.cart)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const [addresses, setAddresses] = useState<any[]>([])
  const [addressId, setAddressId] = useState<number | ''>('')
  const [coupon, setCoupon] = useState('WELCOME10')
  const [provider, setProvider] = useState('RAZORPAY')
  const [line1, setLine1] = useState('12 Market Street')
  const [city, setCity] = useState('Bengaluru')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    void dispatch(fetchCart())
    void api.get('/api/v1/addresses').then((r) => {
      setAddresses(r.data)
      if (r.data[0]) setAddressId(r.data[0].id)
    })
  }, [dispatch])

  const ensureAddress = async () => {
    if (addressId) return Number(addressId)
    const { data } = await api.post('/api/v1/addresses', {
      line1, line2: '', city, state: 'KA', postalCode: '560001', country: 'IN', defaultAddress: true,
    })
    setAddresses((a) => [...a, data])
    setAddressId(data.id)
    return data.id as number
  }

  const placeOrder = async () => {
    try {
      setError('')
      const addr = await ensureAddress()
      const { data: order } = await api.post('/api/v1/checkout', { addressId: addr, couponCode: coupon || null })
      const { data: payment } = await api.post('/api/v1/payments', { orderId: order.id, provider })
      if (provider !== 'COD') {
        await api.post('/api/v1/payments/confirm', { paymentId: payment.id, providerReference: payment.providerReference || 'demo_paid' })
      }
      setMessage(`Order #${order.id} placed via ${provider}`)
      void dispatch(fetchCart())
      setTimeout(() => navigate('/orders'), 800)
    } catch (e: any) {
      setError(e.response?.data?.message || 'Checkout failed')
    }
  }

  return (
    <Stack spacing={2} sx={{ maxWidth: 520 }}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Checkout</Typography>
      <Typography>Cart total: ₹{cart.total}</Typography>
      {error && <Alert severity="error">{error}</Alert>}
      {message && <Alert severity="success">{message}</Alert>}
      {!addresses.length && (
        <>
          <TextField label="Address line" value={line1} onChange={(e) => setLine1(e.target.value)} />
          <TextField label="City" value={city} onChange={(e) => setCity(e.target.value)} />
        </>
      )}
      {!!addresses.length && (
        <TextField select label="Ship to" value={addressId} onChange={(e) => setAddressId(Number(e.target.value))}>
          {addresses.map((a) => <MenuItem key={a.id} value={a.id}>{a.line1}, {a.city}</MenuItem>)}
        </TextField>
      )}
      <TextField label="Coupon" value={coupon} onChange={(e) => setCoupon(e.target.value)} />
      <TextField select label="Payment" value={provider} onChange={(e) => setProvider(e.target.value)}>
        <MenuItem value="RAZORPAY">Razorpay</MenuItem>
        <MenuItem value="STRIPE">Stripe</MenuItem>
        <MenuItem value="COD">Cash on Delivery</MenuItem>
      </TextField>
      <Button variant="contained" sx={{ bgcolor: '#14213d' }} onClick={() => void placeOrder()} disabled={cart.itemCount === 0}>Pay & place order</Button>
    </Stack>
  )
}
