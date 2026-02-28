# Local Development Setup

## One-Command Startup

Start backend, frontend, and Stripe webhooks together:

```bash
# From project root - install once, then run
npm install
npm run dev
```

Or use the PowerShell script (opens 3 separate windows):

```powershell
.\start-dev.ps1
```

## Payment Flow (No Manual Commands Needed)

When a user completes payment on Stripe Checkout:

1. Stripe **automatically** sends `checkout.session.completed` to your webhook
2. With `stripe listen` running, that event is forwarded to your local backend
3. Your backend confirms the booking — **no `stripe trigger` needed**

You only need `stripe listen` running. Real payments trigger webhooks automatically.

## When to Use `stripe trigger`

`stripe trigger checkout.session.completed` creates a **fake** test event. Use it only to test your webhook handler without making a real payment. It will not confirm a real booking (the fake session ID doesn't match your DB).
