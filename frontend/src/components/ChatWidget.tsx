import ChatIcon from '@mui/icons-material/Chat'
import { Box, Fab, IconButton, Paper, Stack, TextField, Typography } from '@mui/material'
import { useState } from 'react'
import type { FormEvent } from 'react'
import api from '../api/client'

interface Msg { role: 'user' | 'bot'; text: string }

export default function ChatWidget() {
  const [open, setOpen] = useState(false)
  const [input, setInput] = useState('')
  const [msgs, setMsgs] = useState<Msg[]>([
    { role: 'bot', text: 'Hi! Ask about orders, cart, products, wishlist — or type help.' },
  ])
  const [busy, setBusy] = useState(false)

  const send = async (e: FormEvent) => {
    e.preventDefault()
    if (!input.trim() || busy) return
    const message = input.trim()
    setInput('')
    setMsgs((m) => [...m, { role: 'user', text: message }])
    setBusy(true)
    try {
      const { data } = await api.post('/api/v1/chat', { message })
      setMsgs((m) => [...m, { role: 'bot', text: data.reply }])
    } catch (err: any) {
      setMsgs((m) => [...m, { role: 'bot', text: err.response?.data?.message || 'Chat failed' }])
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <Fab color="warning" sx={{ position: 'fixed', right: 24, bottom: 24 }} onClick={() => setOpen((o) => !o)}>
        <ChatIcon />
      </Fab>
      {open && (
        <Paper elevation={8} sx={{ position: 'fixed', right: 24, bottom: 96, width: 340, height: 420, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <Box sx={{ bgcolor: '#14213d', color: 'white', px: 2, py: 1.2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography sx={{ fontWeight: 700 }}>Rule assistant</Typography>
            <IconButton size="small" sx={{ color: 'white' }} onClick={() => setOpen(false)}>×</IconButton>
          </Box>
          <Stack spacing={1} sx={{ p: 1.5, flexGrow: 1, overflowY: 'auto' }}>
            {msgs.map((m, i) => (
              <Box
                key={i}
                sx={{
                  alignSelf: m.role === 'user' ? 'flex-end' : 'flex-start',
                  bgcolor: m.role === 'user' ? '#fca311' : '#eee',
                  px: 1.2,
                  py: 0.8,
                  borderRadius: 2,
                  maxWidth: '85%',
                }}
              >
                <Typography variant="body2">{m.text}</Typography>
              </Box>
            ))}
          </Stack>
          <Box component="form" onSubmit={send} sx={{ p: 1, borderTop: '1px solid #ddd', display: 'flex', gap: 1 }}>
            <TextField size="small" fullWidth value={input} onChange={(e) => setInput(e.target.value)} placeholder="my orders / cart / help" />
          </Box>
        </Paper>
      )}
    </>
  )
}
