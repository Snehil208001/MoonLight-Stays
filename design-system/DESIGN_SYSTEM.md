# Moonlight Stays — Cross-Platform Design System

**This document and [`tokens.json`](./tokens.json) are the single source of truth for UI on both clients:**

- **Web** — `moonlight-stays/` (Next.js 14, Tailwind CSS, Framer Motion)
- **Android** — `app/` (Jetpack Compose, Material 3)

Both apps MUST look and behave like the same product. A user switching between Android and web
should instantly recognize the same application — same colors, typography, spacing, radii,
component shapes, and motion. Any new screen or component starts from these tokens; never
introduce an ad-hoc color, font size, radius, or duration.

---

## 1. Design Language

**"Midnight Glassmorphism"** — dark-first, celestial, premium.

- Deep midnight backgrounds (`#0A0A1A` base) with an animated mesh gradient backdrop.
- Surfaces are translucent white glass (5–10% white) with 1px subtle borders and background blur.
- **Electric Cyan `#00FFFF`** is the single primary accent: CTAs, active states, focus rings, glows.
- **Coral `#FF7F50`** is the secondary accent: highlights, favorites, price emphasis.
- Generous spacing, large radii, soft cyan glows on interactive emphasis.
- Both clients currently ship **dark theme only** (web forces `html.dark`; Android forces the dark
  scheme). Light tokens exist in `tokens.json` for future use — implement new components against
  semantic tokens so a light theme can be enabled later without rework.

## 2. Token Implementation Map

| Token group | Canonical value | Web implementation | Android implementation |
|---|---|---|---|
| Colors | `tokens.json → color.dark` | CSS vars in `src/app/globals.css` + Tailwind `theme.extend.colors` (`midnight-*`, `accent-*`, `success`, `warning`, `error`, glass utilities `.glass`, `.glass-strong`) | `ui/theme/Color.kt` (`Midnight*`, `Accent*`, `Glass*`, `Text*`, semantic) wired into `MoonDarkColorScheme` in `Theme.kt` |
| Typography | `tokens.json → typography.scale` | Plus Jakarta Sans via `next/font` (`--font-plus-jakarta`); scale maps to Tailwind text sizes (see §3) | Plus Jakarta Sans in `res/font/` + `ui/theme/Type.kt` Material 3 `Typography` |
| Spacing | 4px base unit | Tailwind default spacing scale (`p-4` = 16px etc.) | `ui/theme/Dimens.kt → Spacing` |
| Radius | sm 8 / md 12 / lg 16 / xl 24 / pill | Tailwind defaults: `rounded-lg`=8, `rounded-xl`=12, `rounded-2xl`=16, `rounded-3xl`=24 (see §4) | `ui/theme/Dimens.kt → Radii` + `MoonShapes` in `Theme.kt` |
| Elevation / glow | `tokens.json → elevation` | `.glow-cyan`, `shadow-*` utilities in `globals.css` | Compose `shadow()` / border + `Glass*` surfaces |
| Motion | `tokens.json → motion` | Framer Motion + Tailwind `duration-*`; durations 150/250/400/500 | `ui/theme/Motion.kt` (durations + easings) |

## 3. Typography

Font: **Plus Jakarta Sans** (fallback Inter → system sans) on BOTH platforms.

| Role | Size/Line | Weight | Web (Tailwind) | Android (Material 3 role) |
|---|---|---|---|---|
| Display L | 48/56 | 800 | `text-5xl font-extrabold` | `displayLarge` |
| Display | 36/44 | 800 | `text-4xl font-extrabold` | `displayMedium` |
| Headline | 28/36 | 700 | `text-3xl font-bold` | `headlineMedium` |
| Title L | 22/28 | 700 | `text-2xl font-bold` | `titleLarge` |
| Title | 18/24 | 600 | `text-lg font-semibold` | `titleMedium` |
| Body L | 16/24 | 400 | `text-base` | `bodyLarge` |
| Body | 14/20 | 400 | `text-sm` | `bodyMedium` |
| Label | 12/16 | 500 | `text-xs font-medium` | `labelMedium` |
| Button | 15/20 | 600 | `text-[15px] font-semibold` | `labelLarge` |

## 4. Radius

| Element | Radius | Web class | Android |
|---|---|---|---|
| Chips, avatars, pills | pill | `rounded-full` | `Radii.pill` / `CircleShape` |
| Buttons, text fields | 12 | `rounded-xl` | `Radii.md` / `MaterialTheme.shapes.small` |
| Cards, images | 16 | `rounded-2xl` | `Radii.lg` / `MaterialTheme.shapes.medium` |
| Modals, bottom sheets | 24 | `rounded-3xl` | `Radii.xl` / `MaterialTheme.shapes.large` |

