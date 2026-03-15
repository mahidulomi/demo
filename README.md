# Demo Shop Management System

JavaFX + Maven shop management app with real-time LAN stock sync.

## What is implemented

- One PC can run as the **server**
- Other PCs can connect as **clients**
- Stock changes sync in real time
- Newly added custom products sync across machines
- Completed checkout sales are recorded and synced to connected clients
- Stock data is persisted in:
  - `~/.shopapp_stock.dat`
- Sales data is persisted in:
  - `~/.shopapp_sales.dat`

## Requirements

- Windows PowerShell
- JDK installed
- `JAVA_HOME` set

Example temporary setup for the current terminal:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.2"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## Run the app

From the project root:

```powershell
.\mvnw.cmd clean javafx:run
```

## Build and test

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## How to use networking

### 1. Start the server machine

Open the app on the main computer.

- Go to the **Stock Management** page
- In the network panel, enter a port like `5555`
- Click **Start Server**
- Note the server machine IP shown in the UI

### 2. Connect a client machine

Open the app on another computer in the same network.

- Go to the **Stock Management** page
- Enter the server IP
- Enter the same port, for example `5555`
- Click **Connect**

### 3. Real-time sync behavior

After connection:

- checkout on one machine updates stock on others
- custom product add in Beauty/Electronics syncs to others
- newly connected clients receive the full product catalog
- newly connected clients also receive synced sales history

## Notes

- The server is the authoritative live host during a session
- If a client disconnects, it falls back to offline mode
- Existing JavaFX warnings from newer JDKs do not block the build in current verification

