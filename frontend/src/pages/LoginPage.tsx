import { Alert, Button, MenuItem, Stack, TextField, Typography } from '@mui/material'
import { Formik, Form } from 'formik'
import { Link, useNavigate } from 'react-router-dom'
import * as Yup from 'yup'
import { login, register, type Role } from '../store/authSlice'
import { useAppDispatch, useAppSelector } from '../store/hooks'

const schema = Yup.object({
  email: Yup.string().email().required(),
  password: Yup.string().min(6).required(),
})

export function LoginPage() {
  const dispatch = useAppDispatch()
  const auth = useAppSelector((s) => s.auth)
  const navigate = useNavigate()

  return (
    <Stack spacing={2} sx={{ maxWidth: 420, mx: 'auto' }}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Welcome back</Typography>
      {auth.error && <Alert severity="error">{auth.error}</Alert>}
      <Formik
        initialValues={{ email: 'customer@shop.com', password: 'password123' }}
        validationSchema={schema}
        onSubmit={async (values) => {
          const result = await dispatch(login(values))
          if (login.fulfilled.match(result)) navigate('/')
        }}
      >
        {({ values, handleChange, errors, touched }) => (
          <Form>
            <Stack spacing={2}>
              <TextField name="email" label="Email" value={values.email} onChange={handleChange} error={!!(touched.email && errors.email)} helperText={touched.email && errors.email} />
              <TextField name="password" type="password" label="Password" value={values.password} onChange={handleChange} error={!!(touched.password && errors.password)} helperText={touched.password && errors.password} />
              <Button type="submit" variant="contained" sx={{ bgcolor: '#14213d' }} disabled={auth.status === 'loading'}>Login</Button>
              <Typography variant="body2">No account? <Link to="/register">Register</Link></Typography>
              <Typography variant="caption" color="text.secondary">Demo: customer@shop.com / seller@shop.com / admin@shop.com — password123</Typography>
            </Stack>
          </Form>
        )}
      </Formik>
    </Stack>
  )
}

export function RegisterPage() {
  const dispatch = useAppDispatch()
  const auth = useAppSelector((s) => s.auth)
  const navigate = useNavigate()

  return (
    <Stack spacing={2} sx={{ maxWidth: 420, mx: 'auto' }}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Create account</Typography>
      {auth.error && <Alert severity="error">{auth.error}</Alert>}
      <Formik
        initialValues={{ email: '', password: '', fullName: '', role: 'CUSTOMER' as Role }}
        validationSchema={schema.shape({ fullName: Yup.string().required(), role: Yup.string().required() })}
        onSubmit={async (values) => {
          const result = await dispatch(register(values))
          if (register.fulfilled.match(result)) navigate('/')
        }}
      >
        {({ values, handleChange, errors, touched }) => (
          <Form>
            <Stack spacing={2}>
              <TextField name="fullName" label="Full name" value={values.fullName} onChange={handleChange} error={!!(touched.fullName && errors.fullName)} />
              <TextField name="email" label="Email" value={values.email} onChange={handleChange} />
              <TextField name="password" type="password" label="Password" value={values.password} onChange={handleChange} />
              <TextField select name="role" label="Role" value={values.role} onChange={handleChange}>
                <MenuItem value="CUSTOMER">Customer</MenuItem>
                <MenuItem value="SELLER">Seller</MenuItem>
              </TextField>
              <Button type="submit" variant="contained" sx={{ bgcolor: '#14213d' }}>Register</Button>
            </Stack>
          </Form>
        )}
      </Formik>
    </Stack>
  )
}
