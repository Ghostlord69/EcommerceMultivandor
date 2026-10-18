import { Button, Stack, Typography } from '@mui/material'
import { useEffect, useState } from 'react'
import { Pie, PieChart, Cell, ResponsiveContainer, Tooltip } from 'recharts'
import api from '../api/client'

const COLORS = ['#14213d', '#fca311', '#2a9d8f', '#e76f51', '#6d597a', '#4cc9f0']

export default function AdminPage() {
  const [dash, setDash] = useState<any>()
  const [pending, setPending] = useState<any[]>([])

  const refresh = async () => {
    setDash((await api.get('/api/v1/admin/dashboard')).data)
    setPending((await api.get('/api/v1/admin/shops/pending')).data)
  }

  useEffect(() => { void refresh() }, [])

  return (
    <Stack spacing={3}>
      <Typography variant="h4" sx={{ fontWeight: 800 }}>Admin</Typography>
      {dash && (
        <>
          <Typography>Pending sellers: {dash.pendingSellerApprovals} · Orders: {dash.totalOrders} · Active coupons: {dash.activeCoupons}</Typography>
          <div style={{ width: '100%', height: 280, background: 'white', borderRadius: 12 }}>
            <ResponsiveContainer>
              <PieChart>
                <Pie data={dash.ordersByStatus} dataKey="count" nameKey="label" outerRadius={100} label>
                  {dash.ordersByStatus.map((_: any, i: number) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </>
      )}
      <Typography variant="h6">Pending shops</Typography>
      {pending.map((s) => (
        <Stack key={s.id} direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <Typography>{s.name} ({s.sellerName})</Typography>
          <Button size="small" variant="contained" onClick={async () => { await api.post(`/api/v1/admin/shops/${s.id}/approve?approved=true`); void refresh() }}>Approve</Button>
        </Stack>
      ))}
      {!pending.length && <Typography color="text.secondary">No pending shops</Typography>}
    </Stack>
  )
}
