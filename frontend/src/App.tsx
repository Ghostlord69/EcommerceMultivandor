import type { ReactNode } from 'react'
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material'
import { Provider } from 'react-redux'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import AdminPage from './pages/AdminPage'
import CartPage from './pages/CartPage'
import CheckoutPage from './pages/CheckoutPage'
import HomePage from './pages/HomePage'
import { LoginPage, RegisterPage } from './pages/LoginPage'
import OrdersPage from './pages/OrdersPage'
import ProductPage from './pages/ProductPage'
import SellerPage from './pages/SellerPage'
import WishlistPage from './pages/WishlistPage'
import { store } from './store/store'
import { useAppSelector } from './store/hooks'

const theme = createTheme({
  palette: {
    primary: { main: '#14213d' },
    secondary: { main: '#fca311' },
  },
  typography: { fontFamily: '"Segoe UI","Trebuchet MS",sans-serif' },
})

function Private({ children }: { children: ReactNode }) {
  const token = useAppSelector((s) => s.auth.token)
  return token ? <>{children}</> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <Routes>
            <Route element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="products/:id" element={<ProductPage />} />
              <Route path="login" element={<LoginPage />} />
              <Route path="register" element={<RegisterPage />} />
              <Route path="cart" element={<Private><CartPage /></Private>} />
              <Route path="checkout" element={<Private><CheckoutPage /></Private>} />
              <Route path="orders" element={<Private><OrdersPage /></Private>} />
              <Route path="wishlist" element={<Private><WishlistPage /></Private>} />
              <Route path="seller" element={<Private><SellerPage /></Private>} />
              <Route path="admin" element={<Private><AdminPage /></Private>} />
            </Route>
          </Routes>
        </BrowserRouter>
      </ThemeProvider>
    </Provider>
  )
}
