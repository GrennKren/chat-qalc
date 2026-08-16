# Chat Qalc (Forked)

This is a fork of [chat-qalc](https://github.com/vlad2305m/chat-qalc).


## How to Use

1. **Download the portable version of Qalculate** from the [official Qalculate website](https://qalculate.github.io/downloads.html).
   - Pick the `qalculate-<version>-x64.zip` file matching your OS (e.g. `qalculate-5.12.0-x64.zip` on Windows).
2. Extract the downloaded file **into** the following directory:
   ```
   .minecraft/config/chatqalc/
   ```
   The downloaded `.zip` already contains a top-level folder named `qalculate`. After
   extraction the final layout must look like this (Windows example; on Linux the
   executable is named `qalc` instead of `qalc.exe` and lives under
   `qalculate-<version>/`):
   ```
   .minecraft/
   └─ config/
      └─ chatqalc/
         └─ qalculate/                       <-- this folder comes from the zip
            ├─ qalc.exe                      <-- the headless calculator binary
            ├─ qalculate-gtk.exe             <-- the optional GUI config app
            ├─ libqalculate/                 <-- definitions, units, currencies
            └─ ... (rest of the Qalculate! program)
   ```
   In other words: the binary the mod invokes is
   `.minecraft/config/chatqalc/qalculate/qalc.exe` on Windows, or
   `.minecraft/config/chatqalc/qalculate-<version>/qalc` on Linux.

After that, the mod should work as intended.