## 5. Core Component Specs

All shared components must follow these recipes on both platforms.

- **Primary Button** — cyan `accent.primary` fill, `onPrimary` (near-black) text, radius 12,
  height 48 (44 compact), Button type style; hover/press: slight scale 0.97 + cyan glow.
  Disabled: 38% opacity.
- **Secondary Button** — transparent glass fill (`glass.surface`), 1px `glass.border`,
  white text; hover: `glass.surfaceHover` + cyan border.
- **Text Button** — no fill, cyan text; hover underline/brighten.
- **Icon Button** — 40×40 circular glass surface, white icon; active state cyan icon.
- **Text Field / Search Bar** — glass surface, 1px glass border, radius 12, white text,
  `text.muted` placeholder; focus: cyan border + subtle glow. Search bar is pill (`rounded-full`)
  on the hero, radius 12 elsewhere.
- **Card (Hotel / Booking / Property / Review / Price / Payment)** — glass surface, 1px glass
  border, radius 16, padding 16, image at radius 16 (top corners in stacked layout);
  hover/press: translateY(-4px) + `glowCyan` border (web) / scale 0.98 press (Android).
- **Chips (filters, amenities)** — pill, glass surface + border; selected: cyan fill at 15%
  (`rgba(0,255,255,0.15)`), cyan border, cyan text.
- **Rating** — star icons in `semantic.rating` gold; value in Body weight 600.
- **Navigation** — Web: top navbar, glass-dark with blur, cyan active link. Android: same brand
  row as top app bar + bottom navigation with cyan active icon, `text.muted` inactive.
  Screen names/order must match §6.
- **Dialogs / Bottom sheets** — `glass.scrim` behind; container `background.raised` at radius 24;
  modalIn motion (fade + scale 0.95→1, 250ms).
- **Toasts / Snackbars** — glass-dark pill, white text, leading semantic icon
  (success `#00E479` / error `#FF4D6D`).
- **Empty state** — centered icon (48, `text.muted`), Title, Body secondary, optional primary button.
- **Error state** — same layout, icon in `semantic.error`, retry secondary button.
- **Loading** — full-screen: brand moon logo pulse on `background.base`; inline: cyan circular
  spinner. Skeletons: `glass.surface` blocks at content radius pulsing 0.4→0.7 opacity, 1.5s loop.
- **Success / Confirmation screen** — centered success check in `semantic.success` with glow,
  Display type, booking summary card, primary CTA.

## 6. Navigation Hierarchy (both platforms, same names & order)

```
Splash → Onboarding → Authentication (Login/Sign Up)
  → Home (search hero + featured hotels)
  → Search results
  → Property Details (photos, rooms, reviews)
  → Booking (guests, dates, promo)
  → Payment (Stripe)
  → Confirmation
  → Profile → Settings
Managers additionally: Admin Dashboard (hotels, rooms, surge, promo codes)
```

Web routes: `/` (splash/onboarding/home), `/login`, `/hotels/[id]`, `/bookings`, `/payments/*`,
`/favorites`, `/profile`, `/admin`. Android screens (`mainui/`): splashscreen, onboarding,
loginscreen, signupscreen, dashboard (guest + manager), hoteldetail — new Android screens must
reuse the web flow's naming and ordering.

## 7. Motion

| Pattern | Spec |
|---|---|
| Page/screen transition | fade + 24px slide-up, 500ms, decelerate |
| Button press | scale 0.97, 150ms |
| Card hover (web) / press (Android) | lift −4px + glow / scale 0.98, 250ms |
| Modal in | scrim fade + scale 0.95→1, 250ms |
| Bottom sheet / drawer | slide-up/in, 400ms, decelerate |
| Skeleton pulse | opacity 0.4↔0.7, 1.5s infinite |
| Mesh gradient backdrop | background-position sweep, 15s infinite |

Easing: standard `cubic-bezier(0.2, 0, 0, 1)`; decelerate `(0, 0, 0, 1)`; accelerate `(0.3, 0, 1, 1)`.
Web: Framer Motion / CSS transitions. Android: `tween(Motion.*, easing = Motion.Standard)` from
`ui/theme/Motion.kt`.

## 8. Rules

1. **Never hardcode** a hex color, font size, radius, spacing, duration, or easing in screen code.
   Consume tokens (`Tailwind classes / CSS vars` on web, `ui/theme/*` on Android).
2. Changing a token means changing it in `tokens.json` **and** both platform implementations in
   the same commit.
3. New components get a spec entry in §5 before/with implementation on the first platform, and the
   second platform ships the equivalent.
4. Screens must be indistinguishable in branding across platforms — if a screen looks like it
   belongs to a different app, it is a bug.
