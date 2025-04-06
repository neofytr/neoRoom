#  neoRoom

**neoRoom** is a cross-platform chat/client system with support for desktop (Electron), Android, and more coming soon!

---

##  Desktop Client (Electron)

The server (`server.go`) is running on an AWS EC2 free-tier instance.

To run the desktop client on **Windows** or **macOS**, follow these steps:

### 1. Prerequisites

Make sure you have **npm** installed. 

### 2. Setup

Open a terminal in client/ and run:

```bash
npm init -y
npm install electron --save-dev
```

Then, replace your `package.json` with the following:

```json
{
  "name": "neoRoom",
  "version": "1.0.0",
  "main": "main.js",
  "scripts": {
    "start": "electron ."
  }
}
```

### 3. Run the App

```bash
npm start
```

---

##  Android Client

The Android app source code is located in:

```
androidApp/neoRoom/
```

The prebuilt APK is available in:

```
androidApp/
```

###  How to use:

1. Transfer the APK to your Android device.
2. Install it (you may need to enable **Install from Unknown Sources**).
3. Launch the app!

---

##  iOS Support

**Coming soon...** 

---

##  Feedback

Feel free to submit issues or suggestions to help improve neoRoom.  
Pull requests welcome!

---


