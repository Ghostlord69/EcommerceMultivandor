import { AppBar, Badge, Box, Button, Container, Toolbar, Typography } from '@mui/material'
import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '../store/hooks'
import { logout } from '../store/authSlice'
import ChatWidget from './ChatWidget'

export default function Layout() {
  const auth = useAppSelector((s) => s.auth)
  const cart = useAppSelector((s) => s.cart)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppBar position="sticky" elevation={0} sx={{ bgcolor: '#14213d' }}>
        <Toolbar sx={{ gap: 2, flexWrap: 'wrap' }}>
          <Typography
            component={Link}
            to="/"
            variant="h6"
            sx={{ fontWeight: 800, letterSpacing: 0.4, color: '#fca311' }}
          >
            BazaarHub
          </Typography>
          <Box sx={{ flexGrow: 1 }} />
          <Button color="inherit" component={Link} to="/">Shop</Button>
          {auth.token && <Button color="inherit" component={Link} to="/wishlist">Wishlist</Button>}
          {auth.token && (
            <Button color="inherit" component={Link} to="/cart">
              <Badge badgeContent={cart.itemCount} color="warning">Cart</Badge>
            </Button>
          )}
          {auth.token && <Button color="inherit" component={Link} to="/orders">Orders</Button>}
          {auth.role === 'SELLER' && <Button color="inherit" component={Link} to="/seller">Seller</Button>}
          {auth.role === 'ADMIN' && <Button color="inherit" component={Link} to="/admin">Admin</Button>}
          {!auth.token ? (
            <>
              <Button color="inherit" component={Link} to="/login">Login</Button>
              <Button variant="contained" sx={{ bgcolor: '#fca311', color: '#14213d' }} component={Link} to="/register">Join</Button>
            </>
          ) : (
            <Button color="inherit" onClick={() => { dispatch(logout()); navigate('/login') }}>Logout</Button>
          )}
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: 4, flexGrow: 1 }}>
        <Outlet />
      </Container>
      {auth.token && <ChatWidget />}
    </Box>
  )
}
