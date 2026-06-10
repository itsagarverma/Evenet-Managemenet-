# 🌸 The Sneh Moments — Wedding Organizer Website

A beautiful, responsive wedding organizer website for **The Sneh Moments**, crafted with love to help couples plan their dream events.

---

## 📁 Project Structure

```
EVENET-MANAGEMENET-SA/
│
├── backend/
│   ├── node_modules/
│   ├── Dockerfile
│   ├── package.json
│   ├── package-lock.json
│   └── server.js
│
├── frontend/
│   ├── Assets/
│   │   ├── images/          # All images (AboutUS, gallery, etc.)
│   │   └── icons/           # Logo, favicon files
│   │
│   ├── css/
│   │   └── style.css        # Main stylesheet
│   │
│   ├── js/
│   │   └── script.js        # Main JavaScript file
│   │
│   ├── components/
│   │   └── services.html    # Services section (loaded dynamically)
│   │
│   ├── pages/
│   │   ├── login.html       # Login / Sign up page
│   │   ├── query.html       # Event query form
│   │   └── comingsoon.html  # Coming soon page
│   │
│   ├── index.html           # Main homepage
│   └── site.webmanifest     # PWA manifest for favicon
│
├── docker-compose.yml
└── projectholeders.txt
```

---

## ✨ Features

- 🏠 **Hero Section** — Full screen wedding background with CTA button
- 👥 **About Us** — Story and mission of The Sneh Moments
- 🎴 **Services Section** — Horizontally scrollable cards with video backgrounds and expandable details
- 🖼️ **Gallery** — Tailored gallery showcasing past events
- 📋 **Query Form** — Full event planning form with EmailJS integration
- 🔐 **Login / Signup** — User authentication page
- 📱 **Fully Responsive** — Works on mobile, tablet, and desktop
- 💌 **EmailJS Integration** — Query form sends emails directly
- 🌐 **Backend API** — Events fetched from live backend server

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Styling | Custom CSS with CSS Variables, Google Fonts |
| Icons | FontAwesome 6 |
| Email | EmailJS |
| Backend | Node.js + Express |
| Deployment | Docker, Render |

---

## 🚀 Getting Started

### Prerequisites
- [VS Code](https://code.visualstudio.com/)
- [Live Server Extension](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer)
- [Node.js](https://nodejs.org/) (for backend)

### Running the Frontend

1. Clone the repository:
```bash
git clone https://github.com/yourusername/evenet-managemenet-sa.git
cd evenet-managemenet-sa
```

2. Open the project in VS Code:
```bash
code .
```

3. Click **"Go Live"** in the bottom right of VS Code to start Live Server

4. Open your browser at:
```
http://localhost:5500/frontend/index.html
```

> ⚠️ **Important:** Always use Live Server to open the project. Opening `index.html` directly as a file will break the dynamic component loading.

### Running the Backend

```bash
cd backend
npm install
node server.js
```

Backend runs at: `http://localhost:3000`

---

## 📧 EmailJS Setup

The query form uses EmailJS to send emails. To configure:

1. Create an account at [emailjs.com](https://www.emailjs.com/)
2. Update these values in `js/script.js`:
```js
emailjs.init("YOUR_PUBLIC_KEY");
emailjs.send("YOUR_SERVICE_ID", "YOUR_TEMPLATE_ID", formData);
```

---

## 🎨 Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| Mauve | `#8d6b79` | Primary color, buttons |
| Lotus Purple | `#702A8C` | Logo, headings |
| Gold | `#c5a059` | Accent, hover effects |
| Dark | `#2c1e24` | Body text |
| Light | `#faf8f5` | Background |

---

## 📱 Responsive Breakpoints

| Breakpoint | Screen Size |
|-----------|------------|
| Large Tablet | max-width: 1024px |
| Tablet | max-width: 768px |
| Mobile | max-width: 480px |
| Small Mobile | max-width: 360px |

---

## 📂 Path Reference

| File Location | CSS/JS Path Prefix |
|--------------|-------------------|
| `frontend/index.html` | `./css/` , `./js/` , `./Assets/` |
| `frontend/pages/*.html` | `../css/` , `../js/` , `../Assets/` |

---

## 🔗 Live Links

- 🌐 **Website:** Coming Soon
- 📸 **Instagram:** [@thesnehmoments](https://www.instagram.com/thesnehmoments)
- 📧 **Email:** thesnehmoments@gmail.com
- 📞 **Phone:** +91 9302259211
- 📍 **Location:** Madhya Pradesh, India

---

## 👩‍💻 Developer Notes

- Dynamic components (services section) are loaded via `fetch()` — requires Live Server
- Favicon files should be placed in `Assets/icons/`
- All pages inside `pages/` folder use `../` prefix for asset paths
- Videos for service cards go in `Assets/videos/`
- macOS junk files (`._style.css`, `._script.js`, `.swp`) can be safely deleted

---

## 📄 License

© 2026 The Sneh Moments. All Rights Reserved.

---

*Crafted With Love 💜*
